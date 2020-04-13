package com.example.fmuv_driver.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private ViewHelper viewHelper;
    private AppViewModel viewModel;
    private Intent speedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        setViewModelObserver();
        // Init All
        initVar();
    }

    private void initVar() {
        viewHelper = new ViewHelper(this);
        checkLocationPermission();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(DashboardActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(DashboardActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return false;
        } else {
            return viewHelper.checkGpsPermission(this);
        }
    }

    public void logout(View view) {
        final Context context = this;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle("Logout")
                .setMessage("Are you sure you want to logout your account?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> data = new HashMap<>();
                        data.put("resp", "0");
                        data.put("main", "account");
                        data.put("sub", "logout_acc");
                        viewModel.okHttpRequest(data, "POST", "");

                        new SharedPref(context, "loginSession").clearSharedPref();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        }, 500);
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void accountClickListener(View view) {
        startActivity(new Intent(DashboardActivity.this, AccountActivity.class));
    }

    public void setViewModelObserver() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("DebugLog", "TOPPPP");
                    if (ContextCompat.checkSelfPermission(DashboardActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        viewHelper.checkGpsPermission(this);
                    }
                }else{
                    Log.d("DebugLog", "BOTOTMMMM");
                    viewHelper.exitApplicationDialog("Permission Denied","Location permission has been denied. Application will exit.");
                }
                return;
            }
        }
    }

    public void onclickTripManager(View view) {
        if (checkLocationPermission()) {
            startActivity(new Intent(DashboardActivity.this, TripManagerActivity.class));
        }
    }

    public void onClickTripHistory(View view) {
        startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
    }
}
