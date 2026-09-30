package com.fastclock.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
    val pi = PI.toFloat()
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.425f
        val innerR = size.minDimension * 0.3375f
        val nativeCanvas = drawContext.canvas.nativeCanvas

        // ── Outer ring track ──
        drawCircle(
            color = RingTrack,
            radius = outerR,
            center = Offset(cx, cy),
            style = Stroke(width = size.minDimension * 0.01f)
        )

        // ── Outer ticks + labels ──
        for (h in 0 until 24) {
            val angle = (h / 24f) * 2f * pi
            val isMajor = h % 6 == 0
            val isMid = h % 3 == 0
            val r1 = if (isMajor) outerR * 0.93f else if (isMid) outerR * 0.947f else outerR * 0.965f
            val r2 = outerR * 0.988f
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            val x1 = cx + r1 * cosA
            val y1 = cy + r1 * sinA
            val x2 = cx + r2 * cosA
            val y2 = cy + r2 * sinA
            drawLine(
                color = if (isMajor) TextMuted else RingTrack,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = if (isMajor) 2f else 1f
            )

            // Hour label
            val lr = outerR * 0.9f
            val lx = cx + lr * cosA
            val ly = cy + lr * sinA
            val label = hourLabel(h)
            val fontSize = when {
                isMajor -> size.minDimension * 0.03f
                isMid -> size.minDimension * 0.025f
                else -> size.minDimension * 0.02f
            }
            nativeCanvas.save()
            nativeCanvas.rotate(Math.toDegrees(angle.toDouble()).toFloat() + 90f, lx, ly)
            val paint = android.graphics.Paint().apply {
                color = TextMuted.argbInt()
                textSize = fontSize
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            nativeCanvas.drawText(label, lx, ly + fontSize / 3f, paint)
            nativeCanvas.restore()
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
            size = Size(innerR * 2f, innerR * 2f),
            style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round)
        )

        // ── Eating arc (amber) ──
        drawArc(
            color = Accent,
            startAngle = protocol.eatStart / 24f * 360f,
            sweepAngle = protocol.eatHours / 24f * 360f,
            useCenter = false,
            topLeft = Offset(cx - innerR, cy - innerR),
            size = Size(innerR * 2f, innerR * 2f),
            style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round)
        )

        // ── Inner labels: EAT / FAST ──
        val innerLabelR = innerR * 0.85f
        drawInnerLabelText(nativeCanvas, cx, cy, innerLabelR, protocol.eatStart, "EAT", Accent, size.minDimension)
        drawInnerLabelText(nativeCanvas, cx, cy, innerLabelR, protocol.eatEnd, "FAST", Accent2, size.minDimension)

        // ── Time hand ──
        val timeAngle = (currentHour / 24f) * 2f * pi
        val hx = cx + outerR * cos(timeAngle).toFloat()
        val hy = cy + outerR * sin(timeAngle).toFloat()
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

private fun drawInnerLabelText(
    canvas: android.graphics.Canvas,
    cx: Float, cy: Float, r: Float,
    hour: Int, text: String, color: Color,
    minDim: Float
) {
    val pi = PI.toFloat()
    val angle = (hour / 24f) * 2f * pi
    val lx = cx + r * cos(angle).toFloat()
    val ly = cy + r * sin(angle).toFloat()
    canvas.save()
    canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat() + 90f, lx, ly)
    val paint = android.graphics.Paint().apply {
        this.color = color.argbInt()
        textSize = minDim * 0.025f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }
    canvas.drawText(text, lx, ly + minDim * 0.008f, paint)
    canvas.restore()
}

private fun Color.argbInt(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

private fun hourLabel(h: Int): String {
    val hh = if (h % 12 == 0) 12 else h % 12
    return "$hh${if (h < 12) "a" else "p"}"
}