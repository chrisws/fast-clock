package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.Surface2
import com.fastclock.ui.theme.TextMuted
@Composable
fun HistoryDialog(
    records: List<DailyRecord>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
            .background(Bg.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("PROGRESS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Accent, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (records.isEmpty()) {
                Text("No fasts recorded yet.", fontSize = 13.sp, color = TextMuted)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(records.take(14)) { DailyRow(it) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap outside to close", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun DailyRow(record: DailyRecord) {
    val barColor = when {
        record.score >= 80 -> Color(0xFF4CAF50)
        record.score >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Surface2, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(record.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextMuted, modifier = Modifier.width(56.dp))

        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Surface)) {
                Box(modifier = Modifier.fillMaxWidth(record.score / 100f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(barColor)) {}
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("%.0fh fasted".format(record.totalHours), fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Text("${record.score}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = barColor, modifier = Modifier.width(36.dp))
    }
}
