package com.fastclock.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "fast_clock_reminders"
const val NOTIF_EAT_START = 1001
const val NOTIF_EAT_END = 1002

/**
 * BroadcastReceiver that fires when a scheduled eating-window alarm triggers.
 */
class FastWindowReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra("request_code", 0)
        val title = when (requestCode) {
            NOTIF_EAT_START -> "Eating window opens"
            NOTIF_EAT_END -> "Time to fast"
            else -> return
        }
        val text = when (requestCode) {
            NOTIF_EAT_START -> "Your eating window is now open. Enjoy your meal!"
            NOTIF_EAT_END -> "Your eating window has closed. Time to start your fast."
            else -> ""
        }

        ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, com.fastclock.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = requestCode
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, notification)
    }

    companion object {
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Fast Clock Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications when your eating window opens and closes"
                }
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }
        }

        fun scheduleWindowAlarms(context: Context, eatStartHour: Int, eatEndHour: Int) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Calculate today's (or tomorrow's) timestamps for eatStart and eatEnd
            val now = System.currentTimeMillis()

            val startMillis = nextOccurrence(now, eatStartHour)
            val endMillis = nextOccurrence(now, eatEndHour)

            // On Android 12+ the exact-alarm permission is not auto-granted.
            // Fall back to inexact alarms if it has not been granted.
            val canUseExact = am.canScheduleExactAlarms()

            // Schedule eat-start alarm
            val startIntent = Intent(context, FastWindowReceiver::class.java).apply {
                putExtra("request_code", NOTIF_EAT_START)
            }
            val startPi = PendingIntent.getBroadcast(
                context, NOTIF_EAT_START, startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (canUseExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, startPi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startMillis, startPi)
            }

            // Schedule eat-end alarm
            val endIntent = Intent(context, FastWindowReceiver::class.java).apply {
                putExtra("request_code", NOTIF_EAT_END)
            }
            val endPi = PendingIntent.getBroadcast(
                context, NOTIF_EAT_END, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (canUseExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endMillis, endPi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endMillis, endPi)
            }
        }

        fun cancelWindowAlarms(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val startIntent = Intent(context, FastWindowReceiver::class.java)
            val startPi = PendingIntent.getBroadcast(
                context, NOTIF_EAT_START, startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(startPi)

            val endIntent = Intent(context, FastWindowReceiver::class.java)
            val endPi = PendingIntent.getBroadcast(
                context, NOTIF_EAT_END, endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(endPi)
        }

        /**
         * Returns the next occurrence of [hour] (0-23) from [nowMillis].
         * If today's occurrence has already passed, returns tomorrow's.
         */
        internal fun nextOccurrence(nowMillis: Long, hour: Int): Long {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = nowMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)

            if (cal.timeInMillis <= nowMillis) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }
    }
}