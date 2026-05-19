package com.example.surrogateshopper;

import android.content.Intent;
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

    // Track destination data for the map
    private double destLat = 0.0;
    private double destLng = 0.0;
    private String destAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvItems = findViewById(R.id.tvItems);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        Button btnShopping = findViewById(R.id.btnShopping);
        Button btnDelivering = findViewById(R.id.btnOutForDelivery);

        orderId = getIntent().getStringExtra("ORDER_ID");
        String initialStatus = getIntent().getStringExtra("STATUS");

        if (initialStatus != null) {
            tvCurrentStatus.setText("Current Status: " + initialStatus);
        }

        if (orderId != null) {
            fetchBasket(orderId);
        }

        // Logic for Shopping
        btnShopping.setOnClickListener(v -> updateStatus(orderId, "Shopping"));

        // Logic for Out for Delivery -> This opens the Navigate Activity
        btnDelivering.setOnClickListener(v -> {
            updateStatus(orderId, "Out for Delivery");
        });
    }

    private void updateStatus(String requestId, String newStatus) {
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
                        tvCurrentStatus.setText("Current Status: " + newStatus);
                        Toast.makeText(OrderDetailsActivity.this, "Status: " + newStatus, Toast.LENGTH_SHORT).show();

                        // If they click Out for Delivery, take them to the Map
                        if (newStatus.equals("Out for Delivery")) {
                            Intent intent = new Intent(OrderDetailsActivity.this, navigate.class);
                            intent.putExtra("REQUEST_ID", orderId);
                            intent.putExtra("LATITUDE", destLat);
                            intent.putExtra("LONGITUDE", destLng);
                            intent.putExtra("ADDRESS", destAddress);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderDetailsActivity.this, "Update Failed", Toast.LENGTH_SHORT).show());
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
                        JSONObject jsonResponse = new JSONObject(data);

                        // Store coordinates and address for the Map
                        destAddress = jsonResponse.getString("request_address");
                        destLat = jsonResponse.getDouble("request_latitude");
                        destLng = jsonResponse.getDouble("request_longitude");

                        // Parse the Items array
                        JSONArray itemsArray = jsonResponse.getJSONArray("items");
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject item = itemsArray.getJSONObject(i);
                            sb.append("• ").append(item.getString("name"))
                                    .append(" (").append(item.getString("size")).append(")")
                                    .append(" x").append(item.getString("quantity"))
                                    .append("\n");
                        }

                        runOnUiThread(() -> {
                            tvItems.setText(sb.length() == 0 ? "No items in basket." : sb.toString());
                        });
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvItems.setText("Error loading details."));
            }
        });
    }
}