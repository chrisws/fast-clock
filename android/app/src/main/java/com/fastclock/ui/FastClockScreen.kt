package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.fastclock.ui.theme.Text
import com.fastclock.ui.theme.TextMuted
import java.util.Calendar
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FastClockScreen(state: FastState, onStateChange: (FastState) -> Unit) {
    var currentHour by remember { mutableFloatStateOf(0f) }
    var showWeighIn by remember { mutableStateOf(false) }
    var showEatCard by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
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

    val p = resolveProtocol(state, currentHour)
    val inEatWindow = isInEatingWindow(currentHour, p)
    val isFasting = !inEatWindow
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
                onWeighIn = { showWeighIn = true },
                onHistory = { showHistory = true },
                onAbout = { showAbout = true },
                onHelp = { showHelp = true },
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

        // ── History dialog ──
        if (showHistory) {
            HistoryDialog(
                records = computeDailyScores(state.fastHistory, p.fastHours),
                onDismiss = { showHistory = false }
            )
        }

        // ── Help dialog ──
        if (showHelp) {
            HelpDialog(
                onDismiss = { showHelp = false }
            )
        }

        // ── About dialog ──
        if (showAbout) {
            AboutDialog(
                onDismiss = { showAbout = false }
            )
        }
    }
}

// ── Top bar: protocol badge + action buttons ──

@Composable
private fun TopBar(
    state: FastState,
    onStateChange: (FastState) -> Unit,
    onWeighIn: () -> Unit,
    onHistory: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    setupMode: Boolean,
    onToggleSetup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Setup mode toggle (plate icon)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (setupMode) Accent2Dim else Surface, CircleShape)
                .clickable { onToggleSetup() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83C\uDF7D", fontSize = 20.sp)
        }

        // Weigh-in button
        Box(
            modifier = Modifier.size(40.dp).clickable { onWeighIn() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\u2696\uFE0F", fontSize = 20.sp)
        }

        // History button (progress receipt)
        Box(
            modifier = Modifier.size(40.dp).clickable { onHistory() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83E\uDDFE", fontSize = 20.sp)
        }

        // Help button (fried egg)
        Box(
            modifier = Modifier.size(40.dp).clickable { onHelp() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83C\uDF73", fontSize = 20.sp)
        }

        // About button (package)
        Box(
            modifier = Modifier.size(40.dp).clickable { onAbout() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83D\uDCE6", fontSize = 20.sp)
        }
    }
}

// ── Clock area: canvas + center button + digital time ──

@OptIn(ExperimentalFoundationApi::class)
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

        // Center button: tap to start/break fast, long-press to toggle rolling mode
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(Accent, CircleShape)
                .combinedClickable(
                    onClick = {
                        // Log "I ate" event
                        val now = System.currentTimeMillis()
                        val onTime = isInEatingWindow(currentHour, protocol)
                        val newScore = if (onTime) state.dailyScore
                            else maxOf(0, state.dailyScore - 10)
                        val event = FastSession(
                            epoch = now,
                            onTime = onTime
                        )
                        onStateChange(state.copy(
                            dailyScore = newScore,
                            fastHistory = state.fastHistory + event
                        ))
                    },
                    onLongClick = { onStateChange(state.copy(clockMode = !state.clockMode)) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83C\uDF5E",
                    fontSize = 26.sp,
                    color = Bg
                )
                Text(
                    text = "I ATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Bg
                )

                // Rolling mode indicator (small dot when active)
                if (state.clockMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.size(6.dp).background(Bg, CircleShape)
                    )
                }
            }

            // Subtle ring when rolling mode is active
            if (state.clockMode) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(2.dp, Accent2.copy(alpha = 0.5f), CircleShape)
                )
            }
        }

        // Info button (eating window)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
                .size(40.dp)
                .background(Surface, CircleShape)
                .clickable { onToggleEatCard() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83C\uDF74", fontSize = 20.sp, color = TextMuted)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fastingBenefit(protocol.fastHours),
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

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
            fontWeight = FontWeight.Bold,
            color = Text
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
        inEat -> {
            "Eating window \u00B7 ${fmtCountdown(timeUntilEatEnd(curH, p))} until fast"
        }
        else -> {
            "Fasting \u00B7 ${fmtCountdown(timeUntilEatStart(curH, p))} until eating"
        }
    }
}


