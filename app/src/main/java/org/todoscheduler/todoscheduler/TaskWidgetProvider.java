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
    public static final String LIST_VIEW_TYPE = "listViewType";
    public static final int LIST_VIEW_TYPE_TASKS = 0;
    public static final int LIST_VIEW_TYPE_SCHEDULE = 1;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_widget);

        Intent taskServiceIntent = new Intent(context, TaskWidgetService.class);
        taskServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        taskServiceIntent.putExtra(LIST_VIEW_TYPE, LIST_VIEW_TYPE_TASKS);
        taskServiceIntent.setData(Uri.parse(taskServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.task_list, taskServiceIntent);
        views.setEmptyView(R.id.task_list, R.id.empty_task_list);

        Intent scheduleServiceIntent = new Intent(context, TaskWidgetService.class);
        scheduleServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        scheduleServiceIntent.putExtra(LIST_VIEW_TYPE, LIST_VIEW_TYPE_SCHEDULE);
        scheduleServiceIntent.setData(Uri.parse(scheduleServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.schedule, scheduleServiceIntent);
        views.setEmptyView(R.id.schedule, R.id.empty_schedule);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.task_list);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.schedule);

        Intent refreshIntent = new Intent(context, RefreshWidgetService.class);
        PendingIntent pendingRefreshIntent = PendingIntent.getService(
                context, 0, refreshIntent, 0);
        views.setOnClickPendingIntent(R.id.refresh_button, pendingRefreshIntent);

        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(
                context, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.open_tasks_headline, pendingLaunchIntent);
        views.setOnClickPendingIntent(R.id.schedule_headline, pendingLaunchIntent);

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

