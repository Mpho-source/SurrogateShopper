package com.example.surrogateshopper;


//import static com.example.surrogatetester.R.id.etName;
import com.example.surrogateshopper.R;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Volunteer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        TextView hi = findViewById(R.id.welcome);

        Intent intent = getIntent();
        String name = intent.getStringExtra("Volunteer_Name");

        hi.setText("Hi, " + name);
    }



}