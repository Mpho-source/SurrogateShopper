package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class OrderDetailsActivity extends AppCompatActivity {

    private static final String URL_STATUS = "https://wmc.ms.wits.ac.za/students/sgroup2715/updateStatus.php";
    private static final String URL_DETAILS = "https://wmc.ms.wits.ac.za/students/sgroup2715/getBasketDetails.php";

    private TextView tvItems;
    private TextView tvCurrentStatus;
    private Button btnShopping;
    private Button btnDelivering;

    private String orderId = "";
    private double destLat = 0.0;
    private double destLng = 0.0;
    private String destAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvItems = findViewById(R.id.tvItems);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        btnShopping = findViewById(R.id.btnShopping);
        btnDelivering = findViewById(R.id.btnOutForDelivery);

        orderId = getIntent().getStringExtra("ORDER_ID");
        String initialStatus = getIntent().getStringExtra("STATUS");

        if (orderId == null || orderId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderId = orderId.trim();

        if (initialStatus == null || initialStatus.trim().isEmpty()) {
            initialStatus = "Accepted";
        }

        tvCurrentStatus.setText("Current Status: " + initialStatus);
        updateButtonsForStatus(initialStatus);

        String passedItems = getIntent().getStringExtra("ITEMS_JSON");
        if (passedItems != null && !passedItems.trim().isEmpty()) {
            renderItemsFromJson(passedItems);
        } else {
            tvItems.setText("Loading items...");
        }

        fetchBasket(orderId);

        btnShopping.setOnClickListener(v -> updateStatus(orderId, "Shopping"));
        btnDelivering.setOnClickListener(v -> updateStatus(orderId, "Out for Delivery"));
    }

    private void updateStatus(String requestId, String newStatus) {
        btnShopping.setEnabled(false);
        btnDelivering.setEnabled(false);

        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .add("status", newStatus)
                .build();

        Request request = new Request.Builder().url(URL_STATUS).post(body).build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    if (response.isSuccessful() && result.toLowerCase().contains("success")) {
                        tvCurrentStatus.setText("Current Status: " + newStatus);
                        updateButtonsForStatus(newStatus);

                        if (newStatus.equals("Out for Delivery")) {
                            Intent intent = new Intent(OrderDetailsActivity.this, navigate.class);
                            intent.putExtra("REQUEST_ID", orderId);
                            intent.putExtra("LATITUDE", destLat);
                            intent.putExtra("LONGITUDE", destLng);
                            intent.putExtra("ADDRESS", destAddress);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(OrderDetailsActivity.this, "Status update failed.", Toast.LENGTH_SHORT).show();
                        btnShopping.setEnabled(true);
                        btnDelivering.setEnabled(true);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                    btnShopping.setEnabled(true);
                    btnDelivering.setEnabled(true);
                });
            }
        });
    }

    private void updateButtonsForStatus(String status) {
        if (status == null) status = "";

        boolean completed = status.equalsIgnoreCase("Completed");
        boolean outForDelivery = status.equalsIgnoreCase("Out for Delivery");

        btnShopping.setEnabled(!completed);
        btnDelivering.setEnabled(!completed);

        if (completed) {
            btnShopping.setVisibility(Button.GONE);
            btnDelivering.setVisibility(Button.GONE);
        } else {
            btnShopping.setVisibility(Button.VISIBLE);
            btnDelivering.setVisibility(Button.VISIBLE);
        }

        if (outForDelivery) {
            btnDelivering.setText("NAVIGATE");
        } else {
            btnDelivering.setText("OUT FOR DELIVERY");
        }
    }

    private void fetchBasket(String requestId) {
        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .build();

        Request request = new Request.Builder().url(URL_DETAILS).post(body).build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return;
                }

                try {
                    JSONObject jsonResponse = new JSONObject(data);

                    destAddress = jsonResponse.optString("request_address", "");
                    destLat = parseDouble(jsonResponse.optString("request_latitude", "0"));
                    destLng = parseDouble(jsonResponse.optString("request_longitude", "0"));

                    JSONArray itemsArray = jsonResponse.optJSONArray("items");
                    if (itemsArray != null && itemsArray.length() > 0) {
                        runOnUiThread(() -> renderItems(itemsArray));
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    private void renderItemsFromJson(String json) {
        try {
            renderItems(new JSONArray(json));
        } catch (Exception e) {
            tvItems.setText("No items found for this order.");
        }
    }

    private void renderItems(JSONArray itemsArray) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < itemsArray.length(); i++) {
            try {
                JSONObject item = itemsArray.getJSONObject(i);
                String name = clean(item.optString("name", "Item"));
                String size = clean(item.optString("size", ""));
                String quantity = clean(item.optString("quantity", "1"));

                sb.append("• ").append(name);
                if (!size.isEmpty()) {
                    sb.append(" (").append(size).append(")");
                }
                sb.append(" ×").append(quantity).append("\n");
            } catch (Exception ignored) {
            }
        }

        tvItems.setText(sb.length() == 0 ? "No items found for this order." : sb.toString());
    }

    private double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return 0.0;
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String clean(String value) {
        if (value == null) return "";
        String v = value.trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }
}
