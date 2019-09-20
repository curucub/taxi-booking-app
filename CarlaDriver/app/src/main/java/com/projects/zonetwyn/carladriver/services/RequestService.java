package com.projects.zonetwyn.carladriver.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.activities.RequestActivity;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Client;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Point;
import com.projects.zonetwyn.carladriver.models.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestService extends Service {

    private FirebaseDatabase database;
    private DatabaseReference requests;
    private DatabaseReference points;
    private DatabaseReference clients;

    private SessionManager sessionManager;
    private Driver driver;
    private boolean isRiding = false;

    public RequestService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = SessionManager.getInstance(getApplicationContext());
        driver = sessionManager.getLoggedDriver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("request", false)) {
                database = FirebaseDatabase.getInstance();
                requests = database.getReference("requests");
                isRiding = (sessionManager.getCurrentRide() != null && sessionManager.getCurrentRide().getPrice() > 0);

                points = database.getReference("points");
                clients = database.getReference("clients");

                Query query = requests.orderByChild("driverUid").equalTo(driver.getUid());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Request> requests = new ArrayList<>();
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            Request request = requestSnapshot.getValue(Request.class);
                            if (request != null) {
                                if (request.getStatus().equals("IN_WAITING")) {
                                    requests.add(request);
                                }
                            }
                        }

                        if (!requests.isEmpty()) {
                            final Request request = requests.get(requests.size() - 1);
                            Query clientQuery = clients.orderByChild("uid").equalTo(request.getClientUid());
                            final Query startingPointQuery = points.orderByChild("uid").equalTo(request.getStartingPointUid());
                            final Query arrivalPointQuery = points.orderByChild("uid").equalTo(request.getArrivalPointUid());

                            clientQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Client client = null;
                                    for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                                        client = clientSnapshot.getValue(Client.class);
                                    }
                                    if (client != null) {
                                        //Toast.makeText(getApplicationContext(), new Gson().toJson(client), Toast.LENGTH_LONG).show();
                                        request.setClient(client);
                                        startingPointQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Point startingPoint = null;
                                                for (DataSnapshot startingPointSnapshot : dataSnapshot.getChildren()) {
                                                    startingPoint = startingPointSnapshot.getValue(Point.class);
                                                }
                                                if (startingPoint != null) {
                                                    request.setStartingPoint(startingPoint);
                                                    arrivalPointQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            Point arrivalPoint = null;
                                                            for (DataSnapshot arrivalPointSnapshot : dataSnapshot.getChildren()) {
                                                                arrivalPoint = arrivalPointSnapshot.getValue(Point.class);
                                                            }
                                                            if (arrivalPoint!= null) {
                                                                request.setArrivalPoint(arrivalPoint);
                                                                launchRequestActivity(request);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else if (intent.getBooleanExtra("remove", false)){
                stopSelf();
            }
        }

        return START_STICKY;
    }

    private void launchRequestActivity(Request request) {
        Intent intent = new Intent(getApplicationContext(), RequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("REQUEST", request);
        startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
