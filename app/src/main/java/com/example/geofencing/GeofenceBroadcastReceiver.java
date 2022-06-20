package com.example.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.Locale;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }
//        Location location = geofencingEvent.getTriggeringLocation();
        int transitionType = geofencingEvent.getGeofenceTransition();
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences("Alarm", Context.MODE_PRIVATE);
        String naslov = sp.getString("naslov", "");

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context,naslov.toUpperCase(Locale.ROOT) +" gEOFENCE_TRANSITION_ENTER".toLowerCase(), Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification(naslov.toUpperCase(Locale.ROOT), "GEOFENCE_TRANSITION_ENTER".toLowerCase(Locale.ROOT), MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, naslov.toUpperCase(Locale.ROOT)+" gEOFENCE_TRANSITION_DWELL ".toLowerCase(Locale.ROOT), Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification(naslov.toUpperCase(Locale.ROOT), "GEOFENCE_TRANSITION_DWELL".toLowerCase(Locale.ROOT), MapsActivity.class);
                break;
        }

    }
}
