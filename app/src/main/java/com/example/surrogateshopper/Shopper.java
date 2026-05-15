package com.example.surrogateshopper;

import java.util.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Shopper extends AppCompatActivity {

    String productName = "";
    String productQty = "";
    String productSize = "";
    String emailForDB = "";

    TextView textList;
    TextView tvBasket;
    Button btnCheckout;
    FloatingActionButton btnAdd;
    String basketName = "";
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

        emailForDB = getIntent().getStringExtra("USER_EMAIL");

        TextView hi = findViewById(R.id.welcomeShopper);
        String name = getIntent().getStringExtra("USER_NAME");
        hi.setText("Hi 👋 " + name);

        getDetails(emailForDB);

        textList = findViewById(R.id.textList);
        tvBasket = findViewById(R.id.tvBasket);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setVisibility(View.INVISIBLE);
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
                if (id == R.id.nav_logout) {
                    Intent intent = new Intent(Shopper.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(id == R.id.nav_order){
                    Intent intent = new Intent(Shopper.this, Orders.class);
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
        Button btnAddProduct = sheetView.findViewById(R.id.btnAddProduct);

        btnAddProduct.setOnClickListener(v -> {
            btnCheckout.setVisibility(View.VISIBLE);
            findViewById(R.id.emptyBasket).setVisibility(View.INVISIBLE);
            findViewById(R.id.welcomeShopper).setVisibility(View.INVISIBLE);
            findViewById(R.id.textEmpty).setVisibility(View.INVISIBLE);

            productName = etProductName.getText().toString().trim();
            productSize = etProductSize.getText().toString().trim();
            productQty = etQuantity.getText().toString().trim();

            if (productName.isEmpty() || productSize.isEmpty() || productQty.isEmpty()) {
                Toast.makeText(this, "Please fill all item parts", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(productQty);
            String toStore = productName + " (" + productSize + ")";

            if (Items.containsKey(toStore)) {
                Items.put(toStore, Items.get(toStore) + qty);
            } else {
                Items.put(toStore, qty);
            }

            tvBasket.setVisibility(View.VISIBLE);
            textList.setText("You have " + Items.size() + " item(s) in your basket");
            displayItemsWithoutListView();
            bottomSheet.dismiss();

            btnCheckout.setOnClickListener(view1 -> popup());
        });
        bottomSheet.show();
    }

    private void displayItemsWithoutListView() {
        LinearLayout container = findViewById(R.id.itemsContainer);
        container.removeAllViews();
        int itemNumber = 1;
        for (Map.Entry<String, Integer> entry : Items.entrySet()) {
            View rowView = getLayoutInflater().inflate(R.layout.item_row, container, false);
            TextView txtName = rowView.findViewById(R.id.rowName);
            TextView txtQty = rowView.findViewById(R.id.rowQty);
            txtName.setText(itemNumber + ". " + entry.getKey());
            txtQty.setText("Qty: " + entry.getValue());
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
            if (name.isEmpty()) return;

            basketName = name;
            basketItemsExpanded.removeAllViews();

            addItemsToDB(Items, emailForDB);

            int number = 1;
            for (Map.Entry<String, Integer> entry : Items.entrySet()) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView itemTxt = new TextView(this);
                itemTxt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                itemTxt.setText(number + ". " + entry.getKey() + " x" + entry.getValue());
                itemTxt.setTextColor(0xFFFFFFFF);
                row.addView(itemTxt);
                basketItemsExpanded.addView(row);
                number++;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault());
            tvBasketName.setText(basketName);
            tvBasketTimestamp.setText(formatter.format(new Date()));

            basketCard.setVisibility(View.VISIBLE);
            textList.setVisibility(View.INVISIBLE);
            tvBasket.setVisibility(View.INVISIBLE);
            btnCheckout.setVisibility(View.INVISIBLE);
            itemsContainer.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.INVISIBLE);

            basketCard.setOnClickListener(v -> {
                basketExpanded = !basketExpanded;
                basketItemsExpanded.setVisibility(basketExpanded ? View.VISIBLE : View.GONE);
                basketDivider.setVisibility(basketExpanded ? View.VISIBLE : View.GONE);
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void getDetails(String email) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder().add("email", email).build();
        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/getDetails.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(responseData);
                            View headerView = navigationView.getHeaderView(0);
                            TextView nName = headerView.findViewById(R.id.nav_user_name);
                            TextView nEmail = headerView.findViewById(R.id.nav_user_email);
                            nName.setText(json.getString("full_name"));
                            nEmail.setText(json.getString("email"));
                        } catch (JSONException e) { e.printStackTrace(); }
                    });
                }
            }
        });
    }

    public void addItemsToDB(HashMap<String, Integer> itemsMap, String email) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Map.Entry<String, Integer> entry : itemsMap.entrySet()) {
                String fullKey = entry.getKey();
                String nameOnly = fullKey;
                String sizeOnly = "";

                if (fullKey.contains(" (") && fullKey.endsWith(")")) {
                    int bracketIndex = fullKey.lastIndexOf(" (");
                    nameOnly = fullKey.substring(0, bracketIndex);
                    sizeOnly = fullKey.substring(bracketIndex + 2, fullKey.length() - 1);
                }

                JSONObject obj = new JSONObject();
                obj.put("name", nameOnly);
                obj.put("quantity", entry.getValue());
                obj.put("size", sizeOnly);
                jsonArray.put(obj);
            }
        } catch (JSONException e) { e.printStackTrace(); }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("items_json", jsonArray.toString())
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/products_items.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    runOnUiThread(() -> Toast.makeText(Shopper.this, res, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    public void doSendRequest(View view){
        Intent intent = new Intent(Shopper.this, Orders.class);
        intent.putExtra("BASKET_NAME", basketName);
        intent.putExtra("BASKET_ITEMS", Items);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}