package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Volunteer extends AppCompatActivity {

    public static List<Order> orders = new ArrayList<>();

    private LinearLayout ordersListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        ordersListContainer = findViewById(R.id.ordersListContainer);

        loadMockOrders();
    }

    private void loadMockOrders() {
        List<String> items = new ArrayList<>();
        items.add("Milk");
        items.add("Bread");

        orders.add(new Order("1", "Monthly Groceries", "Mpho", "Pending", items));

        renderOrders();
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

            // CLICK → OPEN DETAILS
            card.setOnClickListener(v -> {
                Intent intent = new Intent(Volunteer.this, OrderDetailsActivity.class);
                intent.putExtra("ORDER_ID", order.id);
                startActivity(intent);
            });

            // ACCEPT → CHANGE STATUS (cycle)
            btnAccept.setOnClickListener(v -> {

                if (order.status.equals("Pending")) {
                    order.status = "Accepted";
                } else if (order.status.equals("Accepted")) {
                    order.status = "In Progress";
                } else if (order.status.equals("In Progress")) {
                    order.status = "Delivered";
                }

                renderOrders();
            });

            ordersListContainer.addView(card);
        }
    }
}