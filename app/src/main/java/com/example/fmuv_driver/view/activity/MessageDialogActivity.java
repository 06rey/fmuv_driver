package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fmuv_driver.R;

public class MessageDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_dialog);
        setTitle("Find Me UV Message");
        this.setFinishOnTouchOutside(false);
        initialize();
    }

    private void initialize() {
        TextView txtMsg = findViewById(R.id.txtMsg);
        Button btnOk = findViewById(R.id.btnOk);

        txtMsg.setText(getIntent().getStringExtra("msg"));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
