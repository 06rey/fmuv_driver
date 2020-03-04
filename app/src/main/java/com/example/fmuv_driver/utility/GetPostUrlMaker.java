package com.example.fmuv_driver.utility;

import android.app.Activity;
import android.util.Log;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GetPostUrlMaker {

    public String url;
    public OkHttpClient client;
    public FormBody.Builder formBuilder;
    public RequestBody form;
    public okhttp3.Request request;

    public GetPostUrlMaker(Activity activity, String method, Map<String, String> data, String param, String token) {

        url = activity.getResources().getString(R.string.BASE_URL);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        if (method.equals("POST")) {
            formBuilder = new FormBody.Builder();
            formBuilder.add("token", token);
            formBuilder.add("ref", "2");
            formBuilder.add("tk", activity.getResources().getString(R.string.tk));
            formBuilder.add("event", "normal");

            for(String key: data.keySet()) {
                formBuilder.add(key, data.get(key));
            }
            form = formBuilder.build();
            request = new Request.Builder()
                    .url(url)
                    .post(form)
                    .build();

        } else {
            url = url + "?ref=2&tk=" + activity.getResources().getString(R.string.tk);
            url = url + "&event=normal&token="+token;
            for(String key: data.keySet()) {
                url = url + "&" + key + "=" + data.get(key);
            }
            url = url + param;
            request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d("DebugLog", "GetPostUrlMaker->GET: " + url);
        }
    }
}
