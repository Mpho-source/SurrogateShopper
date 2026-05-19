package com.example.surrogateshopper;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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

public class Chat extends AppCompatActivity {

    private static final OkHttpClient HTTP = new OkHttpClient();

    private static final String URL_GET  = "https://wmc.ms.wits.ac.za/students/sgroup2715/getMessage.php";
    private static final String URL_SEND = "https://wmc.ms.wits.ac.za/students/sgroup2715/sendMessage.php";
    private static final long   POLL_MS  = 5_000L;

    private LinearLayout messagesContainer;
    private ScrollView   scrollView;
    private EditText     etMessage;
    private ImageButton  btnSend;
    private TextView     tvSendStatus;

    private String  requestId, orderLabel, senderEmail, senderName;
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
        setContentView(R.layout.activity_chat);

        requestId  = getIntent().getStringExtra("REQUEST_ID");
        orderLabel = getIntent().getStringExtra("ORDER_LABEL");

        senderEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("userEmail", "");


        senderName = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("userName", "Unknown");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(orderLabel != null ? orderLabel : "Chat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        messagesContainer = findViewById(R.id.messagesContainer);
        scrollView        = findViewById(R.id.scrollView);
        etMessage         = findViewById(R.id.etMessage);
        btnSend           = findViewById(R.id.btnSend);
        tvSendStatus      = findViewById(R.id.tvSendStatus);

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
        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .build();
        Request req = new Request.Builder().url(URL_GET).post(body).build();

        HTTP.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {  }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String json = response.body().string();
                runOnUiThread(() -> {
                    try { renderMessages(new JSONArray(json)); }
                    catch (Exception e) { e.printStackTrace(); }
                });
            }
        });
    }



    private void renderMessages(JSONArray array) {
        messagesContainer.removeAllViews();

        if (array.length() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No messages yet — say something! 👋");
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
                String text  = msg.optString("message_text", "");


                String name  = msg.optString("sender_name", "Unknown");


                String email = msg.optString("sender_email", "");
                String time  = msg.optString("sent_at", "");

                boolean isMe  = name.equalsIgnoreCase(senderName) || (!email.isEmpty() && email.equalsIgnoreCase(senderEmail));
                addBubble(text, name, time, isMe, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }



    private void addBubble(String text, String name,
                           String time, boolean isMe, boolean confirmed) {

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(isMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapParams.setMargins(0, 4, 0, 4);
        wrapper.setLayoutParams(wrapParams);

        if (!isMe) {
            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextColor(0xBBFFFFFF);
            tvName.setTextSize(11f);
            tvName.setPadding(20, 0, 20, 3);
            wrapper.addView(tvName);
        }

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextColor(0xFFFFFFFF);
        bubble.setTextSize(15f);
        bubble.setLineSpacing(3f, 1f);
        bubble.setPadding(24, 14, 24, 14);
        bubble.setMaxWidth(1000);
        bubble.setBackgroundResource(isMe ? R.drawable.bubble_me : R.drawable.bubble_them);
        if (!confirmed) bubble.setAlpha(0.6f);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMargins(isMe ? 72 : 16, 0, isMe ? 16 : 72, 0);
        bubble.setLayoutParams(bubbleParams);
        wrapper.addView(bubble);

        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(isMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        metaParams.setMargins(isMe ? 0 : 20, 3, isMe ? 20 : 0, 8);
        metaRow.setLayoutParams(metaParams);

        if (!time.isEmpty()) {
            String displayTime = time.length() > 16 ? time.substring(11, 16) : time;
            TextView tvTime = new TextView(this);
            tvTime.setText(displayTime);
            tvTime.setTextColor(0x66FFFFFF);
            tvTime.setTextSize(10f);
            metaRow.addView(tvTime);
        }

        if (isMe) {
            TextView tvTick = new TextView(this);
            tvTick.setText(confirmed ? "  ✓✓" : "  ✓");
            tvTick.setTextColor(confirmed ? 0xFF4FC3F7 : 0x88FFFFFF);
            tvTick.setTextSize(11f);
            tvTick.setTypeface(null, Typeface.BOLD);
            metaRow.addView(tvTick);
        }

        wrapper.addView(metaRow);
        messagesContainer.addView(wrapper);
    }



    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() || isSending) return;

        isSending = true;
        etMessage.setText("");
        etMessage.setEnabled(false);
        btnSend.setEnabled(false);
        tvSendStatus.setText("Sending...");
        tvSendStatus.setTextColor(0x88FFFFFF);
        tvSendStatus.setVisibility(View.VISIBLE);

        addBubble(text, senderName, "",  true,  false);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));


        RequestBody body = new FormBody.Builder()
                .add("request_id",   requestId)
                .add("sender_email", senderEmail)
                .add("sender_name",  senderName)
                .add("message",      text)
                .build();
        Request req = new Request.Builder().url(URL_SEND).post(body).build();

        HTTP.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    resetSendState();
                    etMessage.setText(text);
                    showSendError("❌  Failed to send — tap send to retry");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    resetSendState();
                    if (response.isSuccessful()) {
                        tvSendStatus.setVisibility(View.GONE);
                        fetchMessages();
                    } else {
                        etMessage.setText(text);
                        showSendError("❌  Server error — try again");
                    }
                });
            }
        });
    }

    private void resetSendState() {
        isSending = false;
        etMessage.setEnabled(true);
        btnSend.setEnabled(true);
    }

    private void showSendError(String msg) {
        tvSendStatus.setText(msg);
        tvSendStatus.setTextColor(0xFFFF5252);
        tvSendStatus.setVisibility(View.VISIBLE);
    }
}
