package com.jms.geofencinghh.model;

import com.google.android.gms.maps.model.LatLng;

public class LatLngModel {
    public double lat;
    public double lon;

    public LatLng getLatLng() {
        return new LatLng(lat, lon);
    }
}
