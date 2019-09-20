package com.projects.zonetwyn.carla.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.activities.MainActivity;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Notification;

public class NotificationService extends Service {

    private FirebaseDatabase database;
    private DatabaseReference notifications;

    private SessionManager sessionManager;
    private Client client;

    private NotificationManager notificationManager;

    private static final String CHANNEL_ID = "carla-id-2";
    private static final String CHANNEL_NAME = "carla-channel-2";

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = SessionManager.getInstance(getApplicationContext());
        client = sessionManager.getLoggedClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("request", false)) {
                //Toast.makeText(this, "Notification service started!", Toast.LENGTH_SHORT).show();
                database = FirebaseDatabase.getInstance();
                notifications = database.getReference("notifications");

                Query query = notifications.orderByChild("userUid").equalTo(client.getUid());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                            if (notificationSnapshot != null) {
                                Notification notification = notificationSnapshot.getValue(Notification.class);
                                if (notification!= null && notification.getStatus().equals("IN_WAITING")) {
                                    if (notification.getType().equals(Notification.TYPE_RIDE_END)) {
                                        showNotification(notification, "ACTION_GO_TO_RIDES");
                                        //stopSelf();
                                    } else if (notification.getType().equals(Notification.TYPE_DRIVER_REACH)) {
                                        showNotification(notification, "ACTION_GO_TO_SEARCH");
                                    } else {
                                        showNotification(notification, "ACTION_GO_TO_REQUESTS");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else if (intent.getBooleanExtra("remove", false)) {
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private boolean checkVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void showNotification(Notification notification, String action) {

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(action, true);
        resultIntent.putExtra("NOTIFICATION_ID", notification);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (checkVersion()) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(this, CHANNEL_ID)
                    .setContentIntent(resultPendingIntent)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getContent())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);

            getNotificationManager().notify(1000, notificationBuilder.build());
        } else {
            android.app.Notification n  = new android.app.Notification.Builder(this)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getContent())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .build();


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(1000, n);
        }

        notification.setStatus("READ");
        notifications.child(notification.getUid())
                    .setValue(notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
