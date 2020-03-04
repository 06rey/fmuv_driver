package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.handler.ServiceServerEventResponseHandler;
import com.example.fmuv_driver.model.BackgroundHttpRequest;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.model.database.DbHelper;
import com.example.fmuv_driver.model.pojo.OverSpeedLog;
import com.example.fmuv_driver.utility.CheckInternet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String OVER_SPEED_MODE = "over_speed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Resend pending over speed logs if connected to internet
        CheckInternet checkInternet = new CheckInternet(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                DbHelper db = new DbHelper(getApplicationContext());
                final String TABLE_NAME = "over_speed";

                List<OverSpeedLog> overSpeedLogList = db.fetchAll(TABLE_NAME);

                for (OverSpeedLog overSpeedLog: overSpeedLogList) {
                    Map<String, String> data = new HashMap<>();

                    Log.d("DebugLog", "OVER SPEED TABLE: SPEED-> " + overSpeedLog.getSpeed() + " TIME_STAMP-> " + overSpeedLog.getTimeStamp());

                    data.put("resp", "1");
                    data.put("main", "trip");
                    data.put("sub", "over_speed");
                    data.put("mode", "resend");
                    data.put("speed", overSpeedLog.getSpeed());
                    data.put("trip_id", overSpeedLog.getTripId());
                    data.put("time_stamp", overSpeedLog.getTimeStamp());
                    data.put("employee_id", "1");

                    new BackgroundHttpRequest(null).okHttpRequest(getApplicationContext(), data, "GET", TABLE_NAME);
                }
                db.deleteAll(TABLE_NAME);
            }
        });
        checkInternet.newCall();

        SharedPref driverPref = new SharedPref(this, "loginSession");
        Intent intent = new Intent(MainActivity.this , LoginActivity.class);
        if (driverPref.isContain("token")) {
            intent = new Intent(MainActivity.this, DashboardActivity.class);
        }
        final Intent finalIntent = intent;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(finalIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 1000);
    }
}
