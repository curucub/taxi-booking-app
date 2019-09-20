package com.projects.zonetwyn.carla.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.projects.zonetwyn.carla.activities.SplashActivity;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Rate;

public class SessionManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final int PRIVATE_MODE = 0;
    private static final String PREFERENCE_FILE = "CARLA_CLIENT";

    private static final String KEY_CLIENT = "client";
    private static final String KEY_LOGIN = "isLoggedIn";

    private static SessionManager sessionManager;

    private static String KEY_MESSAGES_COUNT = "MESSAGES COUNT";
    
    private static String KEY_RATE = "RATE";

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

    public void updateMessagesCount(int count) {
        editor.putInt(KEY_MESSAGES_COUNT, count);
        editor.commit();
    }

    public int getMessagesCount() {
        return preferences.getInt(KEY_MESSAGES_COUNT, 0);
    }


    public void signInClient(Client client) {
        String clientJson = new Gson().toJson(client);

        editor.putBoolean(KEY_LOGIN, true);
        editor.putString(KEY_CLIENT, clientJson);
        editor.commit();
    }

    public void updateClient(Client client) {
        String clientJson = new Gson().toJson(client);

        editor.putString(KEY_CLIENT, clientJson);
        editor.commit();
    }

    public Client getLoggedClient() {
        Client client = null;
        String clientJson = preferences.getString(KEY_CLIENT, "");
        if (!clientJson.isEmpty()) {
            client = new Gson().fromJson(clientJson, Client.class);
        }

        return client;
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
