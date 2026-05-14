package com.example.surrogateshopper;

import java.util.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class Shopper extends AppCompatActivity {

    String productName = "";
    String productQty = "";
    String productSize = "";

    TextView textList;
    TextView tvBasket;
    Button btnCheckout;
   // Button btnAdd;
    FloatingActionButton btnAdd;
    String basketName = "";
    TextView etBasket;
    Button btnSendRequest;
    LinearLayout itemsContainer;

    HashMap<String, Integer> Items = new HashMap<>();







    MaterialCardView basketCard;
    TextView tvBasketName;
    TextView tvBasketTimestamp;
    TextView tvOrderStatus;
    TextView tvChevron;
    LinearLayout basketItemsExpanded;
    View basketDivider;
    boolean basketExpanded = false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper);

        TextView hi = findViewById(R.id.welcomeShopper);

        String name = getIntent().getStringExtra("USER_NAME");

        hi.setText("Hi 👋 " + name);
       /* LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDate = now.format(formatter);
        System.out.println(formattedDate);
        System.out.println("HEEELLLOOOO WOOOORLLD");*/


        String emailForDB = getIntent().getStringExtra("USER_EMAIL");
        getDetails(emailForDB);


        textList = findViewById(R.id.textList);
        tvBasket = findViewById(R.id.tvBasket);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setVisibility(View.INVISIBLE);
        //etBasket = findViewById(R.id.etBasket);
        itemsContainer = findViewById(R.id.itemsContainer);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnAdd = findViewById(R.id.btnAdd);
        tvBasket.setVisibility(View.INVISIBLE);


        basketCard = findViewById(R.id.basketCard);
        tvBasketName = findViewById(R.id.tvBasketName);
        tvBasketTimestamp = findViewById(R.id.tvBasketTimestamp);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvChevron= findViewById(R.id.tvChevron);
        basketItemsExpanded = findViewById(R.id.basketItemsExpanded);
        basketDivider= findViewById(R.id.basketDivider);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_side);


        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    Toast.makeText(Shopper.this, "Heyy", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {

                    Intent intent = new Intent(Shopper.this, MainActivity.class);
                    intent.putExtra("Log out", "");
                    startActivity(intent);
                    finish();
                    Toast.makeText(Shopper.this, "Loggin out...", Toast.LENGTH_SHORT).show();

                }
                else if(id == R.id.nav_home){
                    Toast.makeText(Shopper.this, "Heyy", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.nav_order){
                    Intent intent = new Intent(Shopper.this, Orders.class);
                    intent.putExtra("Order", "");
                    startActivity(intent);
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });


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

            /*Toast.makeText(
                    this,
                    "Item added!",
                    Toast.LENGTH_SHORT).show();*/

            btnCheckout.setOnClickListener(view1 -> {
                popup();

            });

        });

        bottomSheet.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a basket name", Toast.LENGTH_SHORT).show();
                return;
            }

            basketName = name;

            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault());
            String formattedDate = formatter.format(new Date());


            tvBasketName.setText(basketName);
            tvBasketTimestamp.setText(formattedDate);
            tvOrderStatus.setText("PENDING");
            tvOrderStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));


            basketItemsExpanded.removeAllViews();
            int number = 1;
            for (Map.Entry<String, Integer> entry : Items.entrySet()) {

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 10, 0, 10);

                TextView name_ = new TextView(this);
                name_.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                name_.setText(number + ".  " + entry.getKey());
                name_.setTextColor(0xFFFFFFFF);
                name_.setTextSize(14);

                TextView qty = new TextView(this);
                qty.setText("×" + entry.getValue());
                qty.setTextColor(0xCCFFFFFF);
                qty.setTextSize(14);

                row.addView(name_);

                if (number < Items.size()) {
                    View line = new View(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    lp.setMargins(0, 4, 0, 0);
                    line.setLayoutParams(lp);
                    line.setBackgroundColor(0x33FFFFFF);

                    LinearLayout wrapper = new LinearLayout(this);
                    wrapper.setOrientation(LinearLayout.VERTICAL);
                    wrapper.addView(row);
                    wrapper.addView(line);
                    basketItemsExpanded.addView(wrapper);
                } else {
                    basketItemsExpanded.addView(row);
                }

                number++;
            }


            basketCard.setVisibility(View.VISIBLE);
            textList.setVisibility(View.INVISIBLE);
            tvBasket.setVisibility(View.INVISIBLE);
            btnCheckout.setVisibility(View.INVISIBLE);
            itemsContainer.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.INVISIBLE);

            basketCard.setOnClickListener(v -> {
                basketExpanded = !basketExpanded;

                if (basketExpanded) {
                    basketItemsExpanded.setVisibility(View.VISIBLE);
                    basketDivider.setVisibility(View.VISIBLE);
                    //tvChevron.setText("▲");
                } else {
                    basketItemsExpanded.setVisibility(View.GONE);
                    basketDivider.setVisibility(View.GONE);
                    //tvChevron.setText("▼");
                }
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    public void updateNavHeader(String Name, String Email){


        View headerView = navigationView.getHeaderView(0);

        TextView nav_user_name = headerView.findViewById(R.id.nav_user_name);
        TextView nav_user_email = headerView.findViewById(R.id.nav_user_email);

        if (nav_user_name != null && nav_user_email != null) {
            nav_user_name.setText(Name);
            nav_user_email.setText(Email);
        }


    }

    public void doSendRequest(View view){
        Intent intent = new Intent(Shopper.this, Orders.class);


        intent.putExtra("BASKET_NAME", basketName);

        intent.putExtra("BASKET_ITEMS", Items);

        startActivity(intent);
    }
    private void getDetails(String email) {
        OkHttpClient client = new OkHttpClient();


        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/getDetails.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();

                    runOnUiThread(() -> {
                        try {

                            JSONObject json = new JSONObject(responseData);
                            String name = json.getString("full_name");
                            String email = json.getString("email");

                            updateNavHeader(name, email);
                        } catch (JSONException e) { e.printStackTrace(); }
                    });
                }
            }
        });
    }



    public void addItemsToDB(String name, int quantity, String size) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("quantity", String.valueOf(quantity))
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
                    Toast.makeText(Shopper.this, errorType + ": " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    if (res.contains("success")) {
                        Toast.makeText(Shopper.this, "Added " + name, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(Shopper.this, "Server error: " + res, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}