package com.rkulikowsky.zmanim


import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager


class ZmanimAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.i("YOSSEF","pim")
        if (intent!!.action.equals("update_widget")) {
            // Manual or automatic widget update started
            val remoteViews = RemoteViews(context!!.packageName,R.layout.zmanim_app_widget
            )

            val cityPref = intent.getStringArrayListExtra("city")?: arrayListOf()

            val dayTimes = DayTimes(cityName = cityPref[0].getStringFromResources(context), latitude = cityPref[1], longitude = cityPref[2])
            Log.i("YOSSEF",cityPref.toString())

            // Update text, images, whatever - here
            remoteViews.apply {
                setTextViewText(R.id.appwidget_text, dayTimes.cityName)
                setTextViewText(R.id.widget_netz, dayTimes.netz)
                setTextViewText(R.id.widget_chatzot, dayTimes.chatzot)
                setTextViewText(R.id.widget_shkia, dayTimes.shkia)
                setTextViewText(R.id.widget_tzet, dayTimes.tzet)
                setTextViewText(R.id.widget_ma, dayTimes.kSMaguen)
                setTextViewText(R.id.widget_gra, dayTimes.kSGra)
            }
            // Trigger widget layout update
            AppWidgetManager.getInstance(context).updateAppWidget(
                ComponentName(context, ZmanimAppWidget::class.java), remoteViews
            )
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val shared = PreferenceManager.getDefaultSharedPreferences(context)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.zmanim_app_widget)
    val cityRaw = shared.getString("city", null)?:"jerusalem,31.768318,35.213711"
    val cityPref = cityRaw.split(',')
    val dayTimes = DayTimes(cityName = cityPref[0].getStringFromResources(context), latitude = cityPref[1], longitude = cityPref[2])
    views.apply {
        setTextViewText(R.id.appwidget_text, dayTimes.cityName)
        setTextViewText(R.id.widget_netz, dayTimes.netz)
        setTextViewText(R.id.widget_chatzot, dayTimes.chatzot)
        setTextViewText(R.id.widget_shkia, dayTimes.shkia)
        setTextViewText(R.id.widget_tzet, dayTimes.tzet)
        setTextViewText(R.id.widget_ma, dayTimes.kSMaguen)
        setTextViewText(R.id.widget_gra, dayTimes.kSGra)
    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)


}

private fun String.getStringFromResources(context: Context): String {
    val test = context.resources.getIdentifier(this, "string", context.packageName)
    return if (test==0) this else context.resources.getString(test)
}
