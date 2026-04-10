package com.example.surrogateshopper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnSign;
    RadioButton radShopper, radVolunteer;
    EditText etName, etPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSign = findViewById(R.id.btnSign);
        radShopper = findViewById(R.id.radShopper);
        radVolunteer = findViewById(R.id.radVolunteer);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);


        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(radVolunteer.isChecked() && !etName.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()){
                    Intent intent = new Intent(MainActivity.this, Volunteer.class);
                    String name = etName.getText().toString();
                    intent.putExtra("Volunteer_Name", name);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Signing in..." + name, Toast.LENGTH_SHORT).show();
                }
                else if(radShopper.isChecked() && !etName.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty()){

                    Intent intent = new Intent(MainActivity.this, Shopper.class);
                    String name = etName.getText().toString();
                    intent.putExtra("Shopper_Name", name);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Signing in..." + name, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Enter all the required details", Toast.LENGTH_LONG).show();
                }


            }
        });



    }
}