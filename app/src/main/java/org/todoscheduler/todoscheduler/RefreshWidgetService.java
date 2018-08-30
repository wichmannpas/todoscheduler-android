package org.todoscheduler.todoscheduler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

public class RefreshWidgetService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.fetchData();

        return Service.START_NOT_STICKY;
    }

    private void fetchData() {
        Toast.makeText(getApplicationContext(), "Fetching data ...", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                "auth", Context.MODE_PRIVATE);
        String storedAuthToken = sharedPreferences.getString("authToken", "null");

        if (storedAuthToken.equals("null")) {
            return;
        }

        ApiClient client = new ApiClient(
                getApplicationContext(), getString(R.string.api_url), storedAuthToken);

        client.fetchIncompletelyScheduledTasks();
    }
}
