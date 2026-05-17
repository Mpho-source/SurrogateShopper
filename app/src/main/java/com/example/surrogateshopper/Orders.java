package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.util.HashMap;
import java.util.Map;

public class Orders extends AppCompatActivity {

    TextView tvOrderBasket, tvOrderStatus;
    LinearLayout itemsContainer;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // --- TOOLBAR & DRAWER SETUP ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_side);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(Orders.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // --- VIEW BINDING ---
        tvOrderBasket = findViewById(R.id.tvOrderBasket);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        itemsContainer = findViewById(R.id.itemsContainerOrders);
        View basketCard = findViewById(R.id.basketCard);
        TextView textEmpty = findViewById(R.id.textEmpty);
        TextView headerTitle = findViewById(R.id.headerTitle);

        // --- DATA LOGIC ---
        HashMap<String, Integer> items = (HashMap<String, Integer>) getIntent().getSerializableExtra("BASKET_ITEMS");
        String basketName = getIntent().getStringExtra("BASKET_NAME");

        if (items != null && !items.isEmpty()) {
            basketCard.setVisibility(View.VISIBLE);
            headerTitle.setVisibility(View.VISIBLE);
            if (textEmpty != null) textEmpty.setVisibility(View.GONE);

            tvOrderBasket.setText(basketName != null ? basketName : "My Basket");
            tvOrderStatus.setText("PENDING");

            displayOrderItems(items);
        } else {
            basketCard.setVisibility(View.GONE);
            headerTitle.setVisibility(View.GONE);
            if (textEmpty != null) {
                textEmpty.setVisibility(View.VISIBLE);
                textEmpty.setText("You have no active orders at the moment.");
            }
        }
    }

    private void displayOrderItems(HashMap<String, Integer> items) {
        itemsContainer.removeAllViews();
        int number = 1;
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            TextView row = new TextView(this);
            row.setText(number + ". " + entry.getKey() + " x" + entry.getValue());
            row.setTextColor(0xFFFFFFFF);
            row.setPadding(0, 10, 0, 10);
            itemsContainer.addView(row);
            number++;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}