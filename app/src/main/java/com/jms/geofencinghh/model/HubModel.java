package com.jms.geofencinghh.model;

import java.util.ArrayList;

public class HubModel {
    public String type;
    public long id;
    public Bounds bounds;
    public ArrayList<ArrayList<LatLngModel>> geometry;
    public Tag tags;

    public static class Bounds {
        public double minlat;
        public double minlon;
        public double maxlat;
        public double maxlon;
    }
    public static class Tag{
        public String name;
    }
}
