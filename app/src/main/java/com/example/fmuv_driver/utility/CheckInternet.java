package com.example.fmuv_driver.utility;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CheckInternet {

    private OkHttpClient okHttpClient;
    private okhttp3.Request request;
    private Callback callback;

    public CheckInternet(Callback callback) {

        String url = "https://www.google.com";
        okHttpClient = new OkHttpClient.Builder()
                .build();
        request = new Request.Builder()
                .url(url)
                .build();
        this.callback = callback;
    }

    public void newCall() {
        okHttpClient.newCall(request).enqueue(callback);
    }
}
