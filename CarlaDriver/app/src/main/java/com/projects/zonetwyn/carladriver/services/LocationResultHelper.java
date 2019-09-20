package com.projects.zonetwyn.carladriver.services;

import android.content.Context;
import android.location.Location;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;

import java.util.List;

public class LocationResultHelper {

    private Context context;

    private List<Location> locations;

    private SessionManager sessionManager;
    private Driver driver;

    LocationResultHelper(Context context, List<Location> locations) {
        this.context  = context;
        this.locations = locations;
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
    }

    public void saveResults() {
        if (locations != null && !locations.isEmpty()) {
            final Location location = locations.get(0);
            driver = sessionManager.getLoggedDriver();
            if (driver != null) {
                DatabaseReference driversAvailable = FirebaseDatabase.getInstance().getReference("drivers-available");
                GeoFire geoFire = new GeoFire(driversAvailable);
                geoFire.setLocation(driver.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        driver.setLatitude(location.getLatitude());
                        driver.setLongitude(location.getLongitude());
                        driver.setOnline(true);
                        sessionManager.updateDriver(driver);
                        sessionManager.updateLocation(location);
                    }
                });
            }
        }
    }
}
