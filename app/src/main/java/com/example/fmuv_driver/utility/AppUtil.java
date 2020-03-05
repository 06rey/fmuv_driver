package com.example.fmuv_driver.utility;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class AppUtil {

    public float getDistance(Location location1, Location location2) {
        // return in km
        return location1.distanceTo(location2)/1000;
    }

    public void computeDistance(LatLng uvLatLng, LatLng tempLatLng, PolylineOptions polylineOptions) {

    }
}
