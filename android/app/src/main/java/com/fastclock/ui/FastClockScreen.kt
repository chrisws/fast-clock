package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.model.FastSession
import com.fastclock.model.FastState
import com.fastclock.model.Protocol
import com.fastclock.model.ResolvedProtocol
import com.fastclock.ui.clock.ClockCanvas
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Accent2Dim
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.TextMuted
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FastClockScreen(state: FastState, onStateChange: (FastState) -> Unit) {
    var currentHour by remember { mutableFloatStateOf(0f) }
    var showProtocolPanel by remember { mutableStateOf(false) }
    var showWeighIn by remember { mutableStateOf(false) }
    var showEatCard by remember { mutableStateOf(false) }
    var setupMode by remember { mutableStateOf(false) }
    var timeString by remember { mutableStateOf("") }

    // Tick every second
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentHour = cal.get(Calendar.HOUR_OF_DAY) +
                    cal.get(Calendar.MINUTE) / 60f +
                    cal.get(Calendar.SECOND) / 3600f
            val h12 = if (cal.get(Calendar.HOUR_OF_DAY) % 12 == 0) 12 else cal.get(Calendar.HOUR_OF_DAY) % 12
            val ap = if (cal.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM"
            timeString = "$h12:${"%02d".format(cal.get(Calendar.MINUTE))} $ap"
            kotlinx.coroutines.delay(1000L.milliseconds)
        }
    }

    val baseProtocol = state.resolvedProtocol()
    val p = if (state.clockMode) {
        val eatStart = currentHour.toInt() % 24
        val eatEnd = (eatStart + baseProtocol.eatHours) % 24
        ResolvedProtocol(24 - baseProtocol.eatHours, baseProtocol.eatHours, eatStart, eatEnd)
    } else {
        baseProtocol
    }
    val isFasting = state.fastStartEpoch != null
    val inEatWindow = isInEatingWindow(currentHour, p)
    val streak = calcStreak(state.fastHistory)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            TopBar(
                state = state,
                onStateChange = onStateChange,
                onToggleProtocol = { showProtocolPanel = !showProtocolPanel },
                onWeighIn = { showWeighIn = true },
                setupMode = setupMode,
                onToggleSetup = { setupMode = !setupMode }
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ClockArea(
                    protocol = p,
                    currentHour = currentHour,
                    isFasting = isFasting,
                    inEatWindow = inEatWindow,
                    state = state,
                    onStateChange = onStateChange,
                    onToggleEatCard = { showEatCard = !showEatCard },
                    setupMode = setupMode,
                    onProtocolChange = { eatStart, eatEnd ->
                        onStateChange(state.copy(
                            protocol = Protocol.CUSTOM,
                            customEatStart = eatStart,
                            customEatEnd = eatEnd
                        ))
                    }
                )
            }

            // Digital clock display
            Text(
                text = timeString,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.clockMode) Accent2 else TextMuted
            )

            BenefitSummary(protocol = p)

            StatusSection(
                isFasting = isFasting,
                inEatWindow = inEatWindow,
                currentHour = currentHour,
                protocol = p,
                state = state
            )

            WeighInBar(state = state, onStateChange = onStateChange)

            StatsBar(
                protocol = p,
                dailyScore = state.dailyScore,
                streak = streak
            )
        }

        // ── Protocol panel overlay ──
        if (showProtocolPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg.copy(alpha = 0.6f))
                    .clickable { showProtocolPanel = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                ProtocolPanel(
                    state = state,
                    onStateChange = onStateChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                )
            }
        }

        // ── Weigh-in dialog ──
        if (showWeighIn) {
            WeighInDialog(
                onDismiss = { showWeighIn = false },
                onSave = { w ->
                    onStateChange(state.copy(weightLog = state.weightLog + w))
                }
            )
        }

        // ── Eating window card ──
        if (showEatCard) {
            EatWindowCard(
                protocol = p,
                currentHour = currentHour,
                onDismiss = { showEatCard = false }
            )
        }
    }
}

// ── Top bar: protocol badge + action buttons ──

@Composable
private fun TopBar(
    state: FastState,
    onStateChange: (FastState) -> Unit,
    onToggleProtocol: () -> Unit,
    onWeighIn: () -> Unit,
    setupMode: Boolean,
    onToggleSetup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Protocol badge
        Box(
            modifier = Modifier
                .background(Accent2Dim, RoundedCornerShape(20.dp))
                .clickable { onToggleProtocol() }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (state.protocol == Protocol.CUSTOM)
                    "${state.customEatStart}:${state.customEatEnd}"
                else state.protocol.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Accent2
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Clock mode toggle
            Box(
                modifier = Modifier
                    .background(if (state.clockMode) Accent2Dim else Surface, RoundedCornerShape(20.dp))
                    .clickable { onStateChange(state.copy(clockMode = !state.clockMode)) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (state.clockMode) "CLOCK ON" else "CLOCK OFF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (state.clockMode) Accent2 else TextMuted
                )
            }
            // Weigh-in button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onWeighIn() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u2696\uFE0F", fontSize = 20.sp)
            }
            // Settings button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onToggleProtocol() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u2699\uFE0F", fontSize = 20.sp)
            }
            // Setup mode toggle
            Box(
                modifier = Modifier
                    .background(if (setupMode) Accent2Dim else Surface, RoundedCornerShape(20.dp))
                    .clickable { onToggleSetup() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SETUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (setupMode) Accent2 else TextMuted
                )
            }
        }
    }
}

// ── Clock area: canvas + center button + digital time ──

@Composable
private fun ClockArea(
    protocol: ResolvedProtocol,
    currentHour: Float,
    isFasting: Boolean,
    inEatWindow: Boolean,
    state: FastState,
    onStateChange: (FastState) -> Unit,
    onToggleEatCard: () -> Unit,
    setupMode: Boolean = false,
    onProtocolChange: ((eatStart: Int, eatEnd: Int) -> Unit)? = null
) {
    Box(contentAlignment = Alignment.Center) {
        ClockCanvas(
            protocol = protocol,
            currentHour = currentHour,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            setupMode = setupMode,
            onProtocolChange = onProtocolChange
        )

        // Center button
        Button(
            onClick = {
                if (isFasting) {
                    // Break fast: record session, apply score penalty
                    val startEpoch = state.fastStartEpoch!!
                    val now = System.currentTimeMillis()
                    val duration = (now - startEpoch) / 3600000.0
                    val onTime = isInEatingWindow(currentHour, protocol)
                    val newScore = if (onTime) state.dailyScore
                        else maxOf(0, state.dailyScore - 10)
                    val session = FastSession(
                        startEpoch = startEpoch,
                        endEpoch = now,
                        durationHours = duration,
                        onTime = onTime
                    )
                    onStateChange(state.copy(
                        fastStartEpoch = null,
                        dailyScore = newScore,
                        fastHistory = state.fastHistory + session
                    ))
                } else {
                    // Start fast: penalty if in eating window
                    val newScore = if (inEatWindow) maxOf(0, state.dailyScore - 10)
                        else state.dailyScore
                    onStateChange(state.copy(
                        fastStartEpoch = System.currentTimeMillis(),
                        dailyScore = newScore
                    ))
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFasting) Accent else Surface,
                contentColor = if (isFasting) Bg else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(110.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isFasting) "\uD83C\uDF5D" else "\u23F3",
                    fontSize = 26.sp
                )
                Text(
                    text = if (isFasting) "BREAK FAST" else "START FAST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Info button (eating window)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
                .size(28.dp)
                .background(Surface, CircleShape)
                .clickable { onToggleEatCard() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\u24D8", fontSize = 14.sp, color = TextMuted)
        }
    }
}

// ── Status section ──

@Composable
private fun StatusSection(
    isFasting: Boolean,
    inEatWindow: Boolean,
    currentHour: Float,
    protocol: ResolvedProtocol,
    state: FastState
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = statusText(isFasting, inEatWindow, currentHour, protocol, state),
        color = TextMuted,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))
}

// ── Benefit summary (always visible) ──

@Composable
private fun BenefitSummary(protocol: ResolvedProtocol) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${protocol.fastHours}h fast  \u00B7  Eat ${fmtHour(protocol.eatStart)} \u2013 ${fmtHour(protocol.eatEnd)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Accent2,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = fastingBenefit(protocol.fastHours),
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Stats bar ──

@Composable
private fun StatsBar(
    protocol: ResolvedProtocol,
    dailyScore: Int,
    streak: Int
) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        StatItem(protocol.fastHours.toString() + "h", "Fasting")
        StatItem(protocol.eatHours.toString() + "h", "Eating")
        StatItem(dailyScore.toString(), "Score")
        StatItem(streak.toString(), "Streak")
    }
}

// ── Weigh-in bar ──

@Composable
private fun WeighInBar(state: FastState, onStateChange: (FastState) -> Unit) {
    val latest = state.weightLog.lastOrNull()
    val first = state.weightLog.firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WEIGHT",
            fontSize = 11.sp,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (latest != null) "${latest.weightKg} kg" else "\u2014",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (latest != null && first != null && state.weightLog.size > 1) {
            val diff = latest.weightKg - first.weightKg
            val sign = if (diff <= 0) "\u2193" else "\u2191"
            Text(
                text = "$sign ${"%.1f".format(kotlin.math.abs(diff))} from start",
                fontSize = 12.sp,
                color = if (diff <= 0) Accent2 else Accent
            )
        }
    }
}

// ── Eating window card ──

@Composable
private fun EatWindowCard(
    protocol: ResolvedProtocol,
    currentHour: Float,
    onDismiss: () -> Unit
) {
    val inEat = isInEatingWindow(currentHour, protocol)
    val countdown = if (inEat) {
        "Closes in " + fmtCountdown(timeUntilEatEnd(currentHour, protocol))
    } else {
        "Opens in " + fmtCountdown(timeUntilEatStart(currentHour, protocol))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EATING WINDOW",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Accent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${fmtHour(protocol.eatStart)} \u2013 ${fmtHour(protocol.eatEnd)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${protocol.eatHours} hours",
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(Accent2Dim, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = countdown,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Accent2
                )
            }
        }
    }
}

// ── Stat item ──

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Helpers ──

private fun statusText(
    isFasting: Boolean, inEat: Boolean,
    curH: Float, p: ResolvedProtocol,
    state: FastState
): String {
    return when {
        isFasting -> {
            val elapsed = (System.currentTimeMillis() - (state.fastStartEpoch ?: 0L)) / 3600000f
            "Fasting \u00B7 ${fmtCountdown(elapsed)} elapsed"
        }
        inEat -> {
            "Eating \u00B7 ${fmtCountdown(timeUntilEatEnd(curH, p))} until fast"
        }
        else -> {
            "Fasting \u00B7 ${fmtCountdown(timeUntilEatStart(curH, p))} remaining"
        }
    }
}