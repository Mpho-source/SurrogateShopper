package com.example.surrogateshopper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedHashMap;
import java.util.Map;

public class Shopper extends AppCompatActivity {

    String productName = "";
    String productQty = "";
    String productSize = "";

    TextView textList;
    TextView tvBasket;
    Button btnCheckout;
    String basketName = "";
    TextView etBasket;
    Button btnSendRequest;
    LinearLayout itemsContainer;

    LinkedHashMap<String, Integer> Items = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper);

        TextView hi = findViewById(R.id.welcomeShopper);

        String name = getIntent().getStringExtra("USER_NAME");

        hi.setText("Hi 👋 " + name);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDate = now.format(formatter);
        System.out.println(formattedDate);
        System.out.println("HEEELLLOOOO WOOOORLLD");

        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);

        textList = findViewById(R.id.textList);
        tvBasket = findViewById(R.id.tvBasket);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setVisibility(View.INVISIBLE);
        etBasket = findViewById(R.id.etBasket);
        itemsContainer = findViewById(R.id.itemsContainer);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        tvBasket.setVisibility(View.INVISIBLE);
    }

    public void doShowItem(View view) {

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.dialog_item, null);

        bottomSheet.setContentView(sheetView);

        EditText etProductName = sheetView.findViewById(R.id.etProductName);
        EditText etQuantity = sheetView.findViewById(R.id.etQuantity);
        EditText etProductSize = sheetView.findViewById(R.id.etProductSize);

        Button btnAdd = sheetView.findViewById(R.id.btnAddProduct);

        btnAdd.setOnClickListener(v -> {

            btnCheckout.setVisibility(View.VISIBLE);
            ImageView img = findViewById(R.id.emptyBasket);
            img.setVisibility(View.INVISIBLE);

            TextView welcome = findViewById(R.id.welcomeShopper);
            welcome.setVisibility(View.INVISIBLE);

            TextView empty = findViewById(R.id.textEmpty);
            empty.setVisibility(View.INVISIBLE);

            productName = etProductName.getText().toString().trim();
            productSize = etProductSize.getText().toString().trim();
            productQty = etQuantity.getText().toString().trim();

            if (productName.isEmpty() ||
                    productSize.isEmpty() ||
                    productQty.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please fill all item parts",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            int qty;

            try {
                qty = Integer.parseInt(productQty);
            } catch (NumberFormatException e) {

                Toast.makeText(
                        this,
                        "Quantity must be a valid number",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            String toStore = productName + " (" + productSize + ")";
            addItemsToDB(productName, qty, productSize);

            tvBasket.setVisibility(View.VISIBLE);

            if (Items.containsKey(toStore)) {

                int oldQty = Items.get(toStore);

                Items.put(toStore, oldQty + qty);

            } else {

                Items.put(toStore, qty);
            }

            int countItems = Items.size();

            textList.setText(
                    "You have " + countItems + " item(s) in your basket");

            displayItemsWithoutListView();

            bottomSheet.dismiss();

            Toast.makeText(
                    this,
                    "Item added!",
                    Toast.LENGTH_SHORT).show();

            btnCheckout.setOnClickListener(view1 -> {
                popup();

            });

        });

        bottomSheet.show();
    }

    private void displayItemsWithoutListView() {

        LinearLayout container = findViewById(R.id.itemsContainer);

        container.removeAllViews();

        int itemNumber = 1;

        for (Map.Entry<String, Integer> entry : Items.entrySet()) {

            View rowView = getLayoutInflater().inflate(
                    R.layout.item_row,
                    container,
                    false);

            TextView txtName = rowView.findViewById(R.id.rowName);
            TextView txtQty = rowView.findViewById(R.id.rowQty);

            txtName.setText(
                    itemNumber + ". " + entry.getKey());

            txtQty.setText(
                    "Qty: " + entry.getValue());

            container.addView(rowView);

            itemNumber++;
        }
    }

    private void popup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        builder.setView(popupView);

        EditText etName = popupView.findViewById(R.id.etName);

        builder.setPositiveButton("Save", (dialog, which) -> {

            String name = etName.getText().toString();
            basketName = name;

            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String formattedDate = now.format(formatter);

            etBasket.setVisibility(View.VISIBLE);

            etBasket.setText("BASKET: " + name + " " + formattedDate);

            textList.setVisibility(View.INVISIBLE);
            tvBasket.setVisibility(View.INVISIBLE);
            btnCheckout.setVisibility(View.INVISIBLE);
            itemsContainer.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.VISIBLE);

        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    public void addItemsToDB(String name, int quantity, String size) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("quantity", quantity)
                .add("size", size)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/products_items.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errorType = e.getClass().getSimpleName();
                final String errorMsg = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(RegitserUser.this, errorType + ": " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    if (res.contains("success")) {
                        Toast.makeText(RegitserUser.this, "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegitserUser.this, "Server error: " + res, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}