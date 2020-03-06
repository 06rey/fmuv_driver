package com.example.fmuv_driver.utility;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class AppUtil {

    public float getDistance(Location location1, Location location2) {
        // return in km
        return location1.distanceTo(location2)/1000;
    }

    public float computeDistance(LatLng uvLatLng, LatLng tempLatLng, PolylineOptions polylineOptions) {
        List<LatLng> latLngList = polylineOptions.getPoints();
        Location prevLocation = new Location("");
        Location uvLocation = new Location("");
        Location tempLocation = new Location("");

        float uvToTempDistance = 0;

        uvLocation.setLatitude(uvLatLng.latitude);
        uvLocation.setLongitude(uvLatLng.longitude);
        tempLocation.setLatitude(tempLatLng.latitude);
        tempLocation.setLongitude(tempLatLng.longitude);

        boolean isUvLocationFound = false;
        boolean isTempLocationFound = false;

        for (LatLng latLng: latLngList) {
            Location currentLocation = new Location("");
            currentLocation.setLatitude(latLng.latitude);
            currentLocation.setLongitude(latLng.longitude);

            float uvDistance = uvLocation.distanceTo(currentLocation);
            float tempDistance = tempLocation.distanceTo(currentLocation);

            if (uvDistance < 70) {
                isUvLocationFound =  true;
            }

            if (tempDistance < 70) {
                if ((!isUvLocationFound) && (isTempLocationFound)) {
                    return 0;
                }
                isTempLocationFound = true;
            }

            if (isUvLocationFound) {
                if (prevLocation != null) {
                    uvToTempDistance += prevLocation.distanceTo(currentLocation);
                }
                if (isTempLocationFound) {
                    return uvToTempDistance;
                }
            }

            prevLocation.setLatitude(latLng.latitude);
            prevLocation.setLongitude(latLng.longitude);
        }

        return uvToTempDistance;
    }
}
