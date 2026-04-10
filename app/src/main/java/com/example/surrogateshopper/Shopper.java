package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Shopper extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopper);

        TextView hi = findViewById(R.id.welcomeShopper);

        Intent intent = getIntent();
        String name = intent.getStringExtra("Shopper_Name");

        hi.setText("Hi, " + name);



        Button btnAdd = findViewById(R.id.btnAdd);
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

                String prod = etProduct.getText().toString();
                System.out.println(prod);
            }
        });
    }






}