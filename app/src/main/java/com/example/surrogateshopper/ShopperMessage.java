package com.example.surrogateshopper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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


public class ShopperMessage extends AppCompatActivity {

    private static final OkHttpClient HTTP = new OkHttpClient();

    private static final String URL_GET =
            "https://wmc.ms.wits.ac.za/students/sgroup2715/getMessage.php";

    private static final String URL_SEND =
            "https://wmc.ms.wits.ac.za/students/sgroup2715/sendMessage.php";

    private static final long POLL_MS = 5_000L;

    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvStatus;

    private String requestId;
    private String senderEmail;
    private String senderName;

    private boolean isSending = false;

    private final Handler  pollHandler  = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            if (!isSending) fetchMessages();
            pollHandler.postDelayed(this, POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper_message);

        requestId = getIntent().getStringExtra("REQUEST_ID");
        String label = getIntent().getStringExtra("ORDER_LABEL");

        senderEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("userEmail", "");

        senderName = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("userName", "Unknown");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(label != null ? label : "Messages");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        messagesContainer = findViewById(R.id.messagesContainer);
        scrollView        = findViewById(R.id.scrollView);
        etMessage         = findViewById(R.id.etMessage);
        btnSend           = findViewById(R.id.btnSend);
        tvStatus          = findViewById(R.id.tvStatus);

        etMessage.setHintTextColor(0x88FFFFFF);
        btnSend.setOnClickListener(v -> sendMessage());


        pollHandler.post(pollRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacksAndMessages(null);
    }


    private void fetchMessages() {
        if (requestId == null || requestId.isEmpty()) return;

        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .build();

        Request request = new Request.Builder()
                .url(URL_GET)
                .post(body)
                .build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String json = response.body().string();
                Log.d("MESSAGES_JSON", json);

                runOnUiThread(() -> {
                    try {
                        if (!json.trim().startsWith("[")) {
                            return;
                        }
                        renderMessages(new JSONArray(json));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }


    private void renderMessages(JSONArray array) {
        messagesContainer.removeAllViews();

        if (array.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No messages yet.\nSend a message below 👇");
            empty.setTextColor(0x88FFFFFF);
            empty.setTextSize(14f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(32, 64, 32, 0);
            messagesContainer.addView(empty);
            return;
        }

        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject msg = array.getJSONObject(i);
                addMessageRow(
                        msg.optString("sender_name", "Unknown"),
                        msg.optString("message_text", ""),
                        msg.optString("sent_at", "")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }


    private void addMessageRow(String sender, String text, String time) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 16);
        row.setLayoutParams(rowParams);

        row.setBackgroundResource(R.drawable.card_bg);
        row.setPadding(28, 20, 28, 20);

        TextView tvSender = new TextView(this);
        tvSender.setText(sender);
        tvSender.setTextColor(0xFF1976D2);
        tvSender.setTextSize(12f);
        row.addView(tvSender);

        TextView tvText = new TextView(this);
        tvText.setText(text);
        tvText.setTextColor(0xFFFFFFFF);
        tvText.setTextSize(15f);
        tvText.setPadding(0, 6, 0, 0);
        row.addView(tvText);

        if (time != null && !time.trim().isEmpty()) {
            String displayTime = (time.length() >= 16) ? time.substring(0, 16) : time;
            TextView tvTime = new TextView(this);
            tvTime.setText(displayTime);
            tvTime.setTextColor(0x66FFFFFF);
            tvTime.setTextSize(10f);
            tvTime.setPadding(0, 8, 0, 0);
            row.addView(tvTime);
        }

        messagesContainer.addView(row);
    }


    private void sendMessage() {
        String text = etMessage.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Type a message first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSending) return;

        isSending = true;
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText("Sending...");
        tvStatus.setTextColor(0x88FFFFFF);

        RequestBody body = new FormBody.Builder()
                .add("request_id",   requestId)
                .add("sender_email", senderEmail)
                .add("sender_name",  senderName)
                .add("message",      text)
                .build();

        Request request = new Request.Builder()
                .url(URL_SEND)
                .post(body)
                .build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    reset();
                    tvStatus.setText("❌ Failed to send");
                    tvStatus.setTextColor(0xFFFF5252);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    reset();
                    if (response.isSuccessful()) {
                        etMessage.setText("");
                        tvStatus.setVisibility(View.GONE);
                        fetchMessages();
                    } else {
                        tvStatus.setText("❌ Server error");
                        tvStatus.setTextColor(0xFFFF5252);
                    }
                });
            }
        });
    }

    private void reset() {
        isSending = false;
        etMessage.setEnabled(true);
        btnSend.setEnabled(true);
    }
}
