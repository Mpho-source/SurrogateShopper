package com.example.surrogateshopper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
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

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final String URL_REQUESTS = "https://wmc.ms.wits.ac.za/students/sgroup2715/getRequests.php";
    private static final String URL_ACCEPT = "https://wmc.ms.wits.ac.za/students/sgroup2715/acceptRequest.php";

    private final List<Order> orders = new ArrayList<>();
    private LinearLayout ordersListContainer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private String currentVolId = "";
    private String currentEmail = "";
    private String currentName = "Volunteer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        loadSession();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_side);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        setupHeader();
        setupNavigation();

        ordersListContainer = findViewById(R.id.ordersListContainer);
        fetchRequestsFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ordersListContainer != null) fetchRequestsFromServer();
    }

    private void loadSession() {
        currentVolId = value(getIntent().getStringExtra("USER_ID"));
        currentEmail = value(getIntent().getStringExtra("USER_EMAIL"));
        currentName = value(getIntent().getStringExtra("USER_NAME"));

        if (currentVolId.isEmpty()) currentVolId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");
        if (currentEmail.isEmpty()) currentEmail = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userEmail", "");
        if (currentName.isEmpty()) currentName = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userName", "Volunteer");

        getSharedPreferences("UserSession", MODE_PRIVATE).edit().putString("userRole", "volunteer").apply();
    }

    private void setupHeader() {
        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.nav_user_name);
        TextView email = header.findViewById(R.id.nav_user_email);
        name.setText(currentName.isEmpty() ? "Volunteer" : currentName);
        email.setText(currentEmail.isEmpty() ? "No email found" : currentEmail);
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                fetchRequestsFromServer();
            } else if (id == R.id.nav_my_orders) {
                Intent intent = new Intent(Volunteer.this, Orders.class);
                intent.putExtra("ORDER_MODE", "volunteer");
                intent.putExtra("USER_ID", currentVolId);
                intent.putExtra("USER_EMAIL", currentEmail);
                intent.putExtra("USER_NAME", currentName);
                startActivity(intent);
            } else if (id == R.id.nav_messages) {
                Toast.makeText(this, "Open an accepted order, then tap Message.", Toast.LENGTH_LONG).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, currentName + "\n" + currentEmail, Toast.LENGTH_LONG).show();
            } else if (id == R.id.nav_logout) {
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(Volunteer.this, MainActivity.class));
                finishAffinity();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void fetchRequestsFromServer() {
        showLoading("Loading available requests...");
        Request request = new Request.Builder().url(URL_REQUESTS).get().build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showLoading("Could not load requests. Tap Refresh."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showLoading("Server error loading requests."));
                    return;
                }

                try {
                    JSONArray array = new JSONArray(jsonData);
                    List<Order> freshOrders = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        freshOrders.add(new Order(
                                obj.optString("request_id"),
                                "Order #" + obj.optString("request_id"),
                                obj.optString("shopper_name", "Shopper"),
                                obj.optString("status", "Pending"),
                                new ArrayList<>()
                        ));
                    }
                    runOnUiThread(() -> {
                        orders.clear();
                        orders.addAll(freshOrders);
                        renderOrders();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> showLoading("Bad server response. Check getRequests.php."));
                }
            }
        });
    }

    private void showLoading(String message) {
        ordersListContainer.removeAllViews();
        ordersListContainer.addView(makeRefreshButton());
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16f);
        tv.setPadding(8, 20, 8, 8);
        ordersListContainer.addView(tv);
    }

    private Button makeRefreshButton() {
        Button refresh = makeBlueButton("Refresh");
        refresh.setOnClickListener(v -> fetchRequestsFromServer());
        return refresh;
    }

    private void renderOrders() {
        ordersListContainer.removeAllViews();
        ordersListContainer.addView(makeRefreshButton());

        if (orders.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No available requests right now.");
            empty.setTextColor(Color.WHITE);
            empty.setTextSize(16f);
            empty.setPadding(8, 20, 8, 8);
            ordersListContainer.addView(empty);
            return;
        }

        for (Order order : new ArrayList<>(orders)) {
            View card = getLayoutInflater().inflate(R.layout.item_order_card, ordersListContainer, false);
            TextView tvBasket = card.findViewById(R.id.tvBasketName);
            TextView tvShopper = card.findViewById(R.id.tvShopperName);
            TextView tvStatus = card.findViewById(R.id.tvStatus);
            Button btnAccept = card.findViewById(R.id.btnAcceptOrder);

            tvBasket.setText("Order #" + order.id);
            tvShopper.setText("Shopper: " + order.shopperName);
            tvStatus.setText(order.status);
            tvStatus.setTextColor(Color.rgb(25, 118, 210));

            btnAccept.setText("Accept");
            btnAccept.setEnabled(true);
            btnAccept.setTextColor(Color.WHITE);
            btnAccept.setBackgroundColor(Color.rgb(25, 118, 210));
            btnAccept.setOnClickListener(v -> acceptOrder(order, btnAccept, tvStatus));

            ordersListContainer.addView(card);
        }
    }

    private void acceptOrder(Order order, Button button, TextView statusView) {
        if (currentVolId.isEmpty()) {
            Toast.makeText(this, "Login problem: missing volunteer ID.", Toast.LENGTH_LONG).show();
            return;
        }

        button.setEnabled(false);
        button.setText("Accepting...");

        RequestBody body = new FormBody.Builder()
                .add("request_id", order.id)
                .add("volunteer_id", currentVolId)
                .add("volunteer_email", currentEmail)
                .build();

        Request request = new Request.Builder().url(URL_ACCEPT).post(body).build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    button.setEnabled(true);
                    button.setText("Accept");
                    Toast.makeText(Volunteer.this, "Network error accepting order.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() == null ? "" : response.body().string().trim();
                runOnUiThread(() -> {
                    if (response.isSuccessful() && result.toUpperCase().contains("SUCCESS")) {
                        order.status = "Accepted";
                        statusView.setText("Accepted");
                        button.setText("View");
                        button.setEnabled(true);
                        button.setOnClickListener(v -> openOrderDetails(order));
                        Toast.makeText(Volunteer.this, "Order accepted.", Toast.LENGTH_SHORT).show();
                        fetchRequestsFromServer();
                    } else {
                        button.setEnabled(true);
                        button.setText("Accept");
                        Toast.makeText(Volunteer.this, result.isEmpty() ? "Order already taken." : result, Toast.LENGTH_LONG).show();
                        fetchRequestsFromServer();
                    }
                });
            }
        });
    }

    private void openOrderDetails(Order order) {
        Intent intent = new Intent(Volunteer.this, OrderDetailsActivity.class);
        intent.putExtra("ORDER_ID", order.id);
        intent.putExtra("SHOPPER_NAME", order.shopperName);
        intent.putExtra("STATUS", order.status);
        intent.putExtra("ROLE", "volunteer");
        intent.putExtra("USER_ID", currentVolId);
        intent.putExtra("USER_EMAIL", currentEmail);
        startActivity(intent);
    }

    private Button makeBlueButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(25, 118, 210));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);
        button.setLayoutParams(params);
        return button;
    }

    private String value(String s) {
        if (s == null) return "";
        String v = s.trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }
}
