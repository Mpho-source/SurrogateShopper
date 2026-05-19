package com.example.surrogateshopper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final String URL_MESSAGES =
            "https://wmc.ms.wits.ac.za/students/sgroup2715/getMessage.php";

    private LinearLayout container;
    private ScrollView scrollView;

    private String requestId;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            fetchMessages();
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        container = findViewById(R.id.messageContainer);
        scrollView = findViewById(R.id.scrollView);

        requestId = getIntent().getStringExtra("REQUEST_ID");

        if (requestId == null || requestId.isEmpty()) {
            showEmpty("No order selected");
            return;
        }

        fetchMessages();
        handler.post(refreshTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshTask);
    }

    private void fetchMessages() {

        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .build();

        Request req = new Request.Builder()
                .url(URL_MESSAGES)
                .post(body)
                .build();

        HTTP.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showEmpty("Failed to load messages"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(json);
                        render(arr);
                    } catch (Exception e) {
                        showEmpty("Invalid server response");
                    }
                });
            }
        });
    }

    private void render(JSONArray arr) {
        container.removeAllViews();

        if (arr.length() == 0) {
            showEmpty("No messages yet");
            return;
        }

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                String msg = obj.optString("message_text", "");

                String name = obj.optString("sender_name", "Unknown");
                String time = obj.optString("sent_at", "");

                LinearLayout bubble = new LinearLayout(this);
                bubble.setOrientation(LinearLayout.VERTICAL);
                bubble.setPadding(20, 20, 20, 20);
                bubble.setBackgroundColor(0x22FFFFFF);

                TextView t1 = new TextView(this);
                t1.setText(name);
                t1.setTextColor(0xAAFFFFFF);

                TextView t2 = new TextView(this);
                t2.setText(msg);
                t2.setTextColor(0xFFFFFFFF);

                TextView t3 = new TextView(this);
                t3.setText(time);
                t3.setTextColor(0x66FFFFFF);

                bubble.addView(t1);
                bubble.addView(t2);
                bubble.addView(t3);

                container.addView(bubble);

            } catch (Exception ignored) {}
        }

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void showEmpty(String text) {
        container.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0x88FFFFFF);
        tv.setGravity(Gravity.CENTER);

        container.addView(tv);
    }
}
