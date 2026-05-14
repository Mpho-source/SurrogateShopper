package com.example.surrogateshopper;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderDetailsActivity extends AppCompatActivity {

    TextView tvItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvItems = findViewById(R.id.tvItems);

        String orderId = getIntent().getStringExtra("ORDER_ID");

        Order order = null;

        for (Order o : Volunteer.orders) {
            if (o.id.equals(orderId)) {
                order = o;
                break;
            }
        }

        if (order == null) return;

        StringBuilder sb = new StringBuilder();

        for (String item : order.items) {
            sb.append("• ").append(item).append("\n");
        }

        tvItems.setText(sb.toString());
    }
}