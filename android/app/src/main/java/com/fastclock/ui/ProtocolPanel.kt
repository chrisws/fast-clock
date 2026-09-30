package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.model.FastState
import com.fastclock.model.Protocol
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.RingTrack
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.Surface2
import com.fastclock.ui.theme.TextMuted

/**
 * Protocol selection panel with custom fields and guidance.
 */
@Composable
fun ProtocolPanel(
    state: FastState,
    onStateChange: (FastState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // ── Title ──
        Text(
            text = "PROTOCOL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ── Protocol buttons ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Protocol.entries.filter { it != Protocol.CUSTOM }.forEach { p ->
                val selected = state.protocol == p
                ProtocolButton(
                    label = p.label,
                    selected = selected,
                    onClick = { onStateChange(state.copy(protocol = p)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Custom button
        OutlinedButton(
            onClick = { onStateChange(state.copy(protocol = Protocol.CUSTOM)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (state.protocol == Protocol.CUSTOM) Accent2 else TextMuted
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (state.protocol == Protocol.CUSTOM) Accent2 else RingTrack
            )
        ) {
            Text("Custom", fontSize = 13.sp)
        }

        // ── Custom fields (only when custom selected) ──
        if (state.protocol == Protocol.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimeField(
                    label = "Eat start",
                    value = state.customEatStart,
                    onValueChange = { h -> onStateChange(state.copy(customEatStart = h)) },
                    modifier = Modifier.weight(1f)
                )
                TimeField(
                    label = "Eat end",
                    value = state.customEatEnd,
                    onValueChange = { h -> onStateChange(state.copy(customEatEnd = h)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Guidance ──
        Spacer(modifier = Modifier.height(16.dp))
        GuidanceCard()
    }
}

@Composable
private fun ProtocolButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        //modifier = 0,//Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Accent2 else Surface2,
            contentColor = if (selected) Bg else TextMuted
        )
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TimeField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value.toString(),
            onValueChange = { text ->
                val v = text.toIntOrNull()
                if (v != null && v in 0..23) onValueChange(v)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Surface2,
                unfocusedContainerColor = Surface2,
                focusedIndicatorColor = Accent2,
                unfocusedIndicatorColor = RingTrack
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun GuidanceCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "GUIDANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Water, black coffee, and plain tea are fine during your fast. " +
                    "Avoid anything with calories. If you feel unwell, break your fast " +
                    "and listen to your body.",
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "\u2615 Coffee note: black coffee (no sugar, no cream) " +
                    "keeps you in a fasted state.",
            fontSize = 11.sp,
            color = Accent,
            lineHeight = 16.sp
        )
    }
}