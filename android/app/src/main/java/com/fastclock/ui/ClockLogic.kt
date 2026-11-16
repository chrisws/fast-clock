package com.fastclock.ui

import com.fastclock.model.FastSession
import com.fastclock.model.FastState
import com.fastclock.model.ResolvedProtocol
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure clock/time logic extracted from FastClockScreen for unit testing.
 */

/**
 * Resolve the effective protocol for the current state.
 * In clock mode, the eating window rolls with the current hour.
 */
fun resolveProtocol(state: FastState, currentHour: Float): ResolvedProtocol {
    val base = state.resolvedProtocol()
    return if (state.clockMode) {
        val eatStart = currentHour.toInt() % 24
        val eatEnd = (eatStart + base.eatHours) % 24
        ResolvedProtocol(24 - base.eatHours, base.eatHours, eatStart, eatEnd)
    } else {
        base
    }
}

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

/** Returns a short description for a given fasting duration in hours. */
fun fastingBenefit(hours: Int): String {
    return when {
        hours <= 0 -> "No fasting"
        hours < 12 -> "Normal eating \u00B7 No significant fast window"
        hours < 14 -> "A gentle start \u00B7 Easy to maintain"
        hours < 16 -> "A popular beginner protocol \u00B7 16:8"
        hours < 18 -> "A step up \u00B7 18:6 is a common target"
        hours < 20 -> "An advanced protocol \u00B7 20:4"
        hours < 24 -> "A serious commitment \u00B7 23:1"
        else -> "Extended fast \u00B7 Pushing your limits"
    }
}

/** Calculate the consecutive-day streak from [history] (most recent last). */
fun calcStreak(history: List<FastSession>): Int {
    if (history.isEmpty()) return 0

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun localDay(epoch: Long): String = sdf.format(Date(epoch))

    fun dayBefore(epoch: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epoch
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    val todayStr = localDay(System.currentTimeMillis())
    val lastDay = localDay(history.last().epoch)
    if (lastDay != todayStr && lastDay != dayBefore(System.currentTimeMillis())) return 0

    var streak = 1
    for (i in history.size - 2 downTo 0) {
        if (localDay(history[i].epoch) == dayBefore(history[i + 1].epoch)) {
            streak++
        } else {
            break
        }
    }
    return streak
}

/** A single day's aggregated score. */
data class DailyRecord(
    val date: String,        // "2025-01-15"
    val label: String,       // "Mon 15" for display
    val score: Int,          // 0..100
    val eatCount: Int        // number of eat events that day
)

/**
 * Compute per-day scores from [history]. Returns records sorted most-recent first.
 * Scoring: start at 100, -10 per off-time eat event.
 */
fun computeDailyScores(history: List<FastSession>, targetFastHours: Int): List<DailyRecord> {
    if (history.isEmpty()) return emptyList()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labelFmt = SimpleDateFormat("EEE d", Locale.US)

    // Group eat events by date string
    val byDate = history.groupBy { sdf.format(Date(it.epoch)) }

    return byDate.map { (date, events) ->
        var score = 100

        for (event in events) {
            if (!event.onTime) score -= 10
        }

        score = score.coerceIn(0, 100)

        DailyRecord(
            date = date,
            label = labelFmt.format(Date(events.last().epoch)),
            score = score,
            eatCount = events.size
        )
    }.sortedByDescending { it.date }
}
