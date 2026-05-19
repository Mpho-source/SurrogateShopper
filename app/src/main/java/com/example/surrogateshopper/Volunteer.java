package com.example.surrogateshopper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Volunteer extends AppCompatActivity {

    public static List<Order> orders = new ArrayList<>();
    private LinearLayout ordersListContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);


        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This links the sidebar
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer_layout);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawer, toolbar, android.R.string.ok, android.R.string.cancel);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Force the hamburger icon to be white
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        ordersListContainer = findViewById(R.id.ordersListContainer);
        fetchRequestsFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRequestsFromServer(); // Refresh list every time they come back to this screen
    }

    private void fetchRequestsFromServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/getRequests.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONArray array = new JSONArray(jsonData);
                        orders.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            orders.add(new Order(
                                    obj.getString("request_id"),
                                    "Basket #" + obj.getString("request_id"),
                                    obj.getString("shopper_name"),
                                    obj.getString("status"),
                                    new ArrayList<>()
                            ));
                        }
                        runOnUiThread(() -> renderOrders());
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Volunteer.this, "Server Error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void renderOrders() {
        ordersListContainer.removeAllViews();

        for (Order order : orders) {
            View card = getLayoutInflater().inflate(R.layout.item_order_card, ordersListContainer, false);

            TextView tvBasket = card.findViewById(R.id.tvBasketName);
            TextView tvShopper = card.findViewById(R.id.tvShopperName);
            TextView tvStatus = card.findViewById(R.id.tvStatus);
            Button btnAccept = card.findViewById(R.id.btnAcceptOrder);

            tvBasket.setText(order.basketName);
            tvShopper.setText(order.shopperName);
            tvStatus.setText(order.status);

            // Click card to see items
            card.setOnClickListener(v -> {
                Intent intent = new Intent(Volunteer.this, OrderDetailsActivity.class);
                intent.putExtra("ORDER_ID", order.id);
                intent.putExtra("SHOPPER_NAME", order.shopperName);
                intent.putExtra("STATUS", order.status);
                startActivity(intent);
            });

            // Logic to handle "Accepted" state visually
            if (order.status.equalsIgnoreCase("Accepted")) {
                btnAccept.setText("LOCKED");
                btnAccept.setEnabled(false);
                btnAccept.setBackgroundColor(Color.GRAY);
            } else {
                btnAccept.setOnClickListener(v -> {
                    updateStatusOnServer(order.id, "Accepted", order);
                });
            }

            ordersListContainer.addView(card);
        }
    }

    private void updateStatusOnServer(String requestId, String newStatus, Order order) {
        OkHttpClient client = new OkHttpClient();

        // Get the ID of the logged-in Volunteer (Change "VOL_ID" to whatever key you used in Intent)
        String currentVolId = getIntent().getStringExtra("USER_ID");

        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .add("status", newStatus)
                .add("volunteer_id", currentVolId) // CRITICAL: This links the volunteer to the order
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/updateStatus.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Volunteer.this, "Network Error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // This logic clears the list so the volunteer only sees their 1 active task
                        orders.clear();
                        order.status = "Accepted";
                        orders.add(order);
                        renderOrders();
                        Toast.makeText(Volunteer.this, "Order Accepted!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}