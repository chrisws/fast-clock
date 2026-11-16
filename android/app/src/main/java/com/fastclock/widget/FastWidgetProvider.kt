package com.fastclock.widget

import android.appwidget.AppWidgetManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.fastclock.MainActivity
import com.fastclock.R
import com.fastclock.model.Protocol
import com.fastclock.model.ResolvedProtocol
import com.fastclock.ui.isInEatingWindow
import com.fastclock.ui.timeUntilEatEnd
import com.fastclock.ui.timeUntilEatStart
import java.util.Calendar

class FastWidgetProvider : android.appwidget.AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("fastclock", Context.MODE_PRIVATE)
            val protocol = Protocol.fromLabel(prefs.getString("protocol", "18:6") ?: "18:6")
            val eatStart = prefs.getInt("customEatStart", 12)
            val eatEnd = prefs.getInt("customEatEnd", 18)

            val p = ResolvedProtocol(
                fastHours = 24 - protocol.eatHours,
                eatHours = protocol.eatHours,
                eatStart = eatStart,
                eatEnd = eatEnd
            )

            val cal = Calendar.getInstance()
            val curH = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f

            val eating = isInEatingWindow(curH, p)
            val text = if (eating) {
                val remaining = timeUntilEatEnd(curH, p)
                "Eating \u00B7 ${formatHours(remaining)}"
            } else {
                val remaining = timeUntilEatStart(curH, p)
                "Fasting \u00B7 ${formatHours(remaining)}"
            }

            val views = RemoteViews(context.packageName, R.layout.widget_fast_clock)
            views.setTextViewText(R.id.widget_text, text)
            views.setInt(
                R.id.widget_dot,
                "setBackgroundResource",
                if (eating) R.color.widget_eating else R.color.widget_fasting
            )

            // Tap opens the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pi = PendingIntent.getActivity(context, 0, intent, flags)
            views.setPendingIntentTemplate(R.id.widget_text, pi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        internal fun formatHours(hours: Float): String {
            val h = hours.toInt()
            val m = ((hours - h) * 60).toInt()
            return "%02d:%02d".format(h, m)
        }
    }
}