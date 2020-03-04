package com.example.fmuv_driver.model.pojo;

import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class Seat {
    private TextView txtSeat;
    private LatLng pickUpLatLng;
    private String status;
    private String seatNo;

    // SETTERS
    public void setPickUpLatLng(LatLng pickUpLatLng) {
        this.pickUpLatLng = pickUpLatLng;
    }

    public void setTxtSeat(TextView txtSeat) {
        this.txtSeat = txtSeat;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }
    // GETTERS
    public LatLng getPickUpLatLng() {
        return pickUpLatLng;
    }

    public TextView getTxtSeat() {
        return txtSeat;
    }

    public String getStatus() {
        return status;
    }

    public String getSeatNo() {
        return seatNo;
    }

}
