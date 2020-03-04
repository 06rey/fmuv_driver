package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private EditText txtAddress, txtContact, newPass, confPass, currPass, fname, lname, license;
    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private SharedPref driverPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        this.initialize();
    }

    private void initialize() {
        viewHelper = new ViewHelper(this);
        driverPref = new SharedPref(this, "driverPref");

        txtAddress = findViewById(R.id.address);
        txtContact = findViewById(R.id.contact);
        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        license = findViewById(R.id.license);

        newPass = findViewById(R.id.newPass);
        confPass = findViewById(R.id.confPass);
        currPass = findViewById(R.id.currPass);

        setViewModelObserver();

        loadProfile();
    }

    private void loadProfile() {
        viewHelper.showProgressDialog("Please wait ...", null);
        Map<String, String> data = new HashMap<>();
        data.put("main", "account");
        data.put("sub", "load_profile");
        data.put("resp", "1");
        viewModel.okHttpRequest(data, "GET", "");
    }

    public void updateClickListener(View view) {
        if (validateTxtField()) {
            Map<String, String> data = new HashMap<>();
            data.put("resp", "1");
            data.put("main", "account");
            data.put("sub", "update");
            data.put("contact", txtContact.getText().toString());
            data.put("address", txtAddress.getText().toString());

            viewModel.okHttpRequest(data, "POST", "");
        }
    }

    public void changePasswordClickListener(View view) {
        if (validatePasswordField()) {
            if (newPass.getText().toString().trim().length() > 5) {
                if (passwordMatch()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("resp", "1");
                    data.put("main", "account");
                    data.put("sub", "change_password");
                    data.put("current_pass", currPass.getText().toString());
                    data.put("pass1", newPass.getText().toString());

                    viewModel.okHttpRequest(data, "POST", "");

                    viewHelper.showProgressDialog("Please wait ...","");
                }
            } else {
                newPass.setError("Password must be 6 character long.");
            }
        }
    }

    private boolean validatePasswordField() {
        if (newPass.getText().toString().trim().equals("")) {
            newPass.setError("Cannot be empty.");
            return false;
        }
        if (confPass.getText().toString().trim().equals("")) {
            confPass.setError("Please confirm password.");
            return false;
        }
        if (currPass.getText().toString().trim().equals("")) {
            currPass.setError("Please enter current password.");
            return false;
        }
        return true;
    }

    private boolean validateTxtField() {

        if (txtAddress.getText().toString().trim().equals("")) {
            txtAddress.setError("Address cannot be empty");
            return false;
        }
        if (txtContact.getText().toString().trim().equals("")) {
            txtContact.setError("Contact number cannot be empty");
            return false;
        } else {
            if (txtContact.getText().toString().length() < 11 || txtContact.getText().toString().length() > 11) {
                txtContact.setError("Contact number must be 11 digit number");
                return false;
            }
        }

        return true;
    }

    private boolean passwordMatch() {
        if (newPass.getText().toString().trim().equals(confPass.getText().toString().trim())) {
            return true;
        }
        viewHelper.showMessage("Confirm Password","New password and confirm password does not match. Try again.");
        return false;
    }

    private void clearPasswordField() {
        newPass.setText("");
        confPass.setText("");
        currPass.setText("");
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String type = list.get(0).get("type");
                if (type.equals("update_info")) {
                    viewHelper.showMessage("Success", "Information successfully updated.");
                } else if (type.equals("change_password")) {
                    viewHelper.dismissProgressDialog();
                    if (list.get(0).get("status").equals("failed"))  {
                        viewHelper.showMessage("Change Password Fail", "Current password is incorrect. Try again.");
                    } else {
                        viewHelper.showMessage("Success", "Password successfully change.");
                        clearPasswordField();
                    }
                } else if (type.equals("profile")) {
                    fname.setText(list.get(0).get("f_name"));
                    lname.setText(list.get(0).get("l_name"));
                    license.setText(list.get(0).get("license_no"));
                    txtAddress.setText(list.get(0).get("address"));
                    txtContact.setText(list.get(0).get("contact_no"));
                    viewHelper.dismissProgressDialog();
                }
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Update Failed", "Something went wrong.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Update Failed", "This service is not available at the moment.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Update Failed", "Contact number already exists. Please try different contact number.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Update Failed", "Something went wrong.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.showMessage("Update Failed", "Something went wrong.");
            }
        });

        viewModel.getTokenError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissProgressDialog();
                viewHelper.sessionExpiredDialog(new Intent(AccountActivity.this, LoginActivity.class), AccountActivity.this);
            }
        });
    }
}
