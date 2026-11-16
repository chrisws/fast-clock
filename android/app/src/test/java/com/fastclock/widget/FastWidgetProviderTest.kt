package com.fastclock.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class FastWidgetProviderTest {

    @Test
    fun `formatHours - zero`() {
        assertEquals("00:00", FastWidgetProvider.formatHours(0f))
    }

    @Test
    fun `formatHours - whole hours`() {
        assertEquals("02:00", FastWidgetProvider.formatHours(2f))
        assertEquals("12:00", FastWidgetProvider.formatHours(12f))
        assertEquals("23:00", FastWidgetProvider.formatHours(23f))
    }

    @Test
    fun `formatHours - half hour`() {
        assertEquals("01:30", FastWidgetProvider.formatHours(1.5f))
    }

    @Test
    fun `formatHours - quarter hour`() {
        assertEquals("00:15", FastWidgetProvider.formatHours(0.25f))
    }

    @Test
    fun `formatHours - 45 minutes`() {
        assertEquals("05:45", FastWidgetProvider.formatHours(5.75f))
    }


}