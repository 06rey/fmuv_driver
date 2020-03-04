package com.example.fmuv_driver.handler;

import java.util.List;
import java.util.Map;

public class ServiceServerEventResponseHandler {
    private ServiceServerEventResponseListener serviceServerEventResponseListener;

    public void setServiceHttpResponseListener(ServiceServerEventResponseListener serviceServerEventResponseListener) {
        this.serviceServerEventResponseListener = serviceServerEventResponseListener;
    }

    public void setHttpResponse(List<Map<String, String>> list) {
        if (serviceServerEventResponseListener != null) {
            serviceServerEventResponseListener.onServiceServerEventResponse(list);
        }
    }

    public interface ServiceServerEventResponseListener {
        void onServiceServerEventResponse(List<Map<String, String>> list);
    }

}
