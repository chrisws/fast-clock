package com.fastclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastclock.model.WeighIn
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.Bg
import com.fastclock.ui.theme.RingTrack
import com.fastclock.ui.theme.Surface
import com.fastclock.ui.theme.Surface2
import com.fastclock.ui.theme.TextMuted

/**
 * Dialog for entering a weigh-in.
 */
@Composable
fun WeighInDialog(
    onDismiss: () -> Unit,
    onSave: (WeighIn) -> Unit
) {
    var weightText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bg.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "WEIGH-IN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = weightText,
                onValueChange = { text ->
                    weightText = text
                },
                singleLine = true,
                placeholder = { Text("Weight (kg)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Surface2,
                    unfocusedContainerColor = Surface2,
                    focusedIndicatorColor = Accent2,
                    unfocusedIndicatorColor = RingTrack
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface2,
                        contentColor = TextMuted
                    )
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }
                Button(
                    onClick = {
                        val w = weightText.toDoubleOrNull()
                        if (w != null && w > 0) {
                            onSave(WeighIn(System.currentTimeMillis(), w))
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent2,
                        contentColor = Bg
                    )
                ) {
                    Text("Save", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}