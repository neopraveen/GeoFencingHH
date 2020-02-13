package com.jms.geofencinghh.utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class PolygonUtils {
    public static double PI = 3.14159265;
    public static double TWO_PI = 2 * PI;
    private static PolygonUtils instance;

    public static PolygonUtils getInstance() {
        if (instance == null)
            instance = new PolygonUtils();
        return instance;
    }

    public boolean isLocationWithinArea(LatLng geoPoint, double accuracy, List<LatLng> geoFences) {
        //Checking if coordinate is inside the GeoFence.
        boolean isCoordinateInsideFence = coordinateIsInsidePolygon(geoPoint, geoFences);
        if (isCoordinateInsideFence) {
            return true;
        } else {
            //Coordinate is outside, now checking if distance is less than accuracy required or not.
            return checkForAccuracy(geoPoint, accuracy, geoFences);
        }
    }

    public boolean coordinateIsInsidePolygon(
            LatLng geoPoint, List<LatLng> geoFences) {
        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int n = geoFences.size();

        for (i = 0; i < n; i++) {
            point1_lat = geoFences.get(i).latitude - geoPoint.latitude;
            point1_long = geoFences.get(i).longitude - geoPoint.longitude;
            point2_lat = geoFences.get((i + 1) % n).latitude - geoPoint.latitude;
            point2_long = geoFences.get((i + 1) % n).longitude - geoPoint.longitude;
            angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
        }

        if (Math.abs(angle) < PI)
            return false;
        else
            return true;
    }

    private double Angle2D(double y1, double x1, double y2, double x2) {
        double dtheta, theta1, theta2;
        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI)
            dtheta -= TWO_PI;
        while (dtheta < -PI)
            dtheta += TWO_PI;
        return (dtheta);
    }

    public boolean checkForAccuracy(LatLng geoPoint, double accuracyInMeters, List<LatLng> geoFences) {
        if (geoPoint == null || geoFences == null) {
            return false;
        }

        for (int i = 0; i < geoFences.size(); i++) {
            LatLng point = geoFences.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= geoFences.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(geoPoint, point, geoFences.get(segmentPoint));
            if (currentDistance < accuracyInMeters) {
                return true;
            }
        }
        return false;
    }

    public double findMinimumDistance(LatLng geoPoint, List<LatLng> geoFences) {
        double distance = -1;
        if (geoPoint == null || geoFences == null) {
            return distance;
        }

        for (int i = 0; i < geoFences.size(); i++) {
            LatLng point = geoFences.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= geoFences.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(geoPoint, point, geoFences.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
            }
        }
        return distance;
    }

    //Function will be needed if we want to know the nearest GeoFence of the given point
    public LatLng findNearestPoint(LatLng geoPoint, List<LatLng> geoFences) {
        double distance = -1;
        LatLng minimumDistancePoint = geoPoint;

        if (geoPoint == null || geoFences == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < geoFences.size(); i++) {
            LatLng point = geoFences.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= geoFences.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(geoPoint, point, geoFences.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(geoPoint, point, geoFences.get(segmentPoint));
            }
            Log.e("GEOFENCE", ">>\t" + currentDistance);
        }
        return minimumDistancePoint;
    }

    private LatLng findNearestPoint(final LatLng geoPoint, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }
        final double s0lat = Math.toRadians(geoPoint.latitude);
        final double s0lng = Math.toRadians(geoPoint.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }
        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));
    }


    public boolean isValidGpsCoordinate(double latitude,
                                        double longitude) {
        //This is a function to reject invalid lat/longs.
        if (latitude > -90 && latitude < 90 &&
                longitude > -180 && longitude < 180) {
            return true;
        }
        return false;
    }
}