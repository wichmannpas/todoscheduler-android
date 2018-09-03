package org.todoscheduler.todoscheduler;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private final String API_URL;
    private final String AUTH_TOKEN;
    private final Context context;
    private final RequestQueue requestQueue;

    ApiClient(Context context, String apiUrl, String authToken) {
        this.context = context;
        this.API_URL = apiUrl;
        this.AUTH_TOKEN = authToken;

        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void fetchIncompletelyScheduledTasks() {
        Log.v("api", "fetching incompletely scheduled tasks");

        String url = this.API_URL + "/task/task/?incomplete";
        this.requestQueue.add(new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                SharedPreferences sharedPreferences = ApiClient.this.context.getSharedPreferences(
                        "taskData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("incompletelyScheduledTasks", response.toString());
                editor.apply();
                ApiClient.this.updateWidgets();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api", "fetching data failed. are you authenticated?");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Token " + ApiClient.this.AUTH_TOKEN);

                return headers;
            }
        });
    }

    public void fetchTaskChunks() {
        Log.v("api", "fetching task chunks");

        Calendar calendar = Calendar.getInstance();
        Date min = calendar.getTime();
        calendar.add(Calendar.DATE, 10);
        Date max = calendar.getTime();
        String minDate = this.formatDayString(min);
        String maxDate = this.formatDayString(max);

        String url = this.API_URL + "/task/chunk/?min_date=" + minDate + "&max_date=" + maxDate;
        this.requestQueue.add(new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                SharedPreferences sharedPreferences = ApiClient.this.context.getSharedPreferences(
                    "taskData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("taskChunks", response.toString());
                editor.apply();
                ApiClient.this.updateWidgets();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api", "fetching data failed. are you authenticated?");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Token " + ApiClient.this.AUTH_TOKEN);

                return headers;
            }
        });
    }

    private void updateWidgets() {
        Intent intent = new Intent(context, TaskWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, TaskWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(intent);
    }

    private String formatDayString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
}
