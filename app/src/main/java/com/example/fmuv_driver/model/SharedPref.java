package com.example.fmuv_driver.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context,  String name) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(name, 0);
    }

    public boolean isContain(String key) {
        if (sharedPreferences.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    public void setAll(Map<String, String> value) {
        editor = sharedPreferences.edit();
        for (String key: value.keySet()) {
            editor.putString(key, value.get(key));
        }
        editor.commit();
    }

    public void setValue(String key, String value) {
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    public String getValue(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String getUserToken() {
        return sharedPreferences.getString("token", "");
    }


    public void clearSharedPref() {
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

}