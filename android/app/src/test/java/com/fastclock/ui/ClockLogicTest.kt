package com.fastclock.ui

import com.fastclock.model.FastSession
import java.util.Calendar
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
        assertFalse(isInEatingWindow(18f, p))
        assertFalse(isInEatingWindow(23f, p))
    }

    @Test
    fun `isInEatingWindow - wrap-around window - inside`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        assertTrue(isInEatingWindow(22f, p))
        assertTrue(isInEatingWindow(2f, p))
        assertTrue(isInEatingWindow(20f, p))
    }

    @Test
    fun `isInEatingWindow - wrap-around window - outside`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
        assertFalse(isInEatingWindow(12f, p))
        assertFalse(isInEatingWindow(4f, p))
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
        assertEquals(2.0, timeUntilEatEnd(2f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatEnd - wrap-around - inside before midnight`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
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
        assertEquals(8.0, timeUntilEatStart(12f, p).toDouble(), 0.001)
    }

    @Test
    fun `timeUntilEatStart - wrap-around - inside eating window returns zero`() {
        val p = ResolvedProtocol(fastHours = 16, eatHours = 8, eatStart = 20, eatEnd = 4)
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
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, offsetDays.toInt())
        cal.set(Calendar.HOUR_OF_DAY, hour)
        return cal.timeInMillis
    }

    @Test
    fun `calcStreak - empty history returns zero`() {
        assertEquals(0, calcStreak(emptyList()))
    }

    @Test
    fun `calcStreak - single eat event today returns one`() {
        val event = FastSession(epoch = epochForDay(0, 12), onTime = true)
        assertEquals(1, calcStreak(listOf(event)))
    }

    @Test
    fun `calcStreak - single eat event yesterday returns one`() {
        val event = FastSession(epoch = epochForDay(-1, 12), onTime = true)
        assertEquals(1, calcStreak(listOf(event)))
    }

    @Test
    fun `calcStreak - two consecutive days returns two`() {
        val day1 = FastSession(epoch = epochForDay(-1, 12), onTime = true)
        val day2 = FastSession(epoch = epochForDay(0, 12), onTime = true)
        assertEquals(2, calcStreak(listOf(day1, day2)))
    }

    @Test
    fun `calcStreak - gap in days breaks streak`() {
        val day1 = FastSession(epoch = epochForDay(-3, 12), onTime = true)
        val day2 = FastSession(epoch = epochForDay(-1, 12), onTime = true)
        assertEquals(1, calcStreak(listOf(day1, day2)))
    }

    @Test
    fun `calcStreak - eat event two days ago returns zero`() {
        val event = FastSession(epoch = epochForDay(-2, 12), onTime = true)
        assertEquals(0, calcStreak(listOf(event)))
    }

    @Test
    fun `calcStreak - consecutive days at different times`() {
        val day1 = FastSession(epoch = epochForDay(-1, 6), onTime = true)
        val day2 = FastSession(epoch = epochForDay(0, 18), onTime = true)
        assertEquals(2, calcStreak(listOf(day1, day2)))
    }

    // ── computeDailyScores ──

    @Test
    fun `computeDailyScores - empty history returns empty`() {
        assertEquals(emptyList<DailyRecord>(), computeDailyScores(emptyList(), 18))
    }

    @Test
    fun `computeDailyScores - all on-time gives 100`() {
        val events = listOf(
            FastSession(epoch = epochForDay(0, 12), onTime = true),
            FastSession(epoch = epochForDay(0, 18), onTime = true)
        )
        val records = computeDailyScores(events, 18)
        assertEquals(1, records.size)
        assertEquals(100, records[0].score)
        assertEquals(2, records[0].eatCount)
    }

    @Test
    fun `computeDailyScores - off-time event deducts 10`() {
        val events = listOf(
            FastSession(epoch = epochForDay(0, 12), onTime = true),
            FastSession(epoch = epochForDay(0, 3), onTime = false)
        )
        val records = computeDailyScores(events, 18)
        assertEquals(90, records[0].score)
    }

    @Test
    fun `computeDailyScores - multiple off-time events stack`() {
        val events = listOf(
            FastSession(epoch = epochForDay(0, 3), onTime = false),
            FastSession(epoch = epochForDay(0, 5), onTime = false),
            FastSession(epoch = epochForDay(0, 2), onTime = false)
        )
        val records = computeDailyScores(events, 18)
        assertEquals(70, records[0].score)
    }

    @Test
    fun `computeDailyScores - score floors at zero`() {
        val events = (1..12).map { FastSession(epoch = epochForDay(0, 3), onTime = false) }
        val records = computeDailyScores(events, 18)
        assertEquals(0, records[0].score)
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
        assertTrue(result.contains("fat burning"))
    }

    @Test
    fun `fastingBenefit - 14 to 15h`() {
        val result = fastingBenefit(14)
        assertTrue(result.contains("Glycogen depleted"))
        assertTrue(result.contains("Insulin sensitivity"))
    }

    @Test
    fun `fastingBenefit - 16 to 17h`() {
        val result = fastingBenefit(16)
        assertTrue(result.contains("Cellular cleanup"))
        assertTrue(result.contains("diabetes risk"))
    }

    @Test
    fun `fastingBenefit - 18 to 19h`() {
        val result = fastingBenefit(18)
        assertTrue(result.contains("Enhanced cellular repair"))
        assertTrue(result.contains("Growth hormone"))
        assertTrue(result.contains("ketosis"))
    }

    @Test
    fun `fastingBenefit - 20 to 23h`() {
        val result = fastingBenefit(20)
        assertTrue(result.contains("Deep ketosis"))
        assertTrue(result.contains("Hormone optimization"))
    }

    @Test
    fun `fastingBenefit - 24h and above`() {
        val result = fastingBenefit(24)
        assertTrue(result.contains("Extended fast"))
        assertTrue(result.contains("Stem cell"))
    }

    @Test
    fun `fastingBenefit - boundary values are consistent`() {
        assertNotEquals(fastingBenefit(11), fastingBenefit(12))
        assertNotEquals(fastingBenefit(13), fastingBenefit(14))
        assertNotEquals(fastingBenefit(15), fastingBenefit(16))
        assertNotEquals(fastingBenefit(17), fastingBenefit(18))
        assertNotEquals(fastingBenefit(19), fastingBenefit(20))
        assertNotEquals(fastingBenefit(23), fastingBenefit(24))
    }
}