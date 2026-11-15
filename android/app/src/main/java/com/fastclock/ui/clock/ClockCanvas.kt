package com.fastclock.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.fastclock.model.ResolvedProtocol
import com.fastclock.ui.theme.Accent
import com.fastclock.ui.theme.Accent2
import com.fastclock.ui.theme.RingTrack
import com.fastclock.ui.theme.RingTrackInner
import com.fastclock.ui.theme.TextMuted
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.withRotation

private const val DRAG_NONE = 0
private const val DRAG_ROTATE = 1
private const val DRAG_RESIZE_START = 2
private const val DRAG_RESIZE_END = 3

private const val EDGE_TOLERANCE = 1.5f

/**
 * 24-hour circular clock with outer time ring and inner fasting/eating arcs.
 * In setup mode, the fasting arc becomes interactive: drag center to rotate,
 * drag edges to expand/contract.
 */
@Composable
fun ClockCanvas(
    protocol: ResolvedProtocol,
    currentHour: Float,
    modifier: Modifier = Modifier,
    setupMode: Boolean = false,
    onProtocolChange: ((eatStart: Int, eatEnd: Int) -> Unit)? = null
) {
    val pi = PI.toFloat()

    // Stable references so pointerInput (keyed on Unit) always sees latest values
    val protocolRef = rememberUpdatedState(protocol)
    val onProtocolChangeRef = rememberUpdatedState(onProtocolChange)

    Canvas(
        modifier = modifier.then(
            if (setupMode && onProtocolChange != null) {
                Modifier.pointerInput(Unit) {
                    var dragMode = DRAG_NONE
                    var startHour = 0f
                    var origEatStart = 0
                    var origEatEnd = 0
                    val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

                    detectDragGestures(
                        onDragStart = { offset ->
                            val hour = positionToHour(offset, canvasSize)
                            val p = protocolRef.value
                            dragMode = determineDragMode(hour, p)
                            startHour = hour
                            origEatStart = p.eatStart
                            origEatEnd = p.eatEnd
                        },
                        onDragEnd = { dragMode = DRAG_NONE },
                        onDragCancel = { dragMode = DRAG_NONE },
                        onDrag = { change, _ ->
                            if (dragMode == DRAG_NONE) return@detectDragGestures
                            val hour = positionToHour(change.position, canvasSize)
                            when (dragMode) {
                                DRAG_ROTATE -> {
                                    val delta = (hour - startHour).toInt()
                                    val newStart = ((origEatStart + delta) % 24 + 24) % 24
                                    val newEnd = ((origEatEnd + delta) % 24 + 24) % 24
                                    onProtocolChangeRef.value?.invoke(newStart, newEnd)
                                }
                                DRAG_RESIZE_START -> {
                                    val newStart = (hour.toInt() % 24 + 24) % 24
                                    if (newStart != origEatEnd) {
                                        onProtocolChangeRef.value?.invoke(newStart, origEatEnd)
                                    }
                                }
                                DRAG_RESIZE_END -> {
                                    val newEnd = (hour.toInt() % 24 + 24) % 24
                                    if (newEnd != origEatStart) {
                                        onProtocolChangeRef.value?.invoke(origEatStart, newEnd)
                                    }
                                }
                            }
                        }
                    )
                }
            } else Modifier
        )
    ) {
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
            val angle = (h / 24f) * 2f * pi - pi / 2f
            val isMajor = h % 6 == 0
            val isMid = h % 3 == 0
            val r1 = if (isMajor) outerR * 0.93f else if (isMid) outerR * 0.947f else outerR * 0.965f
            val r2 = outerR * 0.988f
            val cosA = cos(angle)
            val sinA = sin(angle)
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
            nativeCanvas.withRotation(Math.toDegrees(angle.toDouble()).toFloat() + 90f, lx, ly) {
                val paint = android.graphics.Paint().apply {
                    color = TextMuted.argbInt()
                    textSize = fontSize
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(label, lx, ly + fontSize / 3f, paint)
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
            startAngle = protocol.eatEnd / 24f * 360f - 90f,
            sweepAngle = protocol.fastHours / 24f * 360f,
            useCenter = false,
            topLeft = Offset(cx - innerR, cy - innerR),
            size = Size(innerR * 2f, innerR * 2f),
            style = Stroke(width = size.minDimension * 0.045f, cap = StrokeCap.Round)
        )

        // ── Eating arc (amber) ──
        drawArc(
            color = Accent,
            startAngle = protocol.eatStart / 24f * 360f - 90f,
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

        // ── Setup mode: drag handles at fasting arc edges ──
        if (setupMode) {
            val handleOuterR = size.minDimension * 0.025f
            val handleInnerR = size.minDimension * 0.015f
            for (edgeHour in listOf(protocol.eatStart.toFloat(), protocol.eatEnd.toFloat())) {
                val angle = (edgeHour / 24f) * 2f * pi - pi / 2f
                val hx = cx + innerR * cos(angle)
                val hy = cy + innerR * sin(angle)
                drawCircle(
                    color = Color.White,
                    radius = handleOuterR,
                    center = Offset(hx, hy),
                    style = Stroke(width = size.minDimension * 0.008f)
                )
                drawCircle(
                    color = Accent2,
                    radius = handleInnerR,
                    center = Offset(hx, hy)
                )
            }
        }

        // ── Time hand ──
        val timeAngle = (currentHour / 24f) * 2f * pi - pi / 2f
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

// ── Gesture helpers ──

private fun positionToHour(pos: Offset, canvasSize: Size): Float {
    val cx = canvasSize.width / 2f
    val cy = canvasSize.height / 2f
    val dx = pos.x - cx
    val dy = pos.y - cy
    val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat() // -π..π, 0=right, +ve=clockwise (screen)
    var clockAngle = angle + PI.toFloat() / 2f // shift so 0=top
    if (clockAngle < 0f) clockAngle += 2f * PI.toFloat()
    return clockAngle / (2f * PI.toFloat()) * 24f
}

private fun determineDragMode(touchHour: Float, p: ResolvedProtocol): Int {
    val distToStart = angularDistance(touchHour, p.eatStart.toFloat())
    val distToEnd = angularDistance(touchHour, p.eatEnd.toFloat())

    if (distToStart <= EDGE_TOLERANCE) return DRAG_RESIZE_START
    if (distToEnd <= EDGE_TOLERANCE) return DRAG_RESIZE_END

    // If touching the fasting arc (not eating), allow to rotate
    if (!isInEatingRange(touchHour, p.eatStart, p.eatEnd)) return DRAG_ROTATE

    return DRAG_NONE
}

private fun angularDistance(a: Float, b: Float): Float {
    val diff = abs(a - b)
    return minOf(diff, 24f - diff)
}

private fun isInEatingRange(hour: Float, start: Int, end: Int): Boolean {
    if (start < end) return hour >= start && hour < end
    return hour >= start || hour < end // wraps around midnight
}

// ── Drawing helpers ──

private fun drawInnerLabelText(
    canvas: android.graphics.Canvas,
    cx: Float, cy: Float, r: Float,
    hour: Int, text: String, color: Color,
    minDim: Float
) {
    val pi = PI.toFloat()
    val angle = (hour / 24f) * 2f * pi - pi / 2f
    val lx = cx + r * cos(angle)
    val ly = cy + r * sin(angle)
    canvas.withRotation(Math.toDegrees(angle.toDouble()).toFloat() + 90f, lx, ly) {
        val paint = android.graphics.Paint().apply {
            this.color = color.argbInt()
            textSize = minDim * 0.025f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        drawText(text, lx, ly + minDim * 0.008f, paint)
    }
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