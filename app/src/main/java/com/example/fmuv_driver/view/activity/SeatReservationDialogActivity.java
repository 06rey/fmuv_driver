package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SeatReservationDialogActivity extends AppCompatActivity {

    private int timerTime = 59;
    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private Activity activity;
    private String queueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_reservation_dialog);
        setTitle("Find Me UV");
        this.setFinishOnTouchOutside(false);

        initialize();
    }

    private void initialize() {
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        setViewModelObserver();
        activity = this;
        viewHelper = new ViewHelper(this);

        Intent intent = getIntent();
        TextView txtNoPass = findViewById(R.id.txtNoPass);
        txtNoPass.setText(intent.getStringExtra("no_of_pass"));
        queueId = intent.getStringExtra("queue_id");

        //timer();
    }

    private void timer() {
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (timerTime == 0) {
                    finish();
                }
                timerTime--;
                setTimerTime();
                timer();
            }
        };
        timer.schedule(timerTask, 1000);
    }

    private void confirmRequest() {
        viewHelper.showProgressDialog("Sending Confirmation...", "");
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "confirm_request");
        data.put("queue_id", queueId);

        viewModel.okHttpRequest(data, "GET", "");
    }

    private void setTimerTime() {
        final TextView txtTimer = findViewById(R.id.txtTimer);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtTimer.setText(String.valueOf(timerTime));
            }
        });
    }

    public void onBtnConfirmClick(View view) {
        confirmRequest();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                //String type = list.get(0).get("type");
                viewHelper.dismissProgressDialog();
                viewHelper.showSuccessDialog("Reservation Confirmed", true, activity);
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.finishActivity(activity, "Confirmation Failed", "Failed to confirm new reservation request. Connection ha timeout");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.finishActivity(activity, "Confirmation Failed", "Something went wrong.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.finishActivity(activity, "Confirmation Failed", "Something went wrong.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.finishActivity(activity, "Confirmation Failed", "Something went wrong.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.finishActivity(activity, "Confirmation Failed", "Something went wrong.");
            }
        });

        viewModel.getTokenError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.sessionExpiredDialog(new Intent(SeatReservationDialogActivity.this, LoginActivity.class), SeatReservationDialogActivity.this);
            }
        });
    }
}
