package com.example.surrogateshopper;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private TextView textEmpty;
    private TextView headerTitle;
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
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        email = getValue("USER_EMAIL", "userEmail");
        name = getValue("USER_NAME", "userName");
        userId = getValue("USER_ID", "userId");

        mode = getIntent().getStringExtra("ORDER_MODE");
        if (mode == null || mode.trim().isEmpty()) {
            mode = "shopper";
        }

        setNavigationHeader(navigationView);
        setNavigationActions(navigationView);

        textEmpty = findViewById(R.id.textEmpty);
        headerTitle = findViewById(R.id.headerTitle);
        basketCard = findViewById(R.id.basketCard);
        ordersContainer = findViewById(R.id.ordersContainer);

        TextView tvOrderBasket = findViewById(R.id.tvOrderBasket);
        TextView tvOrderStatus = findViewById(R.id.tvOrderStatus);
        if (mode.equalsIgnoreCase("volunteer")) {
            tvOrderBasket.setText("Active and Past Orders");
        } else {
            tvOrderBasket.setText("Order History");
        }
        tvOrderStatus.setText("");

        showLoading();
        fetchOrdersFromServer();
    }

    private String getValue(String intentKey, String prefKey) {
        String value = getIntent().getStringExtra(intentKey);
        if (value == null || value.trim().isEmpty()) {
            value = getSharedPreferences("UserSession", MODE_PRIVATE).getString(prefKey, "");
        }
        return value == null ? "" : value.trim();
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

            if (id == R.id.nav_home) {
                finish();
            } else if (id == R.id.nav_order) {
                fetchOrdersFromServer();
            } else if (id == R.id.nav_profile) {
                textEmpty.setVisibility(View.VISIBLE);
                textEmpty.setText((name.isEmpty() ? "User" : name) + "\n" + email);
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
        headerTitle.setVisibility(View.GONE);
        basketCard.setVisibility(View.GONE);
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
                runOnUiThread(() -> showEmpty("Could not load orders. Check your connection."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonStr = response.body() == null ? "" : response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showEmpty("Server error loading orders."));
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        renderOrders(new JSONArray(jsonStr));
                    } catch (Exception e) {
                        showEmpty("Could not read orders data.");
                    }
                });
            }
        });
    }

    private void renderOrders(JSONArray array) {
        ordersContainer.removeAllViews();

        if (array.length() == 0) {
            showEmpty(mode.equalsIgnoreCase("volunteer") ? "No active or past orders yet." : "You have no orders yet.");
            return;
        }

        textEmpty.setVisibility(View.GONE);
        headerTitle.setVisibility(View.VISIBLE);
        headerTitle.setText(mode.equalsIgnoreCase("volunteer") ? "My Orders" : "Order History");
        basketCard.setVisibility(View.VISIBLE);

        addRefreshButton();

        boolean hasActive = false;
        boolean hasPast = false;

        if (mode.equalsIgnoreCase("volunteer")) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject order = array.getJSONObject(i);
                    if (!isCompleted(order.optString("status", ""))) {
                        if (!hasActive) {
                            addSectionTitle("Active Orders");
                            hasActive = true;
                        }
                        addOrder(order);
                    }
                } catch (Exception ignored) {
                }
            }

            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject order = array.getJSONObject(i);
                    if (isCompleted(order.optString("status", ""))) {
                        if (!hasPast) {
                            addSectionTitle("Past Orders");
                            hasPast = true;
                        }
                        addOrder(order);
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            for (int i = 0; i < array.length(); i++) {
                try {
                    addOrder(array.getJSONObject(i));
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void addOrder(JSONObject order) {
        String requestId = clean(order.optString("request_id", ""));
        String status = clean(order.optString("status", "Unknown"));
        String createdAt = clean(order.optString("created_at", ""));
        JSONArray items = order.optJSONArray("items");
        if (items == null) items = new JSONArray();

        addDivider();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setPadding(0, 8, 0, 4);

        TextView tvName = new TextView(this);
        tvName.setText("📦  Order #" + requestId);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(15f);
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
            tvTime.setTextColor(0xBBFFFFFF);
            tvTime.setTextSize(12f);
            tvTime.setPadding(0, 0, 0, 6);
            ordersContainer.addView(tvTime);
        }

        if (items.length() == 0) {
            TextView tvNoItems = new TextView(this);
            tvNoItems.setText("  No items found for this order.");
            tvNoItems.setTextColor(0xCCFFFFFF);
            tvNoItems.setTextSize(13f);
            ordersContainer.addView(tvNoItems);
        } else {
            for (int j = 0; j < items.length(); j++) {
                try {
                    JSONObject item = items.getJSONObject(j);
                    TextView tvItem = new TextView(this);
                    String itemName = clean(item.optString("name", "Item"));
                    String size = clean(item.optString("size", ""));
                    String quantity = clean(item.optString("quantity", "1"));

                    String text = "  " + (j + 1) + ".  " + itemName;
                    if (!size.isEmpty()) text += " (" + size + ")";
                    text += "  ×" + quantity;

                    tvItem.setText(text);
                    tvItem.setTextColor(0xCCFFFFFF);
                    tvItem.setTextSize(13f);
                    tvItem.setPadding(8, 2, 0, 2);
                    ordersContainer.addView(tvItem);
                } catch (Exception ignored) {
                }
            }
        }

        addViewButton(requestId, status, items.toString());
    }

    private void addRefreshButton() {
        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setAllCaps(false);
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundColor(Color.rgb(33, 150, 243));
        refresh.setOnClickListener(v -> fetchOrdersFromServer());
        ordersContainer.addView(refresh);
    }

    private void addSectionTitle(String title) {
        TextView section = new TextView(this);
        section.setText(title);
        section.setTextColor(Color.WHITE);
        section.setTextSize(20f);
        section.setTypeface(null, Typeface.BOLD);
        section.setPadding(0, 18, 0, 8);
        ordersContainer.addView(section);
    }

    private void addViewButton(String requestId, String status, String itemsJson) {
        Button btnView = new Button(this);
        btnView.setText("View Details");
        btnView.setAllCaps(false);
        btnView.setTextColor(Color.WHITE);
        btnView.setBackgroundColor(Color.rgb(33, 150, 243));
        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(Orders.this, OrderDetailsActivity.class);
            intent.putExtra("ORDER_ID", requestId);
            intent.putExtra("STATUS", status);
            intent.putExtra("ITEMS_JSON", itemsJson);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_EMAIL", email);
            intent.putExtra("USER_NAME", name);
            startActivity(intent);
        });
        ordersContainer.addView(btnView);

        Button btnMessage = new Button(this);
        btnMessage.setText(mode.equalsIgnoreCase("volunteer") ? "Message Shopper" : "Message Volunteer");
        btnMessage.setAllCaps(false);
        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Orders.this, ShopperMessage.class);
            intent.putExtra("REQUEST_ID", requestId);
            intent.putExtra("ORDER_LABEL", "Order #" + requestId);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
        });
        ordersContainer.addView(btnMessage);
    }

    private void addDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 12, 0, 12);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(0x44FFFFFF);
        ordersContainer.addView(divider);
    }

    private void showEmpty(String message) {
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
        headerTitle.setVisibility(View.GONE);
        basketCard.setVisibility(View.GONE);
    }

    private boolean isCompleted(String status) {
        return clean(status).equalsIgnoreCase("Completed");
    }

    private int statusColor(String status) {
        if (status.equalsIgnoreCase("Completed")) return Color.rgb(76, 175, 80);
        if (status.equalsIgnoreCase("Pending")) return Color.rgb(255, 193, 7);
        return Color.rgb(33, 150, 243);
    }

    private String clean(String value) {
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
