package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

    RadioButton radShopper, radVolunteer;
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
    //
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();

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
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String res = response.body().string().trim();

                runOnUiThread(() -> {

                    if (res.startsWith("success")) {


                        String[] parts = res.split(":");


                        String role = parts.length > 1 ? parts[1] : "";
                        String name = parts.length > 2 ? parts[2] : "";


                        Intent intent = new Intent(MainActivity.this, pickActivity.class);


                        intent.putExtra("USER_NAME", name);
                        intent.putExtra("USER_EMAIL", email);


                        intent.putExtra("USER_ROLE", role);

                        startActivity(intent);

                        finish();

                    }
                    else if (res.equals("invalid")) {

                        Toast.makeText(
                                getApplicationContext(),
                                "Wrong email or password",
                                Toast.LENGTH_SHORT
                        ).show();

                    }
                    else {

                        Toast.makeText(
                                getApplicationContext(),
                                "Server Error: " + res,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }

    public void doDirectRegister(View view){
        Intent intent = new Intent(MainActivity.this, RegitserUser.class);
        intent.putExtra("Register", "");
        startActivity(intent);
    }
}