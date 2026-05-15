package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class pickActivity extends AppCompatActivity {

    // Radio buttons
    RadioButton radShop, radVolunteer;

    // User data
    String name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_activity);

        // Connect XML views to Java
        radShop = findViewById(R.id.radShop);
        radVolunteer = findViewById(R.id.radVolunteer);

        // Get data from previous activity
        Intent receivedIntent = getIntent();

        name = receivedIntent.getStringExtra("USER_NAME");
        email = receivedIntent.getStringExtra("USER_EMAIL");
    }

    // Called when button is clicked
    public void doSignIn(View view) {

        Intent intent;

        // Check which role was selected
        if (radShop.isChecked()) {

            intent = new Intent(pickActivity.this, Shopper.class);

        }
        else if (radVolunteer.isChecked()) {

            intent = new Intent(pickActivity.this, Volunteer.class);

        }
        else {

            Toast.makeText(
                    this,
                    "Please choose an activity",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Send user info to next activity
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_EMAIL", email);

        startActivity(intent);
        finish();

    }
}