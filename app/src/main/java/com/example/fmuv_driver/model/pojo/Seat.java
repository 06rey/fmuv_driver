package com.example.fmuv_driver.model.pojo;

import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Seat {
    private TextView txtSeat;
    private LatLng pickUpLatLng;
    private String status;
    private String seatNo;
    private String markerTitle;
    private Marker marker;
    private String BookingId;
    private String contactNo;

    // SETTERS


    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public void setBookingId(String bookingId) {
        BookingId = bookingId;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

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


    public String getContactNo() {
        return contactNo;
    }

    public String getBookingId() {
        return BookingId;
    }

    public Marker getMarker() {
        return marker;
    }

    public String getMarkerTitle() {
        return markerTitle;
    }

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
