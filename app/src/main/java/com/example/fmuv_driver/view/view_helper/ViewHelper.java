package com.example.fmuv_driver.view.view_helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;

import java.util.Timer;
import java.util.TimerTask;

public class ViewHelper {

    public AlertDialog progressDialog;
    private Context context;

    public AlertDialog connectLoading;

    public ViewHelper(Context context) {
        this.context = context;
        intiConnectLoading();
    }

    private void intiConnectLoading() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.connect_dialog, null);
        alertBuilder.setView(view);
        connectLoading = alertBuilder.create();
    }

    public void showProgressDialog(String msg, String title) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        TextView txtProgressMsg = view.findViewById(R.id.txtProgressMsg);
        txtProgressMsg.setText(msg);
        if (title == null) {
            title = "";
        }
        alertBuilder.setCancelable(false)
                .setView(view)
                .setTitle(title);
        progressDialog = alertBuilder.create();
        progressDialog.show();
    }

    public void showSuccessDialog(String msg, final boolean finish, final Activity activity) {
        final AlertDialog successDialog;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.success_dialog, null);
        TextView txtMsg = view.findViewById(R.id.txtMsg);
        txtMsg.setText(msg);
        alertBuilder.setCancelable(false)
                .setView(view);
        successDialog = alertBuilder.create();
        successDialog.show();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                successDialog.dismiss();
                if (finish) {
                    activity.finish();
                }
            }
        };
        timer.schedule(timerTask, 2000);
    }

    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public void showMessage(String title, String msg) {
        if (title.trim().equals("") || title == null) {
            title = "";
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null);
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void exitActivity(final Activity activity, String title, String msg) {
        if (title.trim().equals("") || title == null) {
            title = "";
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.onBackPressed();
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public boolean checkGpsPermission(final Activity activity) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle("Device location is off")
                    .setCancelable(false)
                    .setMessage("You cannot use this service if device location is off. Please go to settings and turn on device location.") // Want to enable?
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitApplicationDialog("Location Disabled", "Location service has been denied. Application will exit.");
                        }
                    })
                    .show();
            return false;
        } else {
            return true;
        }
    }

    public void sessionExpiredDialog(final Intent intent, final Activity activity) {

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle("Session Expired")
                .setMessage("Your session has expired. Please login to continue.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SharedPref(context, "loginSession").clearSharedPref();
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        activity.finish();
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void exitApplicationDialog(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void finishActivity(final Activity activity, String title, String msg) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
}
