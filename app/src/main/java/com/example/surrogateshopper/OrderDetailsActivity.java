package com.example.surrogateshopper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView tvItems, tvCurrentStatus;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // 1. Initialize UI Elements
        tvItems = findViewById(R.id.tvItems);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        Button btnShopping = findViewById(R.id.btnShopping);
        Button btnDelivering = findViewById(R.id.btnDelivering);
        Button btnComplete = findViewById(R.id.btnComplete);

        // 2. Get Data from Intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        String initialStatus = getIntent().getStringExtra("STATUS");

        if (initialStatus != null) {
            tvCurrentStatus.setText("Current Status: " + initialStatus);
        }

        if (orderId != null) {
            fetchBasket(orderId);
        }

        // 3. Button Click Listeners
        btnShopping.setOnClickListener(v -> updateStatus(orderId, "Shopping", tvCurrentStatus));
        btnDelivering.setOnClickListener(v -> updateStatus(orderId, "Delivering", tvCurrentStatus));
        btnComplete.setOnClickListener(v -> updateStatus(orderId, "Completed", tvCurrentStatus));
    }

    private void updateStatus(String requestId, String newStatus, TextView statusLabel) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .add("status", newStatus)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/updateStatus.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        statusLabel.setText("Current Status: " + newStatus);
                        Toast.makeText(OrderDetailsActivity.this, "Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();

                        if (newStatus.equals("Completed")) {
                            finish(); // Closes activity and goes back to the list
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderDetailsActivity.this, "Server Update Failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchBasket(String requestId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/getBasketDetails.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.getJSONObject(i);
                            sb.append("• ").append(item.getString("name"))
                                    .append(" (").append(item.getString("size")).append(")")
                                    .append(" x").append(item.getString("quantity"))
                                    .append("\n");
                        }

                        runOnUiThread(() -> {
                            if (jsonArray.length() == 0) {
                                tvItems.setText("Basket is empty.");
                            } else {
                                tvItems.setText(sb.toString());
                            }
                        });
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvItems.setText("Error loading items."));
            }
        });
    }
}