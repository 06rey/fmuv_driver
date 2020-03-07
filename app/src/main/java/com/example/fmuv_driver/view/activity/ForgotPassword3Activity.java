package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgotPassword3Activity extends AppCompatActivity {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private Button btnCont;
    private EditText txtNew, txtConfirm;
    private TextView txtErrorMsg;
    private ProgressBar loadingBar;
    private String user_id;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password3);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        viewHelper = new ViewHelper(this);
        user_id = getIntent().getStringExtra("user_id");
        initView();
        setViewModelObserver();
        activity = this;
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------------------- VIEW INIT --------------------------------------
    //----------------------------------------------------------------------------------------------
    private void initView() {
        txtNew = findViewById(R.id.txtNew);
        txtConfirm = findViewById(R.id.txtConfrim);
        btnCont = findViewById(R.id.btnCont);
        txtErrorMsg = findViewById(R.id.txtErrorMsg);
        loadingBar = findViewById(R.id.loadingBar);
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePassword()) {
                    changePassword();
                }
            }
        });
    }

    private boolean validatePassword() {
        txtErrorMsg.setVisibility(View.INVISIBLE);
        loadingBar.setVisibility(View.VISIBLE);
        if (!txtNew.getText().toString().trim().equals("") || !txtConfirm.getText().toString().trim().equals("")) {
            if (txtNew.getText().toString().trim().length() >= 6) {
                if (txtNew.getText().toString().trim().equals(txtConfirm.getText().toString().trim())) {
                    return true;
                } else {
                    showError("Password does not match. Try again.");
                }
            } else {
                showError("Password must be at least 6 character long.");
            }
        } else {
            showError("New password and confirm password cannot be empty.");
        }
        return false;
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------------------- NETWORK CALL -----------------------------------
    //----------------------------------------------------------------------------------------------

    private void changePassword() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "account");
        data.put("sub", "change_forgot_password");
        data.put("pass1", txtNew.getText().toString().trim());
        data.put("user_id", user_id);

        viewModel.okHttpRequest(data, "GET", "");
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- VIEW MODEL OBSERVER -----------------------------------
    //----------------------------------------------------------------------------------------------

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String type = list.get(0).get("type");
                String status = list.get(0).get("status");

                if (type.equals("change_password")) {
                    if (status.equals("success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MyAlertDialogStyle);
                        builder.setTitle("Success")
                                .setMessage("You have successfully change your account password. Use your new password to login to your account.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ForgotPassword3Activity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        showError("Something went wrong. Try again.");
                    }
                }
                loadingBar.setVisibility(View.INVISIBLE);
            }
        });

        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                showError("Sorry, something went wrong there.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                showError("Service is not available at the moment.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                showError("Sorry, something went wrong there.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                showError("Incorrect code. Try again.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                showError("Sorry, something went wrong there.");
            }
        });

    }

    private void showError(String msg) {
        txtErrorMsg.setText(msg);
        txtErrorMsg.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.INVISIBLE);
        btnCont.setEnabled(true);
    }

}
