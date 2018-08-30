package org.todoscheduler.todoscheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api", error.getMessage());
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
}
