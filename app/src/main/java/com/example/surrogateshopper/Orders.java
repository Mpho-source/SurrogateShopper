package com.example.surrogateshopper;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

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

public class Orders extends AppCompatActivity {

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final String URL_ORDERS = "https://wmc.ms.wits.ac.za/students/sgroup2715/getOrders.php";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private LinearLayout ordersContainer;
    private TextView textEmpty, headerTitle;
    private MaterialCardView basketCard;

    private String email = "";
    private String name = "";
    private String userId = "";
    private String mode = "shopper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_side);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        loadSession();
        setNavigationHeader(navigationView);
        setNavigationActions(navigationView);

        textEmpty = findViewById(R.id.textEmpty);
        headerTitle = findViewById(R.id.headerTitle);
        basketCard = findViewById(R.id.basketCard);
        ordersContainer = findViewById(R.id.ordersContainer);

        showLoading();
        fetchOrdersFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ordersContainer != null) fetchOrdersFromServer();
    }

    private void loadSession() {
        email = value(getIntent().getStringExtra("USER_EMAIL"));
        name = value(getIntent().getStringExtra("USER_NAME"));
        userId = value(getIntent().getStringExtra("USER_ID"));
        mode = value(getIntent().getStringExtra("ORDER_MODE"));

        if (email.isEmpty()) email = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userEmail", "");
        if (name.isEmpty()) name = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userName", "User");
        if (userId.isEmpty()) userId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");
        if (mode.isEmpty()) mode = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userRole", "shopper");
        if (!mode.equalsIgnoreCase("volunteer")) mode = "shopper";
    }

    private void setNavigationHeader(NavigationView navigationView) {
        View header = navigationView.getHeaderView(0);
        TextView nameView = header.findViewById(R.id.nav_user_name);
        TextView emailView = header.findViewById(R.id.nav_user_email);
        nameView.setText(name.isEmpty() ? "User" : name);
        emailView.setText(email.isEmpty() ? "No email found" : email);
    }

    private void setNavigationActions(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home || id == R.id.nav_dashboard) {
                finish();
            } else if (id == R.id.nav_order || id == R.id.nav_my_orders) {
                fetchOrdersFromServer();
            } else if (id == R.id.nav_messages) {
                Toast.makeText(this, "Open an order, then tap Message.", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, name + "\n" + email, Toast.LENGTH_LONG).show();
            } else if (id == R.id.nav_logout) {
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(Orders.this, MainActivity.class));
                finishAffinity();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showLoading() {
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText("Loading orders...");
        basketCard.setVisibility(View.GONE);
        headerTitle.setVisibility(View.GONE);
    }

    private void fetchOrdersFromServer() {
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("user_id", userId)
                .add("mode", mode)
                .build();

        Request request = new Request.Builder().url(URL_ORDERS).post(body).build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Could not load orders. Tap Refresh."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showError("Server error loading orders."));
                    return;
                }
                runOnUiThread(() -> {
                    try {
                        renderOrders(new JSONArray(jsonStr));
                    } catch (Exception e) {
                        showError("Could not read orders from server.");
                    }
                });
            }
        });
    }

    private void showError(String message) {
        textEmpty.setVisibility(View.GONE);
        headerTitle.setVisibility(View.VISIBLE);
        headerTitle.setText(mode.equalsIgnoreCase("volunteer") ? "Past Orders" : "Your Orders");
        basketCard.setVisibility(View.VISIBLE);
        ordersContainer.removeAllViews();
        addRefreshButton();
        addPlainText(message);
    }

    private void renderOrders(JSONArray array) {
        ordersContainer.removeAllViews();
        textEmpty.setVisibility(View.GONE);
        headerTitle.setVisibility(View.VISIBLE);
        headerTitle.setText(mode.equalsIgnoreCase("volunteer") ? "Past Orders" : "Your Orders");
        basketCard.setVisibility(View.VISIBLE);
        addRefreshButton();

        if (array.length() == 0) {
            addPlainText(mode.equalsIgnoreCase("volunteer") ? "You have no accepted or completed orders yet." : "You have no order history yet.");
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject order = array.getJSONObject(i);
                addOrderBlock(order, i > 0);
            } catch (Exception ignored) {}
        }
    }

    private void addOrderBlock(JSONObject order, boolean divider) throws Exception {
        if (divider) addDivider();

        String requestId = value(order.optString("request_id", ""));
        String status = value(order.optString("status", "Unknown"));
        String createdAt = value(order.optString("created_at", ""));
        JSONArray items = order.optJSONArray("items");
        if (items == null) items = new JSONArray();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setPadding(0, 8, 0, 4);

        TextView tvName = new TextView(this);
        tvName.setText("Order #" + requestId);
        tvName.setTextColor(0xFFFFFFFF);
        tvName.setTextSize(17f);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvStatus = new TextView(this);
        tvStatus.setText(status.toUpperCase());
        tvStatus.setTextSize(12f);
        tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setTextColor(statusColor(status));

        titleRow.addView(tvName);
        titleRow.addView(tvStatus);
        ordersContainer.addView(titleRow);

        if (!createdAt.isEmpty()) {
            TextView tvTime = new TextView(this);
            tvTime.setText(createdAt.length() > 16 ? createdAt.substring(0, 16) : createdAt);
            tvTime.setTextColor(0xDDFFFFFF);
            tvTime.setTextSize(12f);
            tvTime.setPadding(0, 0, 0, 6);
            ordersContainer.addView(tvTime);
        }

        if (items.length() == 0) {
            addPlainText("No items found for this order.");
        } else {
            for (int j = 0; j < items.length(); j++) {
                JSONObject item = items.getJSONObject(j);
                TextView tvItem = new TextView(this);
                String itemName = value(item.optString("name", "Item"));
                String size = value(item.optString("size", ""));
                String quantity = value(item.optString("quantity", "1"));
                tvItem.setText((j + 1) + ". " + (itemName.isEmpty() ? "Item" : itemName) + (size.isEmpty() ? "" : " (" + size + ")") + " x" + (quantity.isEmpty() ? "1" : quantity));
                tvItem.setTextColor(0xEEFFFFFF);
                tvItem.setTextSize(14f);
                tvItem.setPadding(8, 2, 0, 2);
                ordersContainer.addView(tvItem);
            }
        }

        addActions(requestId, status);
    }

    private void addActions(String requestId, String status) {
        Button view = makeBlueButton("View details");
        view.setOnClickListener(v -> {
            Intent intent = new Intent(Orders.this, OrderDetailsActivity.class);
            intent.putExtra("ORDER_ID", requestId);
            intent.putExtra("STATUS", status);
            intent.putExtra("ROLE", mode);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });
        ordersContainer.addView(view);

        Button message = makeBlueButton(mode.equalsIgnoreCase("volunteer") ? "Message Shopper" : "Message Volunteer");
        message.setOnClickListener(v -> {
            Intent intent = new Intent(Orders.this, ShopperMessage.class);
            intent.putExtra("REQUEST_ID", requestId);
            intent.putExtra("ORDER_LABEL", "Order #" + requestId);
            startActivity(intent);
        });
        ordersContainer.addView(message);
    }

    private void addRefreshButton() {
        Button refresh = makeBlueButton("Refresh");
        refresh.setOnClickListener(v -> fetchOrdersFromServer());
        ordersContainer.addView(refresh);
    }

    private Button makeBlueButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(0xFF1976D2);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private void addPlainText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xEEFFFFFF);
        tv.setTextSize(15f);
        tv.setPadding(8, 12, 8, 8);
        ordersContainer.addView(tv);
    }

    private void addDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(0x66FFFFFF);
        ordersContainer.addView(divider);
    }

    private int statusColor(String status) {
        if (status.equalsIgnoreCase("Completed")) return 0xFFFFFFFF;
        if (status.equalsIgnoreCase("Pending")) return 0xFFFFFFFF;
        return 0xFFE3F2FD;
    }

    private String value(String value) {
        if (value == null) return "";
        String v = value.trim();
        return v.equalsIgnoreCase("null") ? "" : v;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
