package org.todoscheduler.todoscheduler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setAllowFileAccess(true);

        webView.setWebChromeClient(new TodoSchedulerWebChromeClient());
        webView.setWebViewClient(new TodoSchedulerWebViewClient());

        webView.loadUrl(getString(R.string.client_url));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "auth", Context.MODE_PRIVATE);
                String storedAuthToken = sharedPreferences.getString("authToken", "null");

                if (storedAuthToken.equals("null")) {
                    return;
                }

                ApiClient client = new ApiClient(
                        getApplicationContext(), getString(R.string.api_url), storedAuthToken);
                client.fetchIncompletelyScheduledTasks();
                client.fetchTaskChunks();
            }
        });
    }

    private class TodoSchedulerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("todoscheduler.org")) {
                return false;
            }

            // use external browser for external links
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:console.log('ANDROID:authToken:' + localStorage.getItem('authToken'));");
        }
    }

    private class TodoSchedulerWebChromeClient extends WebChromeClient {
        public boolean onConsoleMessage(ConsoleMessage message) {
            String[] mes = message.message().split(":");

            if (!mes[0].equals("ANDROID") || mes.length < 3) {
                // message is not relevant, do not handle it
                return false;
            }

            if (mes[1].equals("authToken")) {
                String authToken = mes[2];

                if (authToken.equals("null")) {
                    // not authenticated
                    Log.v("auth", "did not extract an auth token");
                    return true;
                }

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        "auth", Context.MODE_PRIVATE);
                String storedAuthToken = sharedPreferences.getString("authToken", "null");

                if (authToken.equals(storedAuthToken)) {
                    Log.v("auth", "auth token is unchanged");
                    return true;
                }

                Log.v("auth", "updating stored auth token");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("authToken", authToken);
                editor.apply();

                return true;
            } else {
                return false;
            }
        }
    }
}
