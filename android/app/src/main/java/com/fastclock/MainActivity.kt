package com.fastclock

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fastclock.model.FastSession
import com.fastclock.model.FastState
import com.fastclock.model.Protocol
import com.fastclock.model.WeighIn
import com.fastclock.notification.FastWindowReceiver
import com.fastclock.ui.FastClockScreen
import com.fastclock.ui.theme.FastClockTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("fastclock", Context.MODE_PRIVATE)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request exact-alarm permission on Android 12+ (not auto-granted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(android.net.Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }

        val initialState = loadState()

        setContent {
            var state by mutableStateOf(initialState)

            FastClockTheme {
                FastClockScreen(
                    state = state,
                    onStateChange = { newState ->
                        state = newState
                        saveState(newState)
                        scheduleNotifications(newState)
                    }
                )
            }
        }

        scheduleNotifications(initialState)
    }

    override fun onResume() {
        super.onResume()
        // Reschedule alarms with current state (handles clock mode changes, protocol edits, etc.)
        val current = loadState()
        scheduleNotifications(current)
    }

    private fun scheduleNotifications(state: FastState) {
        val p = state.resolvedProtocol()
        FastWindowReceiver.ensureChannel(this)
        FastWindowReceiver.scheduleWindowAlarms(this, p.eatStart, p.eatEnd)
    }

    // ── Persistence ──

    private fun loadState(): FastState {
        val protocol = Protocol.fromLabel(prefs.getString("protocol", "18:6") ?: "18:6")
        val customEatStart = prefs.getInt("customEatStart", 12)
        val customEatEnd = prefs.getInt("customEatEnd", 18)
        val fastStartEpoch = prefs.getLong("fastStartEpoch", -1L).let { if (it == -1L) null else it }
        val dailyScore = prefs.getInt("dailyScore", 100)
        val scoreDate = prefs.getString("scoreDate", "") ?: ""

        val fastHistory = prefs.getString("fastHistory", "")?.let { parseFastHistory(it) } ?: emptyList()
        val weightLog = prefs.getString("weightLog", "")?.let { parseWeightLog(it) } ?: emptyList()

        return FastState(
            protocol = protocol,
            customEatStart = customEatStart,
            customEatEnd = customEatEnd,
            fastStartEpoch = fastStartEpoch,
            dailyScore = dailyScore,
            scoreDate = scoreDate,
            fastHistory = fastHistory,
            weightLog = weightLog
        )
    }

    private fun saveState(state: FastState) {
        prefs.edit().apply {
            putString("protocol", state.protocol.label)
            putInt("customEatStart", state.customEatStart)
            putInt("customEatEnd", state.customEatEnd)
            putLong("fastStartEpoch", state.fastStartEpoch ?: -1L)
            putInt("dailyScore", state.dailyScore)
            putString("scoreDate", state.scoreDate)
            putString("fastHistory", serializeFastHistory(state.fastHistory))
            putString("weightLog", serializeWeightLog(state.weightLog))
            apply()
        }
    }

    // ── Serialization helpers ──

    private fun serializeFastHistory(list: List<FastSession>): String {
        return list.joinToString(";") { s ->
            "${s.startEpoch},${s.endEpoch},${s.durationHours},${s.onTime}"
        }
    }

    private fun parseFastHistory(str: String): List<FastSession> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size != 4) return@mapNotNull null
            try {
                FastSession(
                    startEpoch = parts[0].toLong(),
                    endEpoch = parts[1].toLong(),
                    durationHours = parts[2].toDouble(),
                    onTime = parts[3].toBoolean()
                )
            } catch (_: Exception) { null }
        }
    }

    private fun serializeWeightLog(list: List<WeighIn>): String {
        return list.joinToString(";") { w ->
            "${w.dateEpoch},${w.weightKg}"
        }
    }

    private fun parseWeightLog(str: String): List<WeighIn> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size != 2) return@mapNotNull null
            try {
                WeighIn(
                    dateEpoch = parts[0].toLong(),
                    weightKg = parts[1].toDouble()
                )
            } catch (_: Exception) { null }
        }
    }
}