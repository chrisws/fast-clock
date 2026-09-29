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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.model.FastState
import com.fastclock.model.Protocol
import com.fastclock.ui.ProtocolPanel
import com.fastclock.ui.WeighInDialog
import com.fastclock.ui.clock.ClockCanvas
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.TextMuted
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun FastClockScreen(state: FastState, onStateChange: (FastState) -> Unit) {
    var currentHour by remember { mutableStateOf(0f) }
    var showProtocolPanel by remember { mutableStateOf(false) }
    var showWeighIn by remember { mutableStateOf(false) }

    // Tick every second
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentHour = cal.get(Calendar.HOUR_OF_DAY) +
                    cal.get(Calendar.MINUTE) / 60f +
                    cal.get(Calendar.SECOND) / 3600f
            kotlinx.coroutines.delay(1000L)
        }
    }

    val p = state.resolvedProtocol()
    val isFasting = state.fastStartEpoch != null
    val inEatWindow = isInEatingWindow(currentHour, p)

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
            // ── Top bar: weigh-in + settings buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showWeighIn = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2696\uFE0F",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { showProtocolPanel = !showProtocolPanel },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2699\uFE0F",
                        fontSize = 20.sp
                    )
                }
            }

            // ── Clock ──
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                ClockCanvas(
                    protocol = p,
                    currentHour = currentHour,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                // Center button
                Button(
                    onClick = {
                        if (isFasting) {
                            onStateChange(state.copy(fastStartEpoch = null))
                        } else {
                            onStateChange(state.copy(fastStartEpoch = System.currentTimeMillis()))
                        }
                    },
                    shape = CircleShape,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
            }

            // ── Status ──
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusText(isFasting, inEatWindow, currentHour, p, state),
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            // ── Stats bar ──
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(p.fastHours.toString() + "h", "Fasting")
                StatItem(p.eatHours.toString() + "h", "Eating")
                StatItem(state.dailyScore.toString(), "Score")
                StatItem("0", "Streak")
            }
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
    }
}

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

private fun isInEatingWindow(curH: Float, p: com.fastclock.model.ResolvedProtocol): Boolean {
    return if (p.eatStart < p.eatEnd) {
        curH >= p.eatStart && curH < p.eatEnd
    } else {
        curH >= p.eatStart || curH < p.eatEnd
    }
}

private fun statusText(
    isFasting: Boolean, inEat: Boolean,
    curH: Float, p: com.fastclock.model.ResolvedProtocol,
    state: FastState
): String {
    return when {
        isFasting -> {
            val elapsed = (System.currentTimeMillis() - (state.fastStartEpoch ?: 0L)) / 3600000f
            "Fasting \u00B7 ${fmtCountdown(elapsed)} elapsed"
        }
        inEat -> {
            val remaining = if (p.eatStart < p.eatEnd) p.eatEnd - curH
            else (24 - curH) + p.eatEnd
            "Eating \u00B7 ${fmtCountdown(remaining.toFloat())} until fast"
        }
        else -> {
            val remaining = if (p.eatStart < p.eatEnd) {
                if (curH < p.eatStart) p.eatStart - curH else (24 - curH) + p.eatStart
            } else {
                if (curH >= p.eatEnd && curH < p.eatStart) p.eatStart - curH else 0f
            }
            "Fasting \u00B7 ${fmtCountdown(remaining.toFloat())} remaining"
        }
    }
}

private fun fmtCountdown(hours: Float): String {
    val totalMin = (hours * 60).roundToInt().coerceAtLeast(0)
    val h = totalMin / 60
    val m = totalMin % 60
    return "%02d:%02d".format(h, m)
}