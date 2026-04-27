package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class RegitserUser extends AppCompatActivity {


    EditText etPassword = findViewById(R.id.etPassword);
    String pass1 = etPassword.getText().toString();

    EditText etPasswordVal = findViewById(R.id.etPasswordVal);
    String pass2 = etPasswordVal.getText().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regitser_user);
    }


    public boolean validatePass(String pass1, String pass2){
        boolean check = true;

        if(!(pass1.equals(pass2))){
            check = false;
        }


        return check;
    }

}