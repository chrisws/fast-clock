package com.fastclock.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class FastNotificationHelperTest {

    private fun epochFor(hour: Int, minute: Int = 0, dayOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return cal.timeInMillis
    }

    @Test
    fun `nextOccurrence - target hour later today returns today`() {
        val now = epochFor(8, 30)
        val result = FastWindowReceiver.nextOccurrence(now, 12)
        assertEquals(epochFor(12), result)
    }

    @Test
    fun `nextOccurrence - target hour already passed returns tomorrow`() {
        val now = epochFor(14, 0)
        val result = FastWindowReceiver.nextOccurrence(now, 12)
        assertEquals(epochFor(12, 0, 1), result)
    }

    @Test
    fun `nextOccurrence - exactly at target hour returns tomorrow`() {
        val now = epochFor(12, 0)
        val result = FastWindowReceiver.nextOccurrence(now, 12)
        assertEquals(epochFor(12, 0, 1), result)
    }

    @Test
    fun `nextOccurrence - midnight (hour 0) from evening returns tomorrow`() {
        val now = epochFor(22, 0)
        val result = FastWindowReceiver.nextOccurrence(now, 0)
        assertEquals(epochFor(0, 0, 1), result)
    }

    @Test
    fun `nextOccurrence - midnight (hour 0) from early morning returns today`() {
        val now = epochFor(0, 30)
        val result = FastWindowReceiver.nextOccurrence(now, 0)
        // 0:30 is past 0:00, so should return tomorrow
        assertEquals(epochFor(0, 0, 1), result)
    }

    @Test
    fun `nextOccurrence - hour 23 from morning returns today`() {
        val now = epochFor(6, 0)
        val result = FastWindowReceiver.nextOccurrence(now, 23)
        assertEquals(epochFor(23), result)
    }

    @Test
    fun `nextOccurrence - result is always at minute 0 second 0`() {
        val now = epochFor(10, 45, 30)
        val result = FastWindowReceiver.nextOccurrence(now, 15)
        val cal = Calendar.getInstance()
        cal.timeInMillis = result
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `nextOccurrence - result hour matches target`() {
        val now = epochFor(3, 0)
        for (targetHour in listOf(0, 6, 12, 18, 23)) {
            val result = FastWindowReceiver.nextOccurrence(now, targetHour)
            val cal = Calendar.getInstance()
            cal.timeInMillis = result
            assertEquals("target=$targetHour", targetHour, cal.get(Calendar.HOUR_OF_DAY))
        }
    }
}