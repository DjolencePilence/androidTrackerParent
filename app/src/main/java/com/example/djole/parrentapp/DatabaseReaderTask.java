package com.example.djole.parrentapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Djole on 26-Dec-17.
 */



public class DatabaseReaderTask extends AsyncTask<DBNecessaryData, Void, Boolean> {

    private ArrayList<LatLng> markers;
    private LatLng belgrade;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference childLat;
    private DatabaseReference childLong;
    private DatabaseReference myRef = database.getReference("message");

    private String latlngString, latitudeString, longitudeString;


    private LatLng latitude, longitude;

    private Context context;

    public DatabaseReaderTask(Context context){
        this.context = context;/*
        childLat = database.getReference("https://locationproj-a0a94.firebaseio.com/latitude");
        childLat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                latitudeString  = snap.getValue(String.class);
            }
            @Override public void onCancelled(DatabaseError de) { return; }
        });
        childLong = database.getReference("https://locationproj-a0a94.firebaseio.com/longitude");
        childLong.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                longitudeString  = snap.getValue(String.class);
            }
            @Override public void onCancelled(DatabaseError de) { return; }
        });*/
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                latlngString  = snap.getValue(String.class);
                int index = latlngString.indexOf('-');
                if(index == 1 ) System.out.println("negativan je");
                latitudeString = latlngString.substring(0, index);
                longitudeString = latlngString.substring(index+1, latlngString.length());
                System.out.println(latitudeString +" " + longitudeString);
                belgrade = new LatLng(Double.parseDouble(latitudeString), Double.parseDouble(longitudeString));
            }
            @Override public void onCancelled(DatabaseError de) { return; }
        });
    }

    private boolean isPointInPolygon(LatLng tap, ArrayList<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return ((intersectCount % 2) == 1); // odd = inside, even = outside;
    }

    private boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
    }

    @Override
    protected Boolean doInBackground(DBNecessaryData... dbNecessaryData) {
        markers = dbNecessaryData[0].markers;
        belgrade = dbNecessaryData[0].belgrade;
        for(int j =0; j<1000;j++){
            try {
                belgrade = new LatLng(Double.parseDouble(latitudeString), Double.parseDouble(longitudeString));
            }
            catch (Exception e){ System.out.println("Greska u konverziji)");}
            if (isPointInPolygon(belgrade, markers))
                for (int i = 0; i <markers.size(); i++) {
                    if (markers.get(i).equals(belgrade)) {
                        try {
                            //Thread.sleep(5000);
                            return false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            else try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
       return true;
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Intent intent=new Intent(context, Alarm.class);
        PendingIntent p1=PendingIntent.getBroadcast(context,0, intent,0);
        AlarmManager a=(AlarmManager)context.getSystemService(ALARM_SERVICE);
        a.set(AlarmManager.RTC,System.currentTimeMillis() + 10,p1);

    }
}
