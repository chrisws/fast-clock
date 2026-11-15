package com.fastclock.ui

import com.fastclock.model.FastSession
import com.fastclock.model.ResolvedProtocol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

/** Returns a short benefit description for a given fasting duration in hours. */
fun fastingBenefit(hours: Int): String {
    return when {
        hours <= 0 -> "No fasting"
        hours < 12 -> "Normal eating \u00B7 No significant fast benefit"
        hours < 14 -> "Metabolic switch \u00B7 Body shifts to fat burning"
        hours < 16 -> "Glycogen depleted \u00B7 Insulin sensitivity improves"
        hours < 18 -> "Cellular cleanup begins \u00B7 Lower diabetes risk"
        hours < 20 -> "Enhanced cellular repair \u00B7 Growth hormone rise \u00B7 Early ketosis"
        hours < 24 -> "Deep ketosis \u00B7 Peak cellular cleanup \u00B7 Hormone optimization"
        else -> "Extended fast \u00B7 Stem cell regeneration \u00B7 Metabolic reset"
    }
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

/** A single day's aggregated score. */
data class DailyRecord(
    val date: String,        // "2025-01-15"
    val label: String,       // "Mon 15" for display
    val score: Int,          // 0..100
    val sessions: Int,       // number of fasts completed that day
    val totalHours: Double   // total fasting hours that day
)

/**
 * Compute per-day scores from [history]. Returns records sorted most-recent first.
 * Scoring: start at 100, -10 per off-time violation, -5 if duration < 80% of target.
 */
fun computeDailyScores(history: List<FastSession>, targetFastHours: Int): List<DailyRecord> {
    if (history.isEmpty()) return emptyList()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labelFmt = SimpleDateFormat("EEE d", Locale.US)

    // Group sessions by date string (based on endEpoch)
    val byDate = history.groupBy { sdf.format(Date(it.endEpoch)) }

    return byDate.map { (date, sessions) ->
        var score = 100
        var totalHours = 0.0

        for ((_, _, durationHours, onTime) in sessions) {
            totalHours += durationHours
            if (!onTime) score -= 10
            if (durationHours < targetFastHours * 0.8) score -= 5
        }

        score = score.coerceIn(0, 100)

        DailyRecord(
            date = date,
            label = labelFmt.format(Date(sessions.last().endEpoch)),
            score = score,
            sessions = sessions.size,
            totalHours = totalHours
        )
    }.sortedByDescending { it.date }
}
