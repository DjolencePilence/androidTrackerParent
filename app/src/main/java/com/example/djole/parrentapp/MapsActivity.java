package com.example.djole.parrentapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private Button searchButton, buttonStop;
    private ArrayList<Marker> markerPoints;
    private boolean buttonPressed = false;
    private Polygon polygon;
    private HashSet<Marker> markersSet;
    private LatLng belgrade;
    public static Context context;
    private ArrayList<LatLng> llList;
    public static MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = this;
        mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        markerPoints = new ArrayList<>();
        llList = new ArrayList<>();
        markersSet = new HashSet<>();
        searchButton = (Button) findViewById(R.id.btnSearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markerPoints.size() > 2) {
                    buttonPressed = true;
                    if (isPointInPolygon(belgrade, markerPoints)) {
                        drawPolygon();
                        new DatabaseReaderTask(context).execute(new DBNecessaryData(belgrade, llList));
                    }
                }

                /*String location = destination.getText().toString();
                String link = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=F6:5D:82:EF:6F:9A:49:14:5C:B7:30:8A:7A:CF:49:0A:02:DD:72:22";

                GetLocationDownloadTask getLocation = new GetLocationDownloadTask();

                getLocation.execute(link);*/

            }
        });
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mp != null) {
                    if(mp.isPlaying()) {
                        mp.stop();
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
    ///NOV

    public class DatabaseReaderTask2 extends AsyncTask<DBNecessaryData, Void, Boolean> {

        private ArrayList<LatLng> markers;
        private LatLng belgrade;

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
            for (int j = 0; j < 1000; j++) {
                if (isPointInPolygon(belgrade, markers))
                    for (int i = 0; i < markers.size(); i++) {
                        if (markers.get(i).equals(belgrade)) {
                            try {
                                Thread.sleep(5000);
                                return false;
                            } catch (InterruptedException e) {
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
            super.onPostExecute(aBoolean);/*
            Intent intent=new Intent(MapsActivity.this, Alarm.class);
            PendingIntent p1=PendingIntent.getBroadcast(getApplicationContext(),0, intent,0);
            AlarmManager a=(AlarmManager)getSystemService(ALARM_SERVICE);
            a.set(AlarmManager.RTC,System.currentTimeMillis() + 7000,p1);*/
            mp = MediaPlayer.create(MapsActivity.this, Settings.System.DEFAULT_RINGTONE_URI);
            try {
                mp.setLooping(true);
                mp.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        belgrade = new LatLng(44.8247142, 20.3996922);
        mMap.addMarker(new MarkerOptions().position(belgrade).title("Marker in Belgrade"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(belgrade, 17.5f));
        moveToLocationZoom(belgrade.latitude, belgrade.longitude, 17.5f);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (buttonPressed) {
                    markerPoints.clear();
                    markersSet.clear();
                    mMap.clear();
                    belgrade = new LatLng(44.8247142, 20.3996922);
                    mMap.addMarker(new MarkerOptions().position(belgrade).title("Marker in Belgrade"));
                    buttonPressed = false;
                    if (polygon != null)
                        polygon.remove();
                }

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng);

                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                // Add new marker to the Google Map Android API V2
                Marker marker = mMap.addMarker(options);
                markerPoints.add(marker);
                markersSet.add(marker);
                llList.add(marker.getPosition());


            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (markersSet.contains(marker)) {
                    for (int i = 0; i < markerPoints.size(); i++)
                        if (markerPoints.get(i).equals(marker)) break;
                    markerPoints.remove(marker);
                    markersSet.remove(marker);
                    llList.remove(marker.getPosition());
                    marker.remove();
                }
                return true;
            }
        });
    }

    private void drawPolygon() {
        PolygonOptions polygonOptions = new PolygonOptions().fillColor(0x33000fff)
                .strokeWidth(3)
                .strokeColor(Color.RED);

        for (int i = 0; i < markerPoints.size(); i++)
            polygonOptions.add(markerPoints.get(i).getPosition());
        polygon = mMap.addPolygon(polygonOptions);

    }

    //Check whether point lies inside a polygon
    private boolean isPointInPolygon(LatLng tap, ArrayList<Marker> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j).getPosition(), vertices.get(j + 1).getPosition())) {
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


    public static void moveToLocationZoom(double lat, double lng, float zoom) {
        LatLng location = new LatLng(lat, lng);
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(camera);
    }


    public void geoLocate(View view) throws IOException {
        EditText destination = (EditText) findViewById(R.id.editText);

        String location = destination.getText().toString();

        Geocoder geocoder = new Geocoder(this);

        List<Address> addresses = geocoder.getFromLocationName(location, 1);
        Address address = addresses.get(0);

        String locality = address.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double latitude = address.getLatitude();
        double longitude = address.getLongitude();

        moveToLocationZoom(latitude, longitude, 15);

    }


}
