package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgotPassword1Activity extends AppCompatActivity {

    private EditText txtContact;
    private Button btnSearch;
    private TextView txtErrorMsg;
    private ProgressBar loadingBar;

    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password1);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        setViewModelObserver();

        txtErrorMsg = findViewById(R.id.txtErrorMsg);
        txtContact = findViewById(R.id.editTxtContact);
        loadingBar = findViewById(R.id.loadingBar);
        btnSearch = findViewById(R.id.btnSearch);
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------- CONTACT NUMBER VALIDATION ----------------------------------
    //----------------------------------------------------------------------------------------------

    private boolean isContactIsValid() {
        if (txtContact.getText().toString().length() < 11 || txtContact.getText().toString().length() > 11) {
            txtContact.setError("Contact number must be 11 digit number");
            return false;
        } else if (txtContact.getText().toString().trim().equals("")) {
            txtContact.setError("Contact number cannot be empty");
            return false;
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(txtContact.getText().toString())) {
            txtContact.setError("Contact number is not valid");
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- CLICK LISTENER EVENT ----------------------------------
    //----------------------------------------------------------------------------------------------

    public void onClickBtnSearch(View view) {
        if (isContactIsValid()) {
            searchAccount();
            txtErrorMsg.setVisibility(View.INVISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        }
    }

    //----------------------------------------------------------------------------------------------
    //--------------------------------------------- NETWORK CALL -----------------------------------
    //----------------------------------------------------------------------------------------------

    private void searchAccount() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "account");
        data.put("sub", "search_account");
        data.put("contact", txtContact.getText().toString());

        viewModel.okHttpRequest(data, "GET", "");
    }

    //----------------------------------------------------------------------------------------------
    //-------------------------------------- VIEW MODEL OBSERVER -----------------------------------
    //----------------------------------------------------------------------------------------------

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                //String type = list.get(0).get("type");
                Intent intent = new Intent(ForgotPassword1Activity.this, ForgotPassword2Activity.class);
                intent.putExtra("id", list.get(0).get("id"));
                intent.putExtra("user_id", list.get(0).get("user_id"));
                intent.putExtra("contact", txtContact.getText().toString());
                loadingBar.setVisibility(View.INVISIBLE);
                startActivity(intent);
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
                showError("Contact number does not exists.");
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
        btnSearch.setEnabled(true);
    }

}
