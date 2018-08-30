package org.todoscheduler.todoscheduler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final int appWidgetId;
    JSONArray incompletelyScheduledTasks;

    TaskWidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
       getIncompletelyScheduledTasks();
        if (this.incompletelyScheduledTasks == null) {
            return 0;
        }
        return this.incompletelyScheduledTasks.length();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(
                this.context.getPackageName(), R.layout.task_widget_list_item);

        String taskName;
        try {
            JSONObject task = this.incompletelyScheduledTasks.getJSONObject(position);

            taskName = task.getString("name");
        } catch (JSONException e) {
            taskName = "*ERROR*";
        }
        row.setTextViewText(R.id.task_text, taskName);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void getIncompletelyScheduledTasks() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(
                "taskData", Context.MODE_PRIVATE);

        try {
            this.incompletelyScheduledTasks = new JSONArray(
                    sharedPreferences.getString("incompletelyScheduledTasks", "[]")
            );
        } catch (JSONException e) {
            Log.e("api", "failed to extract incompletely scheduled tasks.");
            e.printStackTrace();
        }
    }
}
