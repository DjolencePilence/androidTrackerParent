package com.example.djole.parrentapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by Djole on 26-Dec-17.
 */

public class DBNecessaryData {
    public ArrayList<LatLng> markers;
    public LatLng belgrade;

    public DBNecessaryData(LatLng b, ArrayList<LatLng> m) {
        markers = m;
        belgrade = b;
    }
}
