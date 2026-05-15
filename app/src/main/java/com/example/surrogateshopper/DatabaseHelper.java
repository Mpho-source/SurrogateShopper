package com.example.surrogateshopper;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public static class PickActivity extends AppCompatActivity {


        RadioButton radShop, radVolunteer;


        String name, email;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.pick_activity);


            radShop = findViewById(R.id.radShop);
            radVolunteer = findViewById(R.id.radVolunteer);


            Intent receivedIntent = getIntent();

            name = receivedIntent.getStringExtra("USER_NAME");
            email = receivedIntent.getStringExtra("USER_EMAIL");
        }


        public void doSignIn(View view) {

            Intent intent;


            if (radShop.isChecked()) {

                intent = new Intent(PickActivity.this, Shopper.class);

            }
            else if (radVolunteer.isChecked()) {

                intent = new Intent(PickActivity.this, Volunteer.class);

            }
            else {

                Toast.makeText(
                        this,
                        "Please choose an activity",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            intent.putExtra("USER_NAME", name);
            intent.putExtra("USER_EMAIL", email);

            startActivity(intent);
        }
    }
}
