package com.example.fmuv_driver.model;

import android.app.Activity;
import android.util.Log;

import com.example.fmuv_driver.utility.GetPostUrlMaker;
import com.example.fmuv_driver.utility.ResponseBodyJsonArrayParser;
import com.example.fmuv_driver.view_model.AppViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OkhttpModel implements Callback {

    private AppViewModel viewModel;
    private Activity activity;
    private SharedPref driverPref;

    public OkhttpModel(AppViewModel viewModel, Activity activity) {
        this.viewModel = viewModel;
        this.activity = activity;
        driverPref = new SharedPref(activity.getApplicationContext(), "loginSession");
    }

    public void okHttpRequest(Map<String, String> mapData, String method, String extraParam) {
        mapData.put("event", "normal");
        GetPostUrlMaker http = new GetPostUrlMaker(activity, method, mapData, extraParam, driverPref.getValue("token"));
        http.client.newCall(http.request).enqueue(this);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        viewModel.setOkhttpConnectionError();
        Log.d("DebugLog", "OkhttpModel->onFailure: "+e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response res) throws IOException {
        String response = res.body().string();

        Log.d("DebugLog", response);
        jsonParser(response);

        Log.d("DebugLog", "OkhttpModel->onResponse: " + response);
    }

    public void jsonParser(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if ((response.contains("DATA") && response.contains("200"))) {
                JSONObject data_obj = obj.getJSONObject("DATA");
                List<Map<String, String>> list = new ResponseBodyJsonArrayParser().parseJsonArray(data_obj.getJSONArray("body"), viewModel);
                if (!list.get(0).containsKey("json_error")) {
                    viewModel.setOkhttpData(list);
                } else {
                    viewModel.setOkhttpJsonError();
                }
            } else if ((response.contains("DATA") && response.contains("404"))) {
                viewModel.setOkhttpDataError();
            } else if ((response.contains("SERVICE") && response.contains("503"))) {
                viewModel.setOkHttpServiceError();
            } else if ((response.contains("STATUS") && response.contains("422"))) {
                viewModel.setOkhttpStatusError();
            } else if ((response.contains("TOKEN") && response.contains("0"))) {
                viewModel.setTokenError();
            } else {
                viewModel.setOkhttpJsonError();
                Log.d("DebugLog", "WRONG JSON PATTERN: ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            viewModel.setOkhttpJsonError();
            Log.d("DebugLog", "OkhttpModel->jsonParser: Error parsing json-> " + e.getMessage());
        }
    }
}
