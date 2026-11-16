package com.fastclock.ui.clock

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.fastclock.model.ResolvedProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockCanvasTest {

    private val canvasSize = Size(400f, 400f)
    private val center = Offset(200f, 200f)

    // ── positionToHour ──

    @Test
    fun `positionToHour - top center is hour 0`() {
        val hour = positionToHour(Offset(200f, 10f), canvasSize)
        assertEquals(0f, hour, 0.5f)
    }

    @Test
    fun `positionToHour - right center is hour 6`() {
        val hour = positionToHour(Offset(390f, 200f), canvasSize)
        assertEquals(6f, hour, 0.5f)
    }

    @Test
    fun `positionToHour - bottom center is hour 12`() {
        val hour = positionToHour(Offset(200f, 390f), canvasSize)
        assertEquals(12f, hour, 0.5f)
    }

    @Test
    fun `positionToHour - left center is hour 18`() {
        val hour = positionToHour(Offset(10f, 200f), canvasSize)
        assertEquals(18f, hour, 0.5f)
    }

    @Test
    fun `positionToHour - returns value in 0 to 24 range`() {
        // Test several angles around the circle
        for (deg in 0 until 360 step 15) {
            val rad = Math.toRadians(deg.toDouble())
            val x = 200f + (190f * Math.cos(rad)).toFloat()
            val y = 200f + (190f * Math.sin(rad)).toFloat()
            val hour = positionToHour(Offset(x, y), canvasSize)
            assertTrue("hour=$hour for deg=$deg", hour >= 0f && hour < 24f)
        }
    }

    // ── determineDragMode ──

    @Test
    fun `determineDragMode - near eatStart returns RESIZE_START`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(2, determineDragMode(12f, p)) // DRAG_RESIZE_START
    }

    @Test
    fun `determineDragMode - near eatEnd returns RESIZE_END`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(3, determineDragMode(18f, p)) // DRAG_RESIZE_END
    }

    @Test
    fun `determineDragMode - in fasting arc returns ROTATE`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(1, determineDragMode(3f, p)) // DRAG_ROTATE (3am is in fast period)
    }

    @Test
    fun `determineDragMode - in eating arc returns NONE`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(0, determineDragMode(15f, p)) // DRAG_NONE (3pm is in eat period)
    }

    @Test
    fun `determineDragMode - wrap-around fasting arc returns ROTATE`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // 12am is in the eating window (20→4), so 6am should be fasting
        assertEquals(1, determineDragMode(6f, p)) // DRAG_ROTATE
    }

    @Test
    fun `determineDragMode - wrap-around eating arc returns NONE`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // 11pm is in the eating window (20→4)
        assertEquals(0, determineDragMode(23f, p)) // DRAG_NONE
    }

    // ── angularDistance ──

    @Test
    fun `angularDistance - same point is zero`() {
        assertEquals(0f, angularDistance(5f, 5f), 0.001f)
    }

    @Test
    fun `angularDistance - adjacent hours is one`() {
        assertEquals(1f, angularDistance(5f, 6f), 0.001f)
    }

    @Test
    fun `angularDistance - opposite is twelve`() {
        assertEquals(12f, angularDistance(0f, 12f), 0.001f)
    }

    @Test
    fun `angularDistance - wrap-around takes shortest path`() {
        // 23 to 1 is 2 hours (wrap), not 22
        assertEquals(2f, angularDistance(23f, 1f), 0.001f)
    }

    @Test
    fun `angularDistance - symmetric`() {
        assertEquals(angularDistance(3f, 20f), angularDistance(20f, 3f), 0.001f)
    }

    // ── isInEatingRange ──

    @Test
    fun `isInEatingRange - normal range - inside`() {
        assertTrue(isInEatingRange(14f, 12, 18))
        assertTrue(isInEatingRange(12f, 12, 18))
    }

    @Test
    fun `isInEatingRange - normal range - outside`() {
        assertFalse(isInEatingRange(6f, 12, 18))
        assertFalse(isInEatingRange(18f, 12, 18)) // end is exclusive
        assertFalse(isInEatingRange(23f, 12, 18))
    }

    @Test
    fun `isInEatingRange - wrap-around - inside after midnight`() {
        assertTrue(isInEatingRange(2f, 20, 4))
        assertTrue(isInEatingRange(0f, 20, 4))
    }

    @Test
    fun `isInEatingRange - wrap-around - inside before midnight`() {
        assertTrue(isInEatingRange(22f, 20, 4))
        assertTrue(isInEatingRange(20f, 20, 4))
    }

    @Test
    fun `isInEatingRange - wrap-around - outside`() {
        assertFalse(isInEatingRange(12f, 20, 4))
        assertFalse(isInEatingRange(4f, 20, 4)) // end is exclusive
    }

    // ── hourLabel ──

    @Test
    fun `hourLabel - midnight is 12a`() {
        assertEquals("12a", hourLabel(0))
    }

    @Test
    fun `hourLabel - 6am`() {
        assertEquals("6a", hourLabel(6))
    }

    @Test
    fun `hourLabel - noon is 12p`() {
        assertEquals("12p", hourLabel(12))
    }

    @Test
    fun `hourLabel - 6pm`() {
        assertEquals("6p", hourLabel(18))
    }

    @Test
    fun `hourLabel - 11pm`() {
        assertEquals("11p", hourLabel(23))
    }

    @Test
    fun `hourLabel - all 24 hours produce valid labels`() {
        for (h in 0 until 24) {
            val label = hourLabel(h)
            assertTrue("label='$label' for h=$h", label.endsWith("a") || label.endsWith("p"))
            val numPart = label.dropLast(1).toInt()
            assertTrue("num=$numPart for h=$h", numPart in 1..12)
        }
    }
}