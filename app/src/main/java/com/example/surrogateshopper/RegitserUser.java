package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegitserUser extends AppCompatActivity {


    EditText etPassword ;
    String pass1 ;

    EditText etPasswordVal ;
    String pass2 ;
    EditText etName;
    String names;
    EditText etEmail;


    String userStreet = "";
    String userSuburb = "";
    String userCity = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regitser_user);

        etPassword = findViewById(R.id.etPassword);

        etPasswordVal = findViewById(R.id.etPasswordVal);


        etName = findViewById(R.id.etName);


        etEmail = findViewById(R.id.etEmail);

    }

    public void doshowAddressSheet(View view) {

        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_address, null);
        bottomSheet.setContentView(sheetView);

        EditText etStreet = sheetView.findViewById(R.id.etProductName);
        EditText etSuburb = sheetView.findViewById(R.id.etSuburb);
        EditText etCity = sheetView.findViewById(R.id.etProductSize);
        Button btnSave = sheetView.findViewById(R.id.btnAddProduct);


        btnSave.setOnClickListener(v -> {
            userStreet = etStreet.getText().toString().trim();
            userSuburb = etSuburb.getText().toString().trim();
            userCity = etCity.getText().toString().trim();

            if (userStreet.isEmpty() || userSuburb.isEmpty() || userCity.isEmpty()) {
                Toast.makeText(this, "Please fill all address parts", Toast.LENGTH_SHORT).show();
            } else {
                bottomSheet.dismiss();
                Toast.makeText(this, "Address saved!", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheet.show();
    }


    public void doRegister(View view) {
        String names = etName.getText().toString().trim();
        String emailAddress = etEmail.getText().toString().trim();
        String pass1 = etPassword.getText().toString().trim();
        String pass2 = etPasswordVal.getText().toString().trim();
        String role = "";


        if (names.isEmpty() || emailAddress.isEmpty() || pass1.isEmpty() ) {
            Toast.makeText(this, "Fields missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass1.equals(pass2)) {
            Toast.makeText(this, "Passwords mismatch", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("full_name", names)
                .add("email", emailAddress)
                .add("password", pass1)
                .add("street",userStreet)
                .add("suburb", userSuburb)
                .add("city", userCity)

                .build();


        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/profile.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errorType = e.getClass().getSimpleName();
                final String errorMsg = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(RegitserUser.this, errorType + ": " + errorMsg, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(() -> {
                    if (res.contains("success")) {
                        Toast.makeText(RegitserUser.this, "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegitserUser.this, "Server error: " + res, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void addItemsToDB(String name, int quantity, String size){

    }

    public boolean validatePass(String pass1, String pass2){
        boolean check = true;

        if(!(pass1.equals(pass2))){
            check = false;
        }
        return check;
    }



}
