package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class pickActivity extends AppCompatActivity {

    RadioButton radShop, radVolunteer;
    String name, email, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_activity);

        radShop = findViewById(R.id.radShop);
        radVolunteer = findViewById(R.id.radVolunteer);

        // Retrieve data from MainActivity
        name = getIntent().getStringExtra("USER_NAME");
        email = getIntent().getStringExtra("USER_EMAIL");
        userId = getIntent().getStringExtra("USER_ID");
    }

    public void doSignIn(View view) {
        Intent intent;

        // Get the data that MainActivity sent
        String name = getIntent().getStringExtra("USER_NAME");
        String email = getIntent().getStringExtra("USER_EMAIL");
        String id = getIntent().getStringExtra("USER_ID");

        if (radShop.isChecked()) {
            intent = new Intent(pickActivity.this, Shopper.class);
        } else if (radVolunteer.isChecked()) {
            intent = new Intent(pickActivity.this, Volunteer.class);
        } else {
            Toast.makeText(this, "Please choose an activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the actual Name and ID to the Shopper activity
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_ID", id);
        intent.putExtra("USER_EMAIL", email);

        startActivity(intent);
        finish();
    }
}