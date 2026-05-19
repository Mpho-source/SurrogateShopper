package com.example.surrogateshopper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class pickActivity extends AppCompatActivity {

    private RadioButton radShop, radVolunteer;
    private String name, email, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_activity);

        radShop = findViewById(R.id.radShop);
        radVolunteer = findViewById(R.id.radVolunteer);

        name = getIntent().getStringExtra("USER_NAME");
        email = getIntent().getStringExtra("USER_EMAIL");
        userId = getIntent().getStringExtra("USER_ID");
    }

    public void doSignIn(View view) {
        Intent intent;
        String role;

        if (radShop.isChecked()) {
            intent = new Intent(pickActivity.this, Shopper.class);
            role = "shopper";
        } else if (radVolunteer.isChecked()) {
            intent = new Intent(pickActivity.this, Volunteer.class);
            role = "volunteer";
        } else {
            Toast.makeText(this, "Please choose a role", Toast.LENGTH_SHORT).show();
            return;
        }

        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("userRole", role)
                .putString("userEmail", email == null ? "" : email)
                .putString("userName", name == null ? "User" : name)
                .putString("userId", userId == null ? "" : userId)
                .apply();

        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
