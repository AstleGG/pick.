package com.example.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.PickHistoryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class PickWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PICK = "com.example.action.WIDGET_PICK"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidgetState(context, appWidgetManager, appWidgetId, "Tap 'Pick' to decide!", null)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_PICK) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val pickDao = db.pickDao()
                    val latestPick = pickDao.getLatestPickSuspended()

                    if (latestPick != null && latestPick.options.isNotEmpty()) {
                        // Pick a random option
                        val randomIndex = Random.nextInt(latestPick.options.size)
                        val selectedResult = latestPick.options[randomIndex]

                        // Insert history entry to sync with the main App screen
                        pickDao.insertHistoryEntry(
                            PickHistoryEntry(
                                pickId = latestPick.id,
                                selectedOption = selectedResult
                            )
                        )

                        // Play pop sound effect for satisfying sensory feedback
                        try {
                            SoundEffects.playPop()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Update all active widgets with the result
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, PickWidgetProvider::class.java)
                        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

                        for (widgetId in allWidgetIds) {
                            updateWidgetState(
                                context,
                                appWidgetManager,
                                widgetId,
                                selectedResult,
                                latestPick.title
                            )
                        }
                    } else {
                        // No active picks/options
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, PickWidgetProvider::class.java)
                        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

                        for (widgetId in allWidgetIds) {
                            updateWidgetState(
                                context,
                                appWidgetManager,
                                widgetId,
                                "No choices found!",
                                "Open app to add choices"
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun updateWidgetState(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        resultText: String,
        pickTitle: String?
    ) {
        val views = RemoteViews(context.packageName, R.layout.pick_widget_layout)

        // Set result text
        views.setTextViewText(R.id.widget_result_text, resultText)

        // Set active pick title if available, otherwise fetch in background
        if (pickTitle != null) {
            views.setTextViewText(R.id.widget_pick_title, pickTitle)
        } else {
            // Default text
            views.setTextViewText(R.id.widget_pick_title, "Loading...")
            // Fetch the latest pick title in the background to show correctly initially
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val latestPick = AppDatabase.getDatabase(context).pickDao().getLatestPickSuspended()
                    val title = latestPick?.title ?: "No Saved Decider"
                    views.setTextViewText(R.id.widget_pick_title, title)
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Set intent for "Pick" button
        val pickIntent = Intent(context, PickWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_PICK
        }
        val pickPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            pickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pick_button, pickPendingIntent)

        // Set intent for "Open" button (launches main app screen)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_open_button, openPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
