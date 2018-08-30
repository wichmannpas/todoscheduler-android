package org.todoscheduler.todoscheduler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class TaskWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_widget);

        Intent serviceIntent = new Intent(context, TaskWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.task_list, serviceIntent);
        views.setEmptyView(R.id.task_list, R.id.empty_task_list);

        // TODO: refresh data instead of launching MainActivity
        Intent refreshIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingRefreshIntent = PendingIntent.getActivity(
                context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.refresh_button, pendingRefreshIntent);

        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(
                context, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.open_tasks_headline, pendingLaunchIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

