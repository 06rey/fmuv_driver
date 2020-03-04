package com.example.fmuv_driver.model;

import android.app.Activity;
import android.util.Log;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.utility.GetPostUrlMaker;
import com.example.fmuv_driver.utility.ResponseBodyJsonArrayParser;
import com.example.fmuv_driver.view_model.AppViewModel;
import com.star_zero.sse.EventHandler;
import com.star_zero.sse.EventSource;
import com.star_zero.sse.MessageEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ServerEventModel implements Callback, EventHandler {

    private AppViewModel viewModel;
    private Activity activity;
    private EventSource eventSource;
    private SharedPref driverPref;

    public ServerEventModel(AppViewModel viewModel, Activity activity) {
        this.viewModel = viewModel;
        this.activity = activity;
        driverPref = new SharedPref(activity.getApplicationContext(), "loginSession");
    }
    // SERVER SENT
    public String createGetUrl(Activity activity, Map<String, String> attr, String param) {
        String url;
        url = activity.getResources().getString(R.string.BASE_URL);
        url = url + "?ref=2&resp=1&tk=" + activity.getResources().getString(R.string.tk);
        for(String key: attr.keySet()) {
            url = url + "&" + key + "=" + attr.get(key);
        }
        String token = driverPref.getUserToken();
        url = url + param + "&token=" + token;
        return url;
    }

    public void serverSentEvent(Map<String, String> mapParam, String param) {
        mapParam.put("event", "server_event");
        String URL = createGetUrl(activity, mapParam, param);
        Log.d("DebugLog", "SERVER EVENT URL: "+ URL +"&event=server_event");
        eventSource = new EventSource(URL, this);
        eventSource.connect();
    }

    public void closeConnection() {
        eventSource.close();
    }

    @Override
    public void onOpen() {
        //Log.d("DebugLog", "ServerEventModel->onOpen:");
    }

    @Override
    public void onMessage(@NonNull MessageEvent event) {

        Log.d("DebugLog", "ServerEventModel->SSE->onMessage: " + event.getData());
        try {
            JSONObject obj = new JSONObject(event.getData());
            JSONObject data_obj = obj.getJSONObject("DATA");
            List<Map<String, String>> list = new ResponseBodyJsonArrayParser().parseJsonArray(data_obj.getJSONArray("body"), viewModel);
            viewModel.setServerSentData(list);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DebugLog", "ServerEventModel->SSE->onMessage: JSON ERROR " + e.getMessage());
        }
    }

    @Override
    public void onError(@Nullable Exception e) {
        //Log.d("DebugLog", "ServerEventModel->onError: " + e.getMessage());
    }

    public void initRequest(Map<String, String> mapData, String method, String param) {
        GetPostUrlMaker http = new GetPostUrlMaker(activity, method, mapData, param, driverPref.getValue("token"));
        http.client.newCall(http.request).enqueue(this);
    }

    // OKHTTP
    @Override
    public void onFailure(Call call, IOException e) {
        viewModel.setSseConnectionError();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String res = response.body().string();
        jsonParser(res);

        Log.d("DebugLog", "ServerEventModel->Okhttp->onResponse: " + res);
    }

    // OKHHTP RESPONSE PARSER

    public void jsonParser(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if ((response.contains("DATA") && response.contains("200"))) {
                JSONObject data_obj = obj.getJSONObject("DATA");
                List<Map<String, String>> list = new ResponseBodyJsonArrayParser().parseJsonArray(data_obj.getJSONArray("body"), viewModel);
                if (!list.get(0).containsKey("json_error")) {
                    viewModel.setServerSentInitData(list);
                } else {
                    viewModel.setSseInitJsonError();
                }
            } else if ((response.contains("DATA") && response.contains("404"))) {
                viewModel.setSseInitDataError();
            } else if ((response.contains("SERVICE") && response.contains("503"))) {
                viewModel.setSseServiceError();
            } else if ((response.contains("STATUS") && response.contains("422"))) {
                viewModel.setSseInitStatusError();
            } else {
                viewModel.setSseInitJsonError();
                Log.d("DebugLog", "WRONG JSON PATTERN: ");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            viewModel.setSseInitJsonError();
            Log.d("DebugLog", "ServerEventModel->jsonParser: Error parsing json-> " + e.getMessage());
        }
    }



}
