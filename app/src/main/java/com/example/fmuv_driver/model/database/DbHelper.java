package com.example.fmuv_driver.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fmuv_driver.model.pojo.OverSpeedLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Db";

    public static final String CREATE_OVER_SPEED_TABLE = "CREATE TABLE over_speed(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "speed VARCHAR(50)," +
            "time_stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "trip_id VARCHAR(50)," +
            "employee_id VARCHAR(50)" +
            ")";

    public static final String DROP_OVER_SPEED_TABLE = "DROP TABLE IF EXISTS over_speed";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_OVER_SPEED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_OVER_SPEED_TABLE);
        onCreate(db);
    }

    public void insert(String table, Map<String, String> data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        for (String key: data.keySet()) {
            values.put(key, data.get(key));
        }

        db.insert(table, null, values);
        db.close();
    }

    public List<OverSpeedLog> fetchAll(String table) {
        SQLiteDatabase db = getReadableDatabase();
        List<OverSpeedLog> overSpeedList = new ArrayList<>();

        Cursor cursor = db.query(table, null, null, null, null, null, null, null);
        if (cursor != null) {

        }

        if (cursor.moveToFirst()) {
            do {
                OverSpeedLog overSpeedLog = new OverSpeedLog();

                overSpeedLog.setSpeed(cursor.getString(cursor.getColumnIndex("speed")));
                overSpeedLog.setTimeStamp(cursor.getString(cursor.getColumnIndex("time_stamp")));
                overSpeedLog.setTripId(cursor.getString(cursor.getColumnIndex("trip_id")));

                overSpeedList.add(overSpeedLog);
            } while (cursor.moveToNext());
        }

        db.close();
        return overSpeedList;
    }

    public void deleteRow(String table, Map<String, String> data) {
        String tripId = data.get("trip_id");
        String timeStamp = data.get("time_stamp");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + table + " WHERE trip_id = '" + tripId + "' AND time_stamp = '" + timeStamp + "'");
        db.close();
    }

    public void deleteAll(String table) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + table);
        db.close();
    }

}
