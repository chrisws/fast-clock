package com.fastclock.ui

import com.fastclock.model.FastSession
import com.fastclock.model.ResolvedProtocol
import kotlin.math.roundToInt

/**
 * Pure clock/time logic extracted from FastClockScreen for unit testing.
 */

/** Returns true if [curH] falls within the eating window defined by [p]. */
fun isInEatingWindow(curH: Float, p: ResolvedProtocol): Boolean {
    return if (p.eatStart < p.eatEnd) {
        curH >= p.eatStart && curH < p.eatEnd
    } else {
        curH >= p.eatStart || curH < p.eatEnd
    }
}

/** Hours remaining until the eating window ends. */
fun timeUntilEatEnd(curH: Float, p: ResolvedProtocol): Float {
    return if (p.eatStart < p.eatEnd) {
        p.eatEnd - curH
    } else if (curH >= p.eatStart) {
        (24f - curH) + p.eatEnd
    } else {
        p.eatEnd - curH
    }
}

/** Hours until the eating window opens. */
fun timeUntilEatStart(curH: Float, p: ResolvedProtocol): Float {
    return if (p.eatStart < p.eatEnd) {
        if (curH >= p.eatStart && curH < p.eatEnd) 0f
        else if (curH < p.eatStart) p.eatStart - curH
        else (24f - curH) + p.eatStart
    } else {
        if (curH >= p.eatEnd && curH < p.eatStart) p.eatStart - curH else 0f
    }
}

/** Format fractional hours as a zero-padded HH:MM string. */
fun fmtCountdown(hours: Float): String {
    val totalMin = (hours * 60).roundToInt().coerceAtLeast(0)
    val h = totalMin / 60
    val m = totalMin % 60
    return "%02d:%02d".format(h, m)
}

/** Format a 24-hour value as a 12-hour label (e.g. 13 → "1:00 PM"). */
fun fmtHour(h: Int): String {
    val hh = if (h % 12 == 0) 12 else h % 12
    val ap = if (h < 12) "AM" else "PM"
    return "$hh:00 $ap"
}

/** Calculate the consecutive-day streak from [history] (most recent last). */
fun calcStreak(history: List<FastSession>): Int {
    if (history.isEmpty()) return 0

    fun epochDay(epoch: Long): Long = epoch / 86400000L

    val todayDay = epochDay(System.currentTimeMillis())
    val lastDay = epochDay(history.last().endEpoch)
    if (lastDay != todayDay && lastDay != todayDay - 1) return 0

    var streak = 1
    for (i in history.size - 2 downTo 0) {
        val day = epochDay(history[i].endEpoch)
        val prevDay = epochDay(history[i + 1].endEpoch)
        if (prevDay - day == 1L) {
            streak++
        } else {
            break
        }
    }
    return streak
}