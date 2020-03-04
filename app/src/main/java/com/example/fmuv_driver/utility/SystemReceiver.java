package com.example.fmuv_driver.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.fmuv_driver.service.Speedometer;

public class SystemReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, Speedometer.class));
    }
}
