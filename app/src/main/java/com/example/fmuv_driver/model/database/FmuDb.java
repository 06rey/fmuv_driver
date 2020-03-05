package com.example.fmuv_driver.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FmuDb extends SQLiteOpenHelper {
    private static final String DB_NAME = "fmuDb";

    // ---------------------------------------------------------------------------------------------
    // -------------------------------------- CREATE/DROP TABLE ------------------------------------
    // ---------------------------------------------------------------------------------------------

    public static final String CREATE_TABLE_BOOKING = "CREATE TABLE booking(" +
           "bookId INTEGER PRIMARY KEY AUTOINCREMENT," +
           "booking_id INTEGER," +
           "no_of_passenger INTEGER," +
           "amount DOUBLE," +
           "pass_type VARCHAR(50)," +
           "time_stamp DATETIME," +
           "passenger_id INTEGER)";

    public static final String CREATE_TABLE_SEAT = "CREATE TABLE seat(" +
            "seatId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "seat_id INTEGER," +
            "boarding_pass VARCHAR(5)," +
            "full_name VARCHAR(5)," +
            "seat_no INTEGER," +
            "pick_up_loc VARCHAR(255)," +
            "drop_off_loc VARCHAR(255)," +
            "boarding_status VARCHAR(50)," +
            "pick_up_time DATETIME," +
            "drop_off_time DATETIME," +
            "booking_id INTEGER)";

    public static final String DROP_TABLE_BOOKING = "DROP TABLE IF EXISTS booking";
    public static final String DROP_TABLE_SEAT = "DROP TABLE IF EXISTS seat";

    public FmuDb(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOKING);
        db.execSQL(CREATE_TABLE_SEAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_BOOKING);
        db.execSQL(DROP_TABLE_SEAT);
        onCreate(db);
    }

    public List<Map<String, String>> fetchAllData(String table) {
        SQLiteDatabase db = getReadableDatabase();

        List<Map<String, String>> result = new ArrayList<>();
        Cursor cursor = db.query(table, null, null, null, null, null, null, null);
        if (cursor != null) {

        }
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> rowData = new HashMap<>();
                rowData.put("booking_id", cursor.getString(cursor.getColumnIndex("booking_id")));
                result.add(rowData);
            } while (cursor.moveToNext());
        }
        db.close();
        return result;
    }

    // INSERT ROWS
    public void insert(String table, Map<String, String> data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        for (String key: data.keySet()) {
            values.put(key, data.get(key));
        }
        db.insert(table, null, values);
        db.close();
    }

    // DELETE ROWS
    public void deleteAll(String table) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + table);
        db.close();
    }


}
