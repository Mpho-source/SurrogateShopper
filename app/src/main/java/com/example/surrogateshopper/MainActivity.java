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

    EditText etName, etPassword;

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

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/signin.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string().trim();

                runOnUiThread(() -> {
                    // Inside MainActivity.java -> onResponse -> runOnUiThread
                    if (res.startsWith("success")) {
                        String[] parts = res.split(":");



                        String realName = parts.length > 1 ? parts[1] : "User";
                        String realId = parts.length > 2 ? parts[2] : "";

                        Intent intent = new Intent(MainActivity.this, pickActivity.class);
                        intent.putExtra("USER_NAME", realName); // Correctly sets the name string
                        intent.putExtra("USER_ID", realId);     // Correctly sets the ID string
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                        finish();
                    } else if (res.equals("invalid")) {
                        Toast.makeText(getApplicationContext(), "Wrong email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Server Error: " + res, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void doDirectRegister(View view){
        startActivity(new Intent(MainActivity.this, RegitserUser.class));
    }
}