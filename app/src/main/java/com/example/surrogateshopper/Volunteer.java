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

    private LinearLayout ordersListContainer;
    private TextView tvVolTitle;
    private DrawerLayout drawerLayout;

    private String currentVolId = "";
    private String currentEmail = "";
    private String currentName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        currentVolId = getValue("USER_ID", "userId");
        currentEmail = getValue("USER_EMAIL", "userEmail");
        currentName = getValue("USER_NAME", "userName");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_side);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        setNavigationHeader(navigationView);
        setNavigationActions(navigationView);

        ordersListContainer = findViewById(R.id.ordersListContainer);
        tvVolTitle = findViewById(R.id.tvVolTitle);

        fetchRequestsFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ordersListContainer != null) {
            fetchRequestsFromServer();
        }
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
        nameView.setText(currentName.isEmpty() ? "Volunteer" : currentName);
        emailView.setText(currentEmail.isEmpty() ? "No email found" : currentEmail);
    }

    private void setNavigationActions(NavigationView navigationView) {
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
                Toast.makeText(Volunteer.this, "Open an order, then tap Message Shopper.", Toast.LENGTH_LONG).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(Volunteer.this, currentName + "\n" + currentEmail, Toast.LENGTH_LONG).show();
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
        tvVolTitle.setText("Available Requests");
        showMessage("Loading available requests...");

        Request request = new Request.Builder().url(URL_REQUESTS).get().build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showMessage("Could not load requests. Tap Refresh."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body() == null ? "" : response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showMessage("Server error loading requests."));
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        renderRequests(new JSONArray(jsonData));
                    } catch (Exception e) {
                        showMessage("Bad response from getRequests.php.");
                    }
                });
            }
        });
    }

    private void renderRequests(JSONArray array) {
        ordersListContainer.removeAllViews();
        ordersListContainer.addView(makeRefreshButton());

        if (array.length() == 0) {
            addText("No available requests right now.", 16, Color.WHITE, 18);
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);

                String requestId = clean(obj.optString("request_id"));
                String shopperName = clean(obj.optString("shopper_name"));
                String status = clean(obj.optString("status"));
                JSONArray items = obj.optJSONArray("items");

                View card = getLayoutInflater().inflate(R.layout.item_order_card, ordersListContainer, false);

                TextView tvBasket = card.findViewById(R.id.tvBasketName);
                TextView tvShopper = card.findViewById(R.id.tvShopperName);
                TextView tvStatus = card.findViewById(R.id.tvStatus);
                Button btnAccept = card.findViewById(R.id.btnAcceptOrder);

                tvBasket.setText("Request #" + requestId);
                tvShopper.setText(shopperName.isEmpty() ? "Shopper" : shopperName);
                tvStatus.setText(status.isEmpty() ? "Pending" : status);

                btnAccept.setText("ACCEPT");
                btnAccept.setEnabled(true);
                btnAccept.setBackgroundColor(Color.rgb(76, 175, 80));
                btnAccept.setTextColor(Color.WHITE);

                String itemsJson = items == null ? "[]" : items.toString();

                btnAccept.setOnClickListener(v -> acceptOrder(requestId, shopperName, itemsJson, btnAccept, tvStatus));

                ordersListContainer.addView(card);
            } catch (Exception ignored) {
            }
        }
    }

    private Button makeRefreshButton() {
        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setAllCaps(false);
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundColor(Color.rgb(33, 150, 243));
        refresh.setOnClickListener(v -> fetchRequestsFromServer());
        return refresh;
    }

    private void showMessage(String message) {
        ordersListContainer.removeAllViews();
        ordersListContainer.addView(makeRefreshButton());
        addText(message, 16, Color.WHITE, 18);
    }

    private void addText(String text, int size, int color, int topPadding) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(size);
        tv.setPadding(8, topPadding, 8, 8);
        ordersListContainer.addView(tv);
    }

    private void acceptOrder(String requestId, String shopperName, String itemsJson, Button button, TextView statusView) {
        if (currentVolId.isEmpty()) {
            Toast.makeText(this, "Login problem: missing volunteer ID.", Toast.LENGTH_LONG).show();
            return;
        }

        button.setEnabled(false);
        button.setText("Accepting...");

        RequestBody body = new FormBody.Builder()
                .add("request_id", requestId)
                .add("volunteer_id", currentVolId)
                .add("volunteer_email", currentEmail)
                .build();

        Request request = new Request.Builder().url(URL_ACCEPT).post(body).build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    button.setEnabled(true);
                    button.setText("ACCEPT");
                    Toast.makeText(Volunteer.this, "Network error accepting order.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() == null ? "" : response.body().string().trim();

                runOnUiThread(() -> {
                    if (response.isSuccessful() && result.toLowerCase().contains("success")) {
                        statusView.setText("Accepted");
                        button.setText("VIEW");
                        button.setEnabled(true);
                        button.setBackgroundColor(Color.rgb(33, 150, 243));
                        button.setOnClickListener(v -> openOrderDetails(requestId, shopperName, "Accepted", itemsJson));
                        Toast.makeText(Volunteer.this, "Order accepted.", Toast.LENGTH_SHORT).show();
                    } else {
                        button.setEnabled(true);
                        button.setText("ACCEPT");
                        Toast.makeText(Volunteer.this, result.isEmpty() ? "Order was already taken." : result, Toast.LENGTH_LONG).show();
                        fetchRequestsFromServer();
                    }
                });
            }
        });
    }

    private void openOrderDetails(String requestId, String shopperName, String status, String itemsJson) {
        Intent intent = new Intent(Volunteer.this, OrderDetailsActivity.class);
        intent.putExtra("ORDER_ID", requestId);
        intent.putExtra("SHOPPER_NAME", shopperName);
        intent.putExtra("STATUS", status);
        intent.putExtra("ITEMS_JSON", itemsJson);
        intent.putExtra("USER_ID", currentVolId);
        intent.putExtra("USER_EMAIL", currentEmail);
        intent.putExtra("USER_NAME", currentName);
        startActivity(intent);
    }

    private String clean(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.equalsIgnoreCase("null") ? "" : trimmed;
    }
}
