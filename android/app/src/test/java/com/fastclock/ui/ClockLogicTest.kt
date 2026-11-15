package com.fastclock.ui

import com.fastclock.model.FastSession
import com.fastclock.model.ResolvedProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockLogicTest {

    // ── isInEatingWindow ──

    @Test
    fun `isInEatingWindow - normal window - inside`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertTrue(isInEatingWindow(14f, p))
        assertTrue(isInEatingWindow(12f, p))
    }

    @Test
    fun `isInEatingWindow - normal window - outside`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertFalse(isInEatingWindow(6f, p))
        assertFalse(isInEatingWindow(18f, p)) // end is exclusive
        assertFalse(isInEatingWindow(23f, p))
    }

    @Test
    fun `isInEatingWindow - wrap-around window - inside`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        assertTrue(isInEatingWindow(22f, p))
        assertTrue(isInEatingWindow(2f, p))
        assertTrue(isInEatingWindow(20f, p)) // start is inclusive
    }

    @Test
    fun `isInEatingWindow - wrap-around window - outside`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        assertFalse(isInEatingWindow(12f, p))
        assertFalse(isInEatingWindow(4f, p)) // end is exclusive
        assertFalse(isInEatingWindow(19.9f, p))
    }

    // ── timeUntilEatEnd ──

    @Test
    fun `timeUntilEatEnd - normal window - inside`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(4.0, timeUntilEatEnd(14f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatEnd - wrap-around - inside after midnight`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // At hour 2, end is at 4 → 2 hours remaining
        assertEquals(2.0, timeUntilEatEnd(2f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatEnd - wrap-around - inside before midnight`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // At hour 22, end is at 4 → (24-22) + 4 = 6 hours remaining
        assertEquals(6.0, timeUntilEatEnd(22f, p).toDouble(), 0.001)
    }

    // ── timeUntilEatStart ──

    @Test
    fun `timeUntilEatStart - normal window - before window`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(6.0, timeUntilEatStart(6f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatStart - normal window - inside window returns zero`() {
        val p = ResolvedProtocol(fastHours = 18, eatHours = 6, eatStart = 12, eatEnd = 18)
        assertEquals(0.0, timeUntilEatStart(14f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatStart - wrap-around - in fast period`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // At hour 12, start is at 20 → 8 hours until start
        assertEquals(8.0, timeUntilEatStart(12f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatStart - wrap-around - inside eating window returns zero`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        // At hour 22 (inside eating window) → 0
        assertEquals(0.0, timeUntilEatStart(22f, p).toDouble(), 0.001)
    }

    // ── fmtCountdown ──

    @Test
    fun `fmtCountdown - whole hours`() {
        assertEquals("02:00", fmtCountdown(2f))
        assertEquals("00:00", fmtCountdown(0f))
        assertEquals("23:59", fmtCountdown(23.983333f))
    }

    @Test
    fun `fmtCountdown - fractional hours`() {
        assertEquals("01:30", fmtCountdown(1.5f))
        assertEquals("00:45", fmtCountdown(0.75f))
    }

    @Test
    fun `fmtCountdown - negative coerced to zero`() {
        assertEquals("00:00", fmtCountdown(-1f))
    }

    // ── fmtHour ──

    @Test
    fun `fmtHour - midnight`() {
        assertEquals("12:00 AM", fmtHour(0))
    }

    @Test
    fun `fmtHour - noon`() {
        assertEquals("12:00 PM", fmtHour(12))
    }

    @Test
    fun `fmtHour - morning and afternoon`() {
        assertEquals("6:00 AM", fmtHour(6))
        assertEquals("1:00 PM", fmtHour(13))
        assertEquals("11:00 PM", fmtHour(23))
    }

    // ── calcStreak ──

    private fun epochForDay(offsetDays: Long, hour: Int = 12): Long {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        // Normalize to midday today, then offset
        val todayMidnight = now - (now % oneDay)
        return todayMidnight + (offsetDays * oneDay) + (hour * 3600000L)
    }

    @Test
    fun `calcStreak - empty history returns zero`() {
        assertEquals(0, calcStreak(emptyList()))
    }

    @Test
    fun `calcStreak - single fast ended today returns one`() {
        // Fast ended today at noon — streak should be 1
        val session = FastSession(
            startEpoch = epochForDay(0, 12) - 18 * 3600000L,
            endEpoch = epochForDay(0, 12),
            durationHours = 18.0,
            onTime = true
        )
        assertEquals("Streak should be 1 for a fast ending today", 1, calcStreak(listOf(session)))
    }

    @Test
    fun `calcStreak - single fast ended yesterday returns one`() {
        val session = FastSession(
            startEpoch = epochForDay(-1, 12) - 18 * 3600000L,
            endEpoch = epochForDay(-1, 12),
            durationHours = 18.0,
            onTime = true
        )
        assertEquals("Streak should be 1 for a fast ending yesterday", 1, calcStreak(listOf(session)))
    }

    @Test
    fun `calcStreak - two consecutive days returns two`() {
        val day1 = FastSession(
            startEpoch = epochForDay(-1, 12) - 18 * 3600000L,
            endEpoch = epochForDay(-1, 12),
            durationHours = 18.0, onTime = true
        )
        val day2 = FastSession(
            startEpoch = epochForDay(0, 12) - 18 * 3600000L,
            endEpoch = epochForDay(0, 12),
            durationHours = 18.0, onTime = true
        )
        assertEquals("Two consecutive days should give streak of 2", 2, calcStreak(listOf(day1, day2)))
    }

    @Test
    fun `calcStreak - gap in days breaks streak`() {
        val day1 = FastSession(
            startEpoch = epochForDay(-3, 12) - 18 * 3600000L,
            endEpoch = epochForDay(-3, 12),
            durationHours = 18.0, onTime = true
        )
        val day2 = FastSession(
            startEpoch = epochForDay(-1, 12) - 18 * 3600000L,
            endEpoch = epochForDay(-1, 12),
            durationHours = 18.0, onTime = true
        )
        // Gap: day -3 and day -1 are not consecutive → streak is 1 (only the most recent)
        assertEquals("Gap should break streak to 1", 1, calcStreak(listOf(day1, day2)))
    }

    @Test
    fun `calcStreak - fast ended two days ago returns zero`() {
        val session = FastSession(
            startEpoch = epochForDay(-2, 12) - 18 * 3600000L,
            endEpoch = epochForDay(-2, 12),
            durationHours = 18.0, onTime = true
        )
        assertEquals("Fast ending 2+ days ago should give streak 0", 0, calcStreak(listOf(session)))
    }

    @Test
    fun `calcStreak - consecutive days at different times of day`() {
        // Day -1 ends at 6:00, Day 0 ends at 18:00 — still consecutive calendar days
        val day1 = FastSession(
            startEpoch = epochForDay(-1, 6) - 18 * 3600000L,
            endEpoch = epochForDay(-1, 6),
            durationHours = 18.0, onTime = true
        )
        val day2 = FastSession(
            startEpoch = epochForDay(0, 18) - 18 * 3600000L,
            endEpoch = epochForDay(0, 18),
            durationHours = 18.0, onTime = true
        )
        assertEquals("Consecutive calendar days at different times should give streak 2", 2, calcStreak(listOf(day1, day2)))
    }

    // ── fastingBenefit ──

    @Test
    fun `fastingBenefit - zero hours`() {
        assertEquals("No fasting", fastingBenefit(0))
    }

    @Test
    fun `fastingBenefit - negative hours`() {
        assertEquals("No fasting", fastingBenefit(-3))
    }

    @Test
    fun `fastingBenefit - under 12h`() {
        val result = fastingBenefit(10)
        assertTrue(result.contains("No significant"))
    }

    @Test
    fun `fastingBenefit - 12 to 13h`() {
        val result = fastingBenefit(12)
        assertTrue(result.contains("Metabolic switch"))
    }

    @Test
    fun `fastingBenefit - 14 to 15h`() {
        val result = fastingBenefit(14)
        assertTrue(result.contains("Glycogen depletion"))
    }

    @Test
    fun `fastingBenefit - 16 to 17h`() {
        val result = fastingBenefit(16)
        assertTrue(result.contains("Autophagy onset"))
    }

    @Test
    fun `fastingBenefit - 18 to 19h`() {
        val result = fastingBenefit(18)
        assertTrue(result.contains("Enhanced autophagy"))
        assertTrue(result.contains("ketosis"))
    }

    @Test
    fun `fastingBenefit - 20 to 23h`() {
        val result = fastingBenefit(20)
        assertTrue(result.contains("Deep ketosis"))
        assertTrue(result.contains("GH spike"))
    }

    @Test
    fun `fastingBenefit - 24h and above`() {
        val result = fastingBenefit(24)
        assertTrue(result.contains("Extended fast"))
        assertTrue(result.contains("Stem cell"))
    }

    @Test
    fun `fastingBenefit - boundary values are consistent`() {
        // Each boundary should give a different tier than the one below
        val tiers = (0..30).map { fastingBenefit(it) }
        // Verify monotonic progression: tier at 12 != tier at 11, etc.
        assertNotEquals(fastingBenefit(11), fastingBenefit(12))
        assertNotEquals(fastingBenefit(13), fastingBenefit(14))
        assertNotEquals(fastingBenefit(15), fastingBenefit(16))
        assertNotEquals(fastingBenefit(17), fastingBenefit(18))
        assertNotEquals(fastingBenefit(19), fastingBenefit(20))
        assertNotEquals(fastingBenefit(23), fastingBenefit(24))
    }
}