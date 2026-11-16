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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.BuildConfig
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.Text
import com.fastclock.ui.theme.TextMuted

// ── About dialog ──

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Bg.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Fast Clock", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Text)
            Spacer(modifier = Modifier.height(4.dp))
            Text("v${BuildConfig.VERSION_NAME}", fontSize = 13.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Made with Hali", fontSize = 13.sp, color = Accent2)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "PRIVACY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val privacyItems = listOf(
                "No data is collected or shared with third parties",
                "All data is stored locally on your device only",
                "No ads, tracking, or analytics SDKs",
                "No permissions beyond app functionality",
                "All data is removed when you uninstall the app"
            )
            privacyItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("\u2022 ", fontSize = 12.sp, color = TextMuted)
                    Text(
                        item,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Tap outside to close", fontSize = 11.sp, color = TextMuted)
        }
    }
}

// ── Help dialog ──

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Bg.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How It Works", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Text)
            Spacer(modifier = Modifier.height(16.dp))

            val items = listOf(
                "You have an eating window (e.g. 12 PM \u2013 6 PM). Outside that, you fast.",
                "Tap \uD83C\uDF5E I ATE whenever you eat. In-window scores 100, outside scores 90.",
                "Log at least one meal per day to keep your streak alive.",
                "Forget to log? No entry is created \u2014 no penalty, no bloat.",
                "Long-press the button to enable rolling mode (window follows the clock)."
            )
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("\u2022 ", fontSize = 13.sp, color = TextMuted)
                    Text(
                        item,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Tap outside to close", fontSize = 11.sp, color = TextMuted)
        }
    }
}