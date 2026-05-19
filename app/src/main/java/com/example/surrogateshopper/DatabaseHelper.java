package com.example.surrogateshopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShopperDB";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(
                "CREATE TABLE basket (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "qty INTEGER," +
                        "size TEXT)"
        );


        db.execSQL(
                "CREATE TABLE orders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "basket_name TEXT," +
                        "items TEXT," +
                        "timestamp TEXT," +
                        "status TEXT," +
                        "email TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS basket");
        db.execSQL("DROP TABLE IF EXISTS orders");

        onCreate(db);
    }





    public void insertItem(String name, int qty, String size) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("qty", qty);
        values.put("size", size);

        db.insert("basket", null, values);
    }

    public HashMap<String, Integer> getAllItems() {

        HashMap<String, Integer> items = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM basket", null);

        if (cursor.moveToFirst()) {

            do {

                String key =
                        cursor.getString(1)
                                + " (" +
                                cursor.getString(3)
                                + ")";

                items.put(key, cursor.getInt(2));

            } while (cursor.moveToNext());
        }

        cursor.close();

        return items;
    }

    public void clearBasket() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("basket", null, null);
    }





    public void saveOrder(String basketName,
                          HashMap<String, Integer> items,
                          String timestamp,
                          String status,
                          String email) {

        SQLiteDatabase db = this.getWritableDatabase();

        JSONArray jsonArray = new JSONArray();

        try {

            for (Map.Entry<String, Integer> entry : items.entrySet()) {

                JSONObject obj = new JSONObject();

                obj.put("item", entry.getKey());
                obj.put("qty", entry.getValue());

                jsonArray.put(obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();

        values.put("basket_name", basketName);
        values.put("items", jsonArray.toString());
        values.put("timestamp", timestamp);
        values.put("status", status);
        values.put("email", email);

        db.insert("orders", null, values);
    }
}
