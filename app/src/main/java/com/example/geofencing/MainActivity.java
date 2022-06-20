package com.example.geofencing;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GoogleMap mMap;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    LatLng position;
    String naslov;
    String radij;
    float radij1;
    int s;
    TextView alarm;
    TextView m;
    TextView start;
    Switch stikalo;
    Button delete;
    ConstraintLayout nov_alarm;
    SharedPreferences sp;
    View fragment;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    PendingIntent pendingIntent;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        //Za vklop in izklop geofenca
        geofenceHelper = new GeofenceHelper(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        sp = getApplicationContext().getSharedPreferences("Alarm", Context.MODE_PRIVATE);
        naslov = sp.getString("naslov", "");
        radij = sp.getString("radij", "");
        s = sp.getInt("true", 0);


        alarm = findViewById(R.id.alarm);
        m = findViewById(R.id.m);
        stikalo = findViewById(R.id.stikalo);
        nov_alarm = findViewById(R.id.nov_alarm);
        fragment = findViewById(R.id.map1);
        delete = findViewById(R.id.delete);
        start = findViewById(R.id.start);

        nov_alarm.setVisibility(View.GONE);
        alarm.setVisibility(View.GONE);
        m.setVisibility(View.GONE);
        stikalo.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        //fragment.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
        start.setText("Ni dodanih alarmov ...");

        SharedPreferences.Editor editor = sp.edit();
        stikalo.setChecked(sp.getBoolean("isChecked", true));
        stikalo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                if(isChecked) {
                    editor.putBoolean("isChecked", true);
                    //fragment.setVisibility(View.VISIBLE);
                    if(s != 0) {
                        float new_longitude = sp.getFloat("longitude", 15f);
                        float new_latitude = sp.getFloat("latitude", 15f);
                        position = new LatLng(new_latitude, new_longitude);
                        radij1 = sp.getFloat("radij1", 15f);
                        addCircle(position, radij1);
                        addMarker(position);
                        addGeofence(position,radij1);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Geofence ne obstaja", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    editor.putBoolean("isChecked", false);
                    removeGeofence();
                    mMap.clear();
                }
                editor.commit();
            }
        });

        if(s != 0) {
            start.setVisibility(View.GONE);
            nov_alarm.setVisibility(View.VISIBLE);
            alarm.setVisibility(View.VISIBLE);
            alarm.setText(naslov.toUpperCase());

            m.setVisibility(View.VISIBLE);
            m.setText(radij + "m");

            stikalo.setVisibility(View.VISIBLE);
            fragment.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }
        else {
            start.setVisibility(View.VISIBLE);
            start.setText("Ni dodanih alarmov ...");
        }
        sp = getApplicationContext().getSharedPreferences("Alarm", Context.MODE_PRIVATE);
        s = sp.getInt("true", 0);

        nov_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            SharedPreferences.Editor editor = sp.edit();
            s = 1;
            editor.putInt("true", s);
            editor.commit();
            startActivity(intent);
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        if(s != 0) {
            sp = getApplicationContext().getSharedPreferences("Alarm", Context.MODE_PRIVATE);
            float new_longitude = sp.getFloat("longitude", 15f);
            float new_latitude = sp.getFloat("latitude", 15f);
            position = new LatLng(new_latitude, new_longitude);
            radij1 = sp.getFloat("radij1", 15f);
            addMarker(position);
            addCircle(position, radij1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));
        }
        else{
            LatLng eiffel = new LatLng(46.045202, 14.489885);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 14));
        }
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0,0));
        circleOptions.fillColor(Color.argb(64, 255, 0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    public void dodaj(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        s=0;
        editor.putInt("true", s);
        editor.commit();
        startActivity(intent);
    }

    public void izbrisi(View view) {
        nov_alarm.setVisibility(View.GONE);
        alarm.setVisibility(View.GONE);
        //fragment.setVisibility(View.GONE);
        alarm.setText("Ni dodanih alarmov ...");
        m.setVisibility(View.GONE);
        stikalo.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
        start.setText("Ni dodanih alarmov ...");
        removeGeofence();
        mMap.clear();
        Toast.makeText(MainActivity.this, "Alarm izbrisan", Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = sp.edit();

        editor.clear();
        editor.commit();
        s=0;
        editor.putInt("true", s);
        editor.commit();
    }
    @Override
    public void onBackPressed() {
        // make sure you have this outcommented
        // super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void removeGeofence(){
        geofencingClient.removeGeofences(geofenceHelper.getPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }
    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }
}