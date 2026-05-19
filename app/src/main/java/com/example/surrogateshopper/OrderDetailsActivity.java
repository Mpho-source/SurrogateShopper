package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final String URL_DETAILS = "https://wmc.ms.wits.ac.za/students/sgroup2715/getBasketDetails.php";
    private static final String URL_STATUS = "https://wmc.ms.wits.ac.za/students/sgroup2715/updateStatus.php";

    private TextView tvItems, tvCurrentStatus, tvRoleNotice;
    private Button btnShopping, btnDelivering, btnCompleted;
    private String orderId = "";
    private String currentStatus = "";
    private String role = "shopper";
    private String userId = "";
    private String userEmail = "";
    private double destLat = 0.0;
    private double destLng = 0.0;
    private String destAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvItems = findViewById(R.id.tvItems);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvRoleNotice = findViewById(R.id.tvRoleNotice);
        btnShopping = findViewById(R.id.btnShopping);
        btnDelivering = findViewById(R.id.btnOutForDelivery);
        btnCompleted = findViewById(R.id.btnCompleted);

        btnCompleted.setVisibility(View.INVISIBLE);


        orderId = value(getIntent().getStringExtra("ORDER_ID"));
        currentStatus = value(getIntent().getStringExtra("STATUS"));
        role = value(getIntent().getStringExtra("ROLE"));
        userId = value(getIntent().getStringExtra("USER_ID"));
        userEmail = value(getIntent().getStringExtra("USER_EMAIL"));

        if (role.isEmpty()) {
            role = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userRole", "shopper");
        }
        if (userId.isEmpty()) {
            userId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");
        }
        if (userEmail.isEmpty()) {
            userEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userEmail", "");
        }

        if (orderId.isEmpty()) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentStatus.isEmpty()) currentStatus = "Pending";
        tvItems.setText("Loading items...");
        applyStatusUi();
        fetchBasket(orderId);

        btnShopping.setOnClickListener(v -> updateStatus("Shopping"));
        btnDelivering.setOnClickListener(v -> updateStatus("Out for Delivery"));
        btnCompleted.setOnClickListener(v -> updateStatus("Completed"));
    }

    private void applyStatusUi() {
        tvCurrentStatus.setText("Current Status: " + currentStatus);
        boolean volunteer = role.equalsIgnoreCase("volunteer");

        if (!volunteer) {
            btnShopping.setVisibility(View.GONE);
            btnDelivering.setVisibility(View.GONE);
            btnCompleted.setVisibility(View.GONE);
            tvRoleNotice.setVisibility(View.VISIBLE);
            tvRoleNotice.setText("You can view this order. Only the assigned volunteer can update delivery progress.");
            return;
        }

        tvRoleNotice.setVisibility(View.GONE);
        btnShopping.setVisibility(View.VISIBLE);
        btnDelivering.setVisibility(View.VISIBLE);
        btnCompleted.setVisibility(View.INVISIBLE);

        boolean accepted = currentStatus.equalsIgnoreCase("Accepted");
        boolean shopping = currentStatus.equalsIgnoreCase("Shopping");
        boolean delivering = currentStatus.equalsIgnoreCase("Out for Delivery") || currentStatus.equalsIgnoreCase("Arrived");
        boolean completed = currentStatus.equalsIgnoreCase("Completed");

        btnShopping.setEnabled(accepted);
        btnDelivering.setEnabled(shopping);
        btnCompleted.setEnabled(delivering);

        if (completed) {
            btnShopping.setVisibility(View.GONE);
            btnDelivering.setVisibility(View.GONE);
            btnCompleted.setVisibility(View.GONE);
            tvRoleNotice.setVisibility(View.VISIBLE);
            tvRoleNotice.setText("This order is completed.");
        }
    }

    private void updateStatus(String newStatus) {
        if (!role.equalsIgnoreCase("volunteer")) {
            Toast.makeText(this, "Only volunteers can update orders", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newStatus.equals("Out for Delivery") && !currentStatus.equalsIgnoreCase("Shopping")) {
            Toast.makeText(this, "Mark as shopping first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newStatus.equals("Completed") && !(currentStatus.equalsIgnoreCase("Out for Delivery") || currentStatus.equalsIgnoreCase("Arrived"))) {
            Toast.makeText(this, "Mark as out for delivery first", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);

        RequestBody body = new FormBody.Builder()
                .add("request_id", orderId)
                .add("status", newStatus)
                .add("volunteer_id", userId)
                .add("volunteer_email", userEmail)
                .build();

        Request request = new Request.Builder().url(URL_STATUS).post(body).build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderDetailsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    applyStatusUi();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() == null ? "" : response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful() && result.toLowerCase().contains("success")) {
                        currentStatus = newStatus;
                        Toast.makeText(OrderDetailsActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                        applyStatusUi();
                        if (newStatus.equals("Out for Delivery")) openNavigation();
                    } else {
                        Toast.makeText(OrderDetailsActivity.this, "Status update rejected", Toast.LENGTH_SHORT).show();
                        applyStatusUi();
                    }
                });
            }
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        btnShopping.setEnabled(enabled);
        btnDelivering.setEnabled(enabled);
        btnCompleted.setEnabled(enabled);
    }

    private void openNavigation() {
        Intent intent = new Intent(OrderDetailsActivity.this, navigate.class);
        intent.putExtra("REQUEST_ID", orderId);
        intent.putExtra("LATITUDE", destLat);
        intent.putExtra("LONGITUDE", destLng);
        intent.putExtra("ADDRESS", destAddress);
        startActivity(intent);
    }

    private void fetchBasket(String requestId) {
        RequestBody body = new FormBody.Builder().add("request_id", requestId).build();
        Request request = new Request.Builder().url(URL_DETAILS).post(body).build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvItems.setText("Could not load order items."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> tvItems.setText("Could not load order items."));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(data);
                    destAddress = json.optString("request_address", "");
                    destLat = parseDouble(json.optString("request_latitude", "0"));
                    destLng = parseDouble(json.optString("request_longitude", "0"));
                    JSONArray items = json.optJSONArray("items");
                    String text = formatItems(items);
                    runOnUiThread(() -> tvItems.setText(text));
                } catch (Exception e) {
                    runOnUiThread(() -> tvItems.setText("Could not read order items."));
                }
            }
        });
    }

    private String formatItems(JSONArray items) throws Exception {
        if (items == null || items.length() == 0) return "No items found for this order.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String name = value(item.optString("name", "Item"));
            String size = value(item.optString("size", ""));
            String quantity = value(item.optString("quantity", "1"));
            sb.append("• ").append(name.isEmpty() ? "Item" : name);
            if (!size.isEmpty()) sb.append(" (").append(size).append(")");
            sb.append(" x").append(quantity.isEmpty() ? "1" : quantity).append("\n");
        }
        return sb.toString();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value == null ? "0" : value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String value(String s) {
        if (s == null) return "";
        String v = s.trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }
}
