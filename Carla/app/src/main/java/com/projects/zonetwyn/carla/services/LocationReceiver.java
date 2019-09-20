package com.projects.zonetwyn.carla.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;

import java.util.List;

public class LocationReceiver extends BroadcastReceiver {

    public static final String ACTION_LOCATION_UPDATE = "ACTION LOCATION UPDATE";
    public static final String UPDATED_LOCATION = "UPDATED LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            if (locationResult != null) {
                List<Location> locations = locationResult.getLocations();
                Location location = locations.get(0);
                Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show();
                Intent locationIntent = new Intent(ACTION_LOCATION_UPDATE);
                intent.putExtra(UPDATED_LOCATION, new Gson().toJson(location));
                context.sendBroadcast(locationIntent);
            } else {
                Toast.makeText(context, null, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, null, Toast.LENGTH_SHORT).show();
        }
    }
}
