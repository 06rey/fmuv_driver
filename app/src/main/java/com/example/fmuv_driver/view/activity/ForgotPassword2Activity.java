package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgotPassword2Activity extends AppCompatActivity {

    private EditText txtCode;
    private Button btnNext, btnSendAgain;
    private TextView txtErrorMsg;
    private ProgressBar loadingBar;

    private AppViewModel viewModel;


    private String id, user_id, contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password2);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        setViewModelObserver();

        txtErrorMsg = findViewById(R.id.txtErrorMsg);
        txtCode = findViewById(R.id.editTxtCode);
        loadingBar = findViewById(R.id.loadingBar);
        btnNext = findViewById(R.id.btnNext);
        btnSendAgain = findViewById(R.id.btnSendAgain);

        setClickListener();

        id = getIntent().getStringExtra("id");
        user_id = getIntent().getStringExtra("user_id");

    }
    //----------------------------------------------------------------------------------------------
    //--------------------------------- CONTACT NUMBER VALIDATION ----------------------------------
    //----------------------------------------------------------------------------------------------

    private boolean isCodeIsValid() {
        if (txtCode.getText().toString().length() < 6 || txtCode.getText().toString().length() > 6) {
            txtCode.setError("Code is 6 digit number");
            return false;
        } else if (txtCode.getText().toString().trim().equals("")) {
            txtCode.setError("Code cannot be empty");
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- CLICK LISTENER EVENT ----------------------------------
    //----------------------------------------------------------------------------------------------

    private void setClickListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCodeIsValid()) {
                    verifyAccount();
                    txtErrorMsg.setVisibility(View.INVISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnNext.setEnabled(false);
                loadingBar.setVisibility(View.VISIBLE);
                sendCode();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------------------- NETWORK CALL -----------------------------------
    //----------------------------------------------------------------------------------------------

    private void verifyAccount() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "account");
        data.put("sub", "verify_account");
        data.put("id", id);
        data.put("user_id", user_id);
        data.put("code", txtCode.getText().toString());

        viewModel.okHttpRequest(data, "GET", "");
    }

    private void sendCode() {
        txtErrorMsg.setVisibility(View.INVISIBLE);
        txtCode.setText("");
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "account");
        data.put("sub", "send_code");
        data.put("user_id", user_id);
        data.put("contact", contact);
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
                if (type.equals("verify")) {
                    Intent intent = new Intent(ForgotPassword2Activity.this, ForgotPassword3Activity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                } else if (type.equals("send_code")) {
                    id = list.get(0).get("id");
                    user_id = list.get(0).get("user_id");
                    btnNext.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Code has bessn sent", Toast.LENGTH_SHORT).show();
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
        btnNext.setEnabled(true);
    }

}