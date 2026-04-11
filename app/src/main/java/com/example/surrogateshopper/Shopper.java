package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.R.layout;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class Shopper extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper);

        TextView hi = findViewById(R.id.welcomeShopper);

        Intent intent = getIntent();
        String name = intent.getStringExtra("Shopper_Name");

        hi.setText("Hi \uD83D\uDC4B " + name);



        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        Button btnGo = findViewById(R.id.btnGo);
        Button btnDone = findViewById(R.id.btnDone);
        EditText etProduct = findViewById(R.id.etProduct);
        ArrayList<String> products = new ArrayList<>();
        ListView productsList = findViewById(R.id.productsList);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textEmpty = findViewById(R.id.textEmpty);
                textEmpty.setText("");
                ImageView img = findViewById(R.id.emptyBasket);
                img.setVisibility(View.INVISIBLE);

                EditText etProduct = findViewById(R.id.etProduct);
                etProduct.setVisibility(View.VISIBLE);

                Button btnGo = findViewById(R.id.btnGo);
                btnGo.setVisibility(View.VISIBLE);

                Button btnDone = findViewById(R.id.btnDone);
                btnDone.setVisibility(View.VISIBLE);

                TextView welcomeShopper = findViewById(R.id.welcomeShopper);
                welcomeShopper.setVisibility(View.INVISIBLE);

                TextView textList = findViewById(R.id.textList);
                textList.setText("");
                String prod = etProduct.getText().toString();
                System.out.println(prod);
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String prodEntered = etProduct.getText().toString();
                products.add(prodEntered);

                String prod = etProduct.getText().toString();
                //Toast.makeText(Shopper.this, "Added " + prod, Toast.LENGTH_SHORT).show();
                Snackbar.make(btnGo, "Added " + prod, Snackbar.LENGTH_SHORT).setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .show();

                etProduct.setText("");






            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                productsList.setVisibility(View.VISIBLE);
                etProduct.setVisibility(View.INVISIBLE);
                btnDone.setVisibility(View.INVISIBLE);
                btnGo.setVisibility(View.INVISIBLE);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(Shopper.this, layout.simple_list_item_1, products);
                productsList.setAdapter(adapter);

                TextView textList = findViewById(R.id.textList);
                textList.setText("MY LIST");



            }
        });


    }






}