package com.example.surrogateshopper;


//import static com.example.surrogatetester.R.id.etName;
import com.example.surrogateshopper.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Volunteer extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        TextView hi = findViewById(R.id.welcome);

        Intent intent = getIntent();
        String name = intent.getStringExtra("USER_NAME");

        hi.setText("Hi, " + name);
    }






}