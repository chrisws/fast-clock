package com.fastclock.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DrawScope
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.fastclock.model.ResolvedProtocol
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.RingTrack
import com.fastclock.ui.theme.RingTrackInner
import com.fastclock.ui.theme.TextMuted
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 24-hour circular clock with outer time ring and inner fasting/eating arcs.
 * Mirrors the SVG from prototype/index.html.
 */
@Composable
fun ClockCanvas(
    protocol: ResolvedProtocol,
    currentHour: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.425f   // ~170/400
        val innerR = size.minDimension * 0.3375f  // ~135/400
        val innerCirc = 2f * PI * innerR

        // ── Outer ring track ──
        drawCircle(
            color = RingTrack,
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = size.minDimension * 0.01f)
        )

        // ── Outer ticks + labels ──
        for (h in 0 until 24) {
            val angle = (h / 24f) * 2f * PI
            val isMajor = h % 6 == 0
            val isMid = h % 3 == 0
            val r1 = if (isMajor) outerR * 0.93f else if (isMid) outerR * 0.947f else outerR * 0.965f
            val r2 = outerR * 0.988f
            val x1 = cx + r1 * cos(angle)
            val y1 = cy + r1 * sin(angle)
            val x2 = cx + r2 * cos(angle)
            val y2 = cy + r2 * sin(angle)
            drawLine(
                color = if (isMajor) TextMuted else RingTrack,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = if (isMajor) 2f else 1f
            )

            // Hour label
            val lr = outerR * 0.9f
            val lx = cx + lr * cos(angle)
            val ly = cy + lr * sin(angle)
            val label = hourLabel(h)
            val fontSize = when {
                isMajor -> size.minDimension * 0.03f
                isMid -> size.minDimension * 0.025f
                else -> size.minDimension * 0.02f
            }
            drawContext.canvas.nativeCanvas.apply {
                val paint = Paint().apply {
                    this.color = TextMuted
                    textSize = fontSize
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                // Rotate text to be perpendicular to the ring
                save()
                rotate(Math.toDegrees(angle).toFloat() + 90f, lx, ly)
                drawText(label, lx, ly + fontSize / 3f, paint)
                restore()
            }
        }

        // ── Inner ring track ──
        drawCircle(
            color = RingTrackInner,
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = size.minDimension * 0.045f)
        )

        // ── Fasting arc (teal) ──
        drawArc(
            color = Accent2,
            startAngle = protocol.eatEnd / 24f * 360f,
            sweepAngle = protocol.fastHours / 24f * 360f,
            useCenter = false,
            topLeft = Offset(cx - innerR, cy - innerR),
            size = androidx.compose.ui.geometry.Size(innerR * 2f, innerR * 2f),
            style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round)
        )

        // ── Eating arc (amber) ──
        drawArc(
            color = Accent,
            startAngle = protocol.eatStart / 24f * 360f,
            sweepAngle = protocol.eatHours / 24f * 360f,
            useCenter = false,
            topLeft = Offset(cx - innerR, cy - innerR),
            size = androidx.compose.ui.geometry.Size(innerR * 2f, innerR * 2f),
            style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round)
        )

        // ── Inner labels: EAT / FAST ──
        val innerLabelR = innerR * 0.85f
        drawInnerLabel(cx, cy, innerLabelR, protocol.eatStart, "EAT", Accent)
        drawInnerLabel(cx, cy, innerLabelR, protocol.eatEnd, "FAST", Accent2)

        // ── Time hand ──
        val timeAngle = (currentHour / 24f) * 2f * PI
        val hx = cx + outerR * cos(timeAngle)
        val hy = cy + outerR * sin(timeAngle)
        drawLine(
            color = TextMuted,
            start = Offset(cx, cy),
            end = Offset(hx, hy),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // ── Time dot ──
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.0125f,
            center = Offset(hx, hy)
        )
    }
}

private fun DrawScope.drawInnerLabel(
    cx: Float, cy: Float, r: Float,
    hour: Int, text: String, color: Color
) {
    val angle = (hour / 24f) * 2f * PI
    val lx = cx + r * cos(angle)
    val ly = cy + r * sin(angle)
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            this.color = color
            textSize = size.minDimension * 0.025f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        save()
        rotate(Math.toDegrees(angle).toFloat() + 90f, lx, ly)
        drawText(text, lx, ly + size.minDimension * 0.008f, paint)
        restore()
    }
}

private fun hourLabel(h: Int): String {
    val hh = if (h % 12 == 0) 12 else h % 12
    return "$hh${if (h < 12) "a" else "p"}"
}