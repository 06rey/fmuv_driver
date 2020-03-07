package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnLogin;
    private SharedPref driverPref;
    private ViewHelper viewHelper;
    private AppViewModel viewModel;
    private final static String NEW_LOGIN = "0";
    private final static String LOGOUT_FROM_OTHER_DEVICE = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        
        initialize();
    }

    private void initialize() {
        txtEmail = findViewById(R.id.etRegFname);
        txtPassword = findViewById(R.id.etUserPass);
        btnLogin = findViewById(R.id.btnLogin);

        setTxtListener();
        setBtnListener();

        viewHelper = new ViewHelper(this);
        driverPref = new SharedPref(this, "loginSession");

        setViewModelObserver();
    }

    private void setBtnListener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHelper.showProgressDialog("Logging In ...", null);
                Map<String, String> data = new HashMap<>();
                data.put("main", "account");
                data.put("resp", "1");
                data.put("mode", NEW_LOGIN);
                data.put("sub", "login");
                data.put("contact", txtEmail.getText().toString().trim());
                data.put("pass", txtPassword.getText().toString().trim());
                viewModel.okHttpRequest(data, "GET", "");
            }
        });
    }

    private void setTxtListener() {
        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtEmail.getText().toString().length() > 0 && txtPassword.getText().toString().length() > 0) {
                    btnLogin.setEnabled(true);
                    btnLogin.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    btnLogin.setEnabled(false);
                    btnLogin.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtEmail.getText().toString().length() > 0 && txtPassword.getText().toString().length() > 0) {
                    btnLogin.setEnabled(true);
                    btnLogin.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    btnLogin.setEnabled(false);
                    btnLogin.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void setViewModelObserver() {
        final Context mContext = this;
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String status = list.get(0).get("status");
                if (status.equals("failed")) {
                    viewHelper.dismissProgressDialog();
                    if (list.get(0).get("msg").equals("already login")) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
                        alertBuilder.setTitle("Already Login")
                                .setMessage("You already login from another device. If you wish to login to this device please logout from other device.")
                                .setPositiveButton("Logout from other device", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        viewHelper.showProgressDialog("Logging In ...", null);
                                        Map<String, String> data = new HashMap<>();
                                        data.put("main", "account");
                                        data.put("mode", LOGOUT_FROM_OTHER_DEVICE);
                                        data.put("resp", "1");
                                        data.put("sub", "login");
                                        data.put("contact", txtEmail.getText().toString().trim());
                                        data.put("pass", txtPassword.getText().toString().trim());
                                        viewModel.okHttpRequest(data, "GET", "");
                                    }
                                })
                        .setNegativeButton("Cancel", null);
                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }
                } else {
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    driverPref.setAll(list.get(1));
                    viewHelper.dismissProgressDialog();
                }
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Login Failed", "Something went wrong.");
                viewHelper.dismissProgressDialog();
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Login Failed", "This service is not available at the moment.");
                viewHelper.dismissProgressDialog();
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                String msg = txtEmail.getText().toString().trim() + " doesn't match an existing account.";
                viewHelper.showMessage("Can't Find Account", msg);
                viewHelper.dismissProgressDialog();
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Login Failed", "Incorrect password. Try again.");
                viewHelper.dismissProgressDialog();
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Login Failed", "Something went wrong.");
                viewHelper.dismissProgressDialog();
            }
        });
    }

    public void forgotPasswordClick(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPassword1Activity.class));
    }
}
