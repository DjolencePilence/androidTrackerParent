package com.example.djole.parrentapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private Button searchButton, buttonStop, buttonDraw, buttonClear, buttonShowChild, buttonStart;
    private ArrayList<Marker> markerPoints;
    private boolean buttonPressed = false;
    private Polygon polygon;
    private HashSet<Marker> markersSet;
    private LatLng belgrade;
    public static Context context;
    private ArrayList<LatLng> llList;
    public static MediaPlayer mp;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");
    private String latitudeString, longitudeString;
    private Marker childMarker = null, centerMarker = null;

    //importing circle
    private Circle circle;
    private LatLng childLatLng;
    private Switch switchOnOff, switchTrackAChild;
    private PendingIntent pendingIntent;
    private boolean circleDrawn = false;
    private int radius = 0;
    private LatLng circleCenter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = this;
        mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        /*markerPoints = new ArrayList<>();
        llList = new ArrayList<>();
        markersSet = new HashSet<>();*/
        //SEARCH
        searchButton = (Button) findViewById(R.id.btnSearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    geoLocate();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {/*
                if(circle != null){
                    //circle.remove();
                    circle.setVisible(false);
                }*/
                if (mp != null) {
                    if (mp.isPlaying()) {
                        mp.stop();
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                switchOnOff.setChecked(false);
            }
        });
        //DRAW
        buttonDraw = findViewById(R.id.buttonDraw);
        buttonDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleDrawn = drawCircle();
            }
        });
        //CLEAR
        buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearMap();
                if(pendingIntent != null) {
                   pendingIntent.cancel();
                   pendingIntent = null;
                }
                circleDrawn = false;
            }
        });
        //SWITCH_TRAKING
        switchTrackAChild = findViewById(R.id.switchTrackAChild);
        switchTrackAChild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(belgrade, 16f));
                }
            }
        });

        //SWITCH_ON_OFF
        switchOnOff = findViewById(R.id.switchOnOff);
        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    circleDrawn = drawCircle();
                    switchOnOff.setChecked(circleDrawn);
                    if(circleDrawn) setTheAlarmIfNeeded();
                }else{
                    if(pendingIntent != null) {
                        pendingIntent.cancel();
                        pendingIntent = null;
                    }
                }
            }
        });


        belgrade = new LatLng(44.8247142, 20.3996922);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {

                String latlngString = snap.getValue(String.class);
                int index = latlngString.indexOf(',');
                latitudeString = latlngString.substring(0, index);
                longitudeString = latlngString.substring(index + 1, latlngString.length());
                //System.out.println(latitudeString + " " + longitudeString);
                belgrade = new LatLng(Double.parseDouble(latitudeString), Double.parseDouble(longitudeString));
                if (childMarker != null) {
                    childMarker.remove();
                    childMarker = mMap.addMarker(new MarkerOptions().position(belgrade).title("Child").
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    childMarker.showInfoWindow();
                    Switch switchTrackAChild = findViewById(R.id.switchTrackAChild);
                    if(switchTrackAChild.isChecked())
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(belgrade, 16f));
                }
                setTheAlarmIfNeeded();
            }

            @Override
            public void onCancelled(DatabaseError de) {
                return;
            }
        });
    }
    ///NOV



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
        childMarker = mMap.addMarker(new MarkerOptions().position(belgrade).title("Child").
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        childMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(belgrade, 16f));

        //App is started again after back was pressed
        loadPreferences();


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                if(centerMarker != null) centerMarker.remove();
                centerMarker = mMap.addMarker(options);
                childMarker.showInfoWindow();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.hashCode() != childMarker.hashCode()) {
                    marker.remove();
                    centerMarker = null;
                }
                childMarker.showInfoWindow();
                return true;
            }
        });
    }


    public static void moveToLocationZoom(double lat, double lng, float zoom) {
        LatLng location = new LatLng(lat, lng);
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(location, zoom);
        mMap.moveCamera(camera);
    }

    public void setTheAlarmIfNeeded(){
        if(switchOnOff.isChecked() && !isInsideOfCircle(centerMarker.getPosition())) {
            Intent intent = new Intent(context, Alarm.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager a = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            a.set(AlarmManager.RTC, System.currentTimeMillis() + 10, pendingIntent);
        }
    }
    public void geoLocate() throws IOException {
        EditText destination = (EditText) findViewById(R.id.txtLocation);

        String location = destination.getText().toString();

        Geocoder geocoder = new Geocoder(this);

        List<Address> addresses = geocoder.getFromLocationName(location, 1);
        if(addresses.size() == 0) {
            Toast.makeText(context, "No such a place", Toast.LENGTH_LONG).show();
            return;
        }
        Address address = addresses.get(0);
        String locality = address.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double latitude = address.getLatitude();
        double longitude = address.getLongitude();

        moveToLocationZoom(latitude, longitude, 16);

    }

    public boolean isInsideOfCircle(LatLng point){
        float[] results = new float[1];
        Location.distanceBetween(childMarker.getPosition().latitude, childMarker.getPosition().longitude,
                point.latitude, point.longitude,
                results);
        return results[0] <= circle.getRadius();
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    // returns true if circle is drawn
    public boolean drawCircle(){
        if(centerMarker == null){
            Toast.makeText(context,"Please select the center on the map",Toast.LENGTH_LONG).show();
            return false;
        }
        EditText radiusEditText = findViewById(R.id.txtRadius);
        String radiusString = radiusEditText.getText().toString();
        if(!isNumeric(radiusString)) {
            Toast.makeText(context,"Enter radius in meters",Toast.LENGTH_LONG).show();
            return false;
        }
        radius = Integer.parseInt(radiusString);
        if(circle != null) circle.remove();
        circle = mMap.addCircle(new CircleOptions()
                .center(centerMarker.getPosition())
                .radius(radius)
                .fillColor(Color.GREEN)
                .strokeColor(Color.GREEN));
        circleCenter = centerMarker.getPosition();
        return true;
    }

    public void clearMap(){
        if(circle != null){
            circle.remove();
            circle = null;
        }
        if(centerMarker != null){
            centerMarker.remove();
            centerMarker = null;
        }
        switchOnOff.setChecked(false);
    }
    private void savePreferences(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear();
        editor.putBoolean("circleDrawn", circleDrawn);
        if(circleCenter != null){
            editor.putFloat("centerLatitude", (float) circleCenter.latitude);
            editor.putFloat("centerLongitude", (float) circleCenter.longitude);
        }
        editor.putBoolean("alarmSet", switchOnOff.isChecked());
        editor.putInt("radius", radius);
        editor.putBoolean("loadPref",true);
        editor.commit();

    }

    private void loadPreferences(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean loadPref = sharedPreferences.getBoolean("loadPref", false);
        if(loadPref) {
            float lat = sharedPreferences.getFloat("centerLatitude", 0);
            float lng = sharedPreferences.getFloat("centerLongitude", 0);
            boolean isCircleDrawn = sharedPreferences.getBoolean("circleDrawn", false);
            int radiusToRedraw = sharedPreferences.getInt("radius", 100);
            boolean isSwitchOnOffChecked = sharedPreferences.getBoolean("alarmSet", false);


            EditText radiusEditText = findViewById(R.id.txtRadius);
            radiusEditText.setText(radiusToRedraw + "");

            if (isCircleDrawn) {
                LatLng latLng = new LatLng(lat, lng);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                centerMarker = mMap.addMarker(options);

                switchOnOff = findViewById(R.id.switchOnOff);
                switchOnOff.setChecked(isSwitchOnOffChecked);
                if (!switchOnOff.isChecked()) {
                    drawCircle();
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("loadPref", false);
            editor.commit();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        savePreferences();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
