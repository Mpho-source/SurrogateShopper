package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etName, etPassword;
    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final String SIGNIN_URL = "https://wmc.ms.wits.ac.za/students/sgroup2715/signin.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
    }

    public void doSignIn(View view) {
        String email = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(SIGNIN_URL)
                .post(formBody)
                .build();

        HTTP.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body() == null ? "" : response.body().string().trim();

                runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Server error", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (res.startsWith("success")) {
                        String[] parts = res.split(":", 3);
                        String realName = parts.length > 1 ? parts[1] : "User";
                        String realId = parts.length > 2 ? parts[2] : "";
                        String savedRole = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userRole", "");

                        getSharedPreferences("UserSession", MODE_PRIVATE)
                                .edit()
                                .putString("userEmail", email)
                                .putString("userName", realName)
                                .putString("userId", realId)
                                .apply();

                        if (savedRole.equals("shopper")) {
                            openShopper(realName, realId, email);
                        } else if (savedRole.equals("volunteer")) {
                            openVolunteer(realName, realId, email);
                        } else {
                            Intent intent = new Intent(MainActivity.this, pickActivity.class);
                            intent.putExtra("USER_NAME", realName);
                            intent.putExtra("USER_ID", realId);
                            intent.putExtra("USER_EMAIL", email);
                            startActivity(intent);
                            finish();
                        }
                    } else if (res.equals("invalid")) {
                        Toast.makeText(MainActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Server error: " + res, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void openShopper(String name, String id, String email) {
        Intent intent = new Intent(MainActivity.this, Shopper.class);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_ID", id);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }

    private void openVolunteer(String name, String id, String email) {
        Intent intent = new Intent(MainActivity.this, Volunteer.class);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("USER_ID", id);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }

    public void doDirectRegister(View view) {
        startActivity(new Intent(MainActivity.this, RegitserUser.class));
    }
}
