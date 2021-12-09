package com.example.workbuddy;

import org.osmdroid.util.GeoPoint;

public class Utils {
    public static GeoPoint lerp(GeoPoint point1, GeoPoint point2, double t) {
        final double dlat = point2.getLatitude() - point1.getLatitude();
        final double dlong = point2.getLongitude() - point1.getLongitude();
        return new GeoPoint(point1.getLatitude() + dlat * t, point1.getLongitude() + dlong * t);
    }
}
