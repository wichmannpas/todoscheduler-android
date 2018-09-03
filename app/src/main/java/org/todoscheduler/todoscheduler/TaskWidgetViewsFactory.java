package org.todoscheduler.todoscheduler;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TaskWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final int appWidgetId;
    private final int listViewType;
    private JSONArray incompletelyScheduledTasks;
    private List<JSONObject> scheduleToday;
    private List<JSONObject> scheduleTomorrow;

    TaskWidgetViewsFactory(Context context, Intent intent) {
        this.listViewType = intent.getIntExtra(
                TaskWidgetProvider.LIST_VIEW_TYPE, TaskWidgetProvider.LIST_VIEW_TYPE_TASKS);
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
        if (this.listViewType == TaskWidgetProvider.LIST_VIEW_TYPE_TASKS) {
            return this.getTaskCount();
        } else if (this.listViewType == TaskWidgetProvider.LIST_VIEW_TYPE_SCHEDULE) {
            return this.getScheduleCount();
        }
        Log.e("widget", "invalid list view type");
        return 0;
    }

    private int getTaskCount() {
        getIncompletelyScheduledTasks();
        if (this.incompletelyScheduledTasks == null) {
            return 0;
        }
        return this.incompletelyScheduledTasks.length();
    }

    private int getScheduleCount() {
        getSchedule();
        if (this.scheduleToday == null || this.scheduleTomorrow == null) {
            return 0;
        }
        int todayCount = this.scheduleToday.size();
        if (todayCount == 0) {
            todayCount = 1;
        }
        int tomorrowCount = this.scheduleTomorrow.size();
        if (tomorrowCount == 0) {
            tomorrowCount = 1;
        }
        return 2 + todayCount + tomorrowCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (this.listViewType == TaskWidgetProvider.LIST_VIEW_TYPE_TASKS) {
            return this.getTaskViewAt(position);
        } else if (this.listViewType == TaskWidgetProvider.LIST_VIEW_TYPE_SCHEDULE) {
            return this.getScheduleViewAt(position);
        }
        Log.e("widget", "invalid list view type");
        return null;
    }

    private RemoteViews getTaskViewAt(int position) {
        RemoteViews row = new RemoteViews(
                this.context.getPackageName(), R.layout.task_widget_task_list_item);

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

    private RemoteViews getScheduleViewAt(int position) {
        int todayHeadline = 0;
        int todayStart = todayHeadline + 1;
        int tomorrowHeadline = todayStart + this.scheduleToday.size();
        if (this.scheduleToday.size() == 0) {
            tomorrowHeadline += 1;
        }
        int tomorrowStart = tomorrowHeadline + 1;

        RemoteViews row;

        String headline = null;
        if (position == todayHeadline) {
            headline = "today";
        } else if (position == tomorrowHeadline) {
            headline = "tomorrow";
        }

        if (headline != null) {
            // headline row

            row = new RemoteViews(
                    this.context.getPackageName(), R.layout.task_widget_schedule_list_headline);

            row.setTextViewText(R.id.headline, headline);
        } else {
            // content row
            List<JSONObject> list = this.scheduleToday;
            if (position >= tomorrowStart) {
                // tomorrow
                position -= tomorrowStart;
                list = this.scheduleTomorrow;
            } else {
                // today
                position -= todayStart;
            }
            if (list.size() == 0) {
                return new RemoteViews(
                        this.context.getPackageName(), R.layout.task_widget_schedule_list_empty);
            }

            row = new RemoteViews(
                    this.context.getPackageName(), R.layout.task_widget_schedule_list_item);

            String taskName;
            double duration = 0;
            try {
                JSONObject chunk = list.get(position);

                if (chunk.getBoolean("finished")) {
                    row = new RemoteViews(
                            this.context.getPackageName(),
                            R.layout.task_widget_schedule_finished_list_item);
                }

                taskName = chunk.getJSONObject("task").getString("name");
                duration = chunk.getDouble("duration");
            } catch (JSONException e) {
                taskName = "*ERROR*";
            }

            row.setTextViewText(R.id.task_text, taskName);
            row.setTextViewText(R.id.duration, Double.toString(duration) + "h");
        }

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
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

    private void getSchedule() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(
                "taskData", Context.MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        Date dateToday = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date dateTomorrow = calendar.getTime();
        String today = this.formatDayString(dateToday);
        String tomorrow = this.formatDayString(dateTomorrow);

        this.scheduleToday = new LinkedList<JSONObject>();
        this.scheduleTomorrow = new LinkedList<JSONObject>();

        try {
            JSONArray chunks = new JSONArray(sharedPreferences.getString("taskChunks", "[]"));
            for (int i = 0; i < chunks.length(); i++) {
                JSONObject chunk = chunks.getJSONObject(i);
                if (chunk.getString("day").equals(today)) {
                    this.scheduleToday.add(chunk);
                } else if (chunk.getString("day").equals(tomorrow)) {
                    this.scheduleTomorrow.add(chunk);
                }
            }
        } catch (JSONException e) {
            Log.e("api", "failed to extract incompletely scheduled tasks.");
            e.printStackTrace();

            return;
        }

        // sort lists
        Comparator<JSONObject> comparator = new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    boolean o1Finished = o1.getBoolean("finished");
                    boolean o2Finished = o2.getBoolean("finished");
                    // we display finished task chunks below unfinished task chunks in the widget
                    if (o1Finished && !o2Finished) {
                        return 1;
                    } else if (o2Finished && !o1Finished) {
                        return -1;
                    }

                    // equal finished state, compare day order
                    return Integer.compare(o1.getInt("day_order"), o2.getInt("day_order"));
                } catch (JSONException e) {
                    Log.e("widget", "failed to extract day order from widget");
                    return 1;
                }
            }
        };
        Collections.sort(this.scheduleToday, comparator);
        Collections.sort(this.scheduleTomorrow, comparator);
    }

    private String formatDayString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}
