package com.projects.zonetwyn.carladriver.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.projects.zonetwyn.carladriver.activities.SplashActivity;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Rate;
import com.projects.zonetwyn.carladriver.models.Ride;

public class SessionManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public static final int PRIVATE_MODE = 0;
    public static final String PREFERENCE_FILE = "CARLA_DRIVER";

    private static final String KEY_DRIVER = "driver";
    private static final String KEY_LOGIN = "isLoggedIn";

    public static final String KEY_LOCATION = "location";
    public static final String KEY_ONLINE = "isOnline";

    public static final String KEY_RIDE = "ride";
    public static final String KEY_GPS_PREFERENCE = "KEY_GPS_PREFERENCE";
    private static String KEY_RATE = "RATE";

    private static final String KEY_LAST_LOCATION = "KEY_LOCATION";


    private static SessionManager sessionManager;

    public SessionManager(Context context) {

        this.context = context;
        preferences = context.getSharedPreferences(PREFERENCE_FILE, PRIVATE_MODE);
        editor = preferences.edit();
        editor.apply();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if(sessionManager == null) {
            sessionManager = new SessionManager(context);
        }

        return sessionManager;
    }

    public void updateRate(Rate rate) {
        String rateJson = new Gson().toJson(rate);

        editor.putString(KEY_RATE, rateJson);
        editor.commit();
    }

    public Rate getRate() {
        Rate rate = null;
        String rateJson = preferences.getString(KEY_RATE, "");
        if (!rateJson.isEmpty()) {
            rate = new Gson().fromJson(rateJson, Rate.class);
        }

        return rate;
    }

    public void updateRide(Ride ride) {
        String rideJson = new Gson().toJson(ride);
        editor.putString(KEY_RIDE, rideJson);
        editor.commit();
    }

    public Ride getCurrentRide() {
        String rideJson = preferences.getString(KEY_RIDE, "");
        Ride ride = null;
        if (rideJson != null && rideJson.length() > 10) {
            ride = new Gson().fromJson(rideJson, Ride.class);
        }
        return ride;
    }

    public void cleanRide() {
        if (preferences.contains(KEY_RIDE)) {
            editor.remove(KEY_RIDE);
            editor.commit();
        }
    }

    public void saveGpsPreference(String gpsPreference) {
        editor.putString(KEY_GPS_PREFERENCE, gpsPreference);
        editor.commit();
    }

    public String readGpsPreference() {
        return preferences.getString(KEY_GPS_PREFERENCE, "");
    }

    public void signInDriver(Driver driver) {
        String driverJson = new Gson().toJson(driver);

        editor.putBoolean(KEY_LOGIN, true);
        editor.putString(KEY_DRIVER, driverJson);
        editor.commit();
    }

    public void updateDriver(Driver driver) {
        String driverJson = new Gson().toJson(driver);

        editor.putString(KEY_DRIVER, driverJson);
        editor.commit();
    }

    public void updateLocation(Location location) {
        String locationJson = new Gson().toJson(location);

        editor.putString(KEY_LOCATION, locationJson);
        editor.commit();
    }

    public Location getLocation() {

        String locationJson = preferences.getString(KEY_LOCATION, null);
        if (locationJson != null) {
            Location location = new Gson().fromJson(locationJson, Location.class);
            return location;
        }
        return null;
    }

    public void updateOnlineStatus(boolean online) {
        Driver driver = getLoggedDriver();
        driver.setOnline(online);
        updateDriver(driver);
        editor.putBoolean(KEY_ONLINE, online);
        editor.commit();
    }

    public boolean isOnline() {
        return preferences.getBoolean(KEY_ONLINE, false);
    }

    public Driver getLoggedDriver() {
        Driver driver = null;
        String driverJson = preferences.getString(KEY_DRIVER, "");
        if (!driverJson.isEmpty()) {
            driver = new Gson().fromJson(driverJson, Driver.class);
        }

        return driver;
    }

    public void checkLoggedIn() {
        if (!isLoggedIn()) {
            Intent intent = new Intent(context, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void logout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth != null && auth.getCurrentUser() != null) {
            auth.signOut();
        }
        editor.clear();
        editor.commit();
        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGIN, false);
    }

}


