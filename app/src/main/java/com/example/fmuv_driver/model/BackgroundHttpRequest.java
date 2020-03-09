package com.example.fmuv_driver.model;

import android.content.Context;
import android.util.Log;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.handler.ServiceServerEventResponseHandler;
import com.example.fmuv_driver.model.database.DbHelper;
import com.star_zero.sse.EventHandler;
import com.star_zero.sse.EventSource;
import com.star_zero.sse.MessageEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackgroundHttpRequest implements Callback, EventHandler {

    private String url;
    private OkHttpClient client;
    private FormBody.Builder formBuilder;
    private RequestBody form;
    private okhttp3.Request request;
    private Context context;
    private DbHelper db;
    private Map<String, String> data;
    private String mode;
    private static final String TABLE_NAME = "over_speed";

    private EventSource eventSource;
    private ServiceServerEventResponseHandler serviceServerEventResponseHandler;

    public BackgroundHttpRequest(ServiceServerEventResponseHandler serviceServerEventResponseHandler) {
        this.serviceServerEventResponseHandler = serviceServerEventResponseHandler;
    }

    public void serverSentEvent(Context context, Map<String, String> data, String mode) {
        Log.d("DebugLog", "Background service event source has initiated!");
        String token = new SharedPref(context, "loginSession").getValue("token");
        String url;
        this.mode = mode;
        url = context.getResources().getString(R.string.BASE_URL);
        url = url + "?ref=2&resp=1&event=server_event&tk=" + context.getResources().getString(R.string.tk) + "&token=" + token;
        for(String key: data.keySet()) {
            url = url + "&" + key + "=" + data.get(key);
        }
        Log.d("DebugLog", "Background service event source URL: ---- " + url);
        eventSource = new EventSource(url, this);
        eventSource.connect();
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onMessage(@NonNull MessageEvent event) {
        Log.d("DebugLog", "Background service event source onMessage" + event.getData());
        String response = event.getData();
        parseJson(response);
    }

    @Override
    public void onError(@Nullable Exception e) {

    }

    public void closeServentSentEvent() {
        eventSource.close();
    }


    public void okHttpRequest(Context context, Map<String, String> data, String method, String mode) {
        db = new DbHelper(context);
        this.data = data;
        this.mode = mode;
        this.context = context;

        String token = new SharedPref(context, "loginSession").getValue("token");
        String appId = context.getResources().getString(R.string.tk);
        url = context.getResources().getString(R.string.BASE_URL);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        if (method.equals("POST")) {
            createPostUrl(token, appId);
        } else {
            createGetUrl(token, appId);
        }

        client.newCall(request).enqueue(this);
    }

    private void createPostUrl(String token, String appId) {
        formBuilder = new FormBody.Builder();
        formBuilder.add("token", token);
        formBuilder.add("ref", "2");
        formBuilder.add("tk", appId);
        formBuilder.add("event", "normal");

        for(String key: data.keySet()) {
            formBuilder.add(key, data.get(key));
        }
        form = formBuilder.build();
        request = new Request.Builder()
                .url(url)
                .post(form)
                .build();
    }

    private void createGetUrl(String token, String appId) {
        url = url + "?ref=2&tk=" + appId;
        url = url + "&event=normal&token="+token;
        for(String key: data.keySet()) {
            url = url + "&" + key + "=" + data.get(key);
        }
        request = new Request.Builder()
                .url(url)
                .build();
        Log.d("DebugLog", "GetPostUrlMaker->GET: " + url);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (mode.equals("over_speed")) {
            if (data.get("mode").equals("new")) {
                Map<String, String> values = new HashMap<>();
                values.put("speed", data.get("speed"));
                values.put("trip_id", data.get("trip_id"));
                values.put("employee_id", "1");
                db.insert(TABLE_NAME, values);
            }
        }

        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> res = new HashMap<>();
        res.put("status", "failed");
        list.add(res);
        serviceServerEventResponseHandler.setHttpResponse(list);
    }

    @Override
    public void onResponse(Call call, Response res) throws IOException {
        String response = res.body().string();

        Log.d("DebugLog", "RESPONSE: " + response);
        parseJson(response);

        Log.d("DebugLog", "Over speed log has successfully send to server.");
    }

    private void parseJson(String response) {
        if ((response.contains("DATA") && response.contains("200"))) {
            try {
                JSONObject obj = new JSONObject(response);
                JSONObject obj_data = obj.getJSONObject("DATA");
                JSONArray arr = obj_data.getJSONArray("body");
                JSONObject data_obj = arr.getJSONObject(0);
                String mode = data_obj.getString("mode");

                int arrLen = arr.length();
                List<Map<String, String>> list = new ArrayList<>();
                for(int a=0; a<arrLen; a++) {
                    int len = data_obj.names().length();
                    Map<String, String> value = new HashMap<>();
                    for (int x=0; x<len; x++) {
                        String key = data_obj.names().getString(x);
                        if (key.equals("pick_up_loc")) {
                            JSONObject jsonLatlng = new JSONObject(data_obj.getString(key));
                            value.put("pick_lat", jsonLatlng.getString("lat"));
                            value.put("pick_lng", jsonLatlng.getString("lng"));
                        } else {
                            value.put(key, data_obj.getString(key));
                        }
                    }
                    list.add(value);
                }

                if (mode.equals("resend")) {
                    resendOverSpeed(data_obj);
                } else {
                    serviceServerEventResponseHandler.setHttpResponse(list);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("DebugLog", "JSONException Message: " + e.getMessage());
            }
        }
    }

    private void resendOverSpeed(JSONObject data_obj) {
        Map<String, String> data = new HashMap<>();
        try {
            data.put("trip_id", data_obj.getString("trip_id"));
            data.put("time_stamp", data_obj.getString("time_stamp"));
            db.deleteRow(TABLE_NAME, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
