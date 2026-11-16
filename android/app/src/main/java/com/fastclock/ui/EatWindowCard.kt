package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.model.ResolvedProtocol
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Accent2Dim
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.Text
import com.fastclock.ui.theme.TextMuted

// ── Eating window card ──

@Composable
fun EatWindowCard(
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
                fontWeight = FontWeight.Bold,
                color = Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${protocol.eatHours}h eating \u00B7 ${protocol.fastHours}h fasting",
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Benefit guidance
            Text(
                text = fastingBenefit(protocol.fastHours),
                fontSize = 13.sp,
                color = Accent2,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Protocol guidance
            Text(
                text = "Stay within this window for your best daily score. " +
                        "Fasting shifts your body to fat burning and cellular cleanup.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fasting guidance (migrated from ProtocolPanel)
            Text(
                text = "Water, black coffee, and plain tea are fine during your fast. " +
                        "Avoid anything with calories. If you feel unwell, break your fast " +
                        "and listen to your body.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\u2615 Coffee note: black coffee (no sugar, no cream) " +
                        "keeps you in a fasted state.",
                fontSize = 11.sp,
                color = Accent,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
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