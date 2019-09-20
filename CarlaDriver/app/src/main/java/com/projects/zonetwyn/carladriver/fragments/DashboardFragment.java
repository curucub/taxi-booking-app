package com.projects.zonetwyn.carladriver.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Point;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context context;

    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap map;

    private Marker currentMarker;
    private Location currentLocation;

    private SessionManager sessionManager;

    private boolean shown = false;

    private Handler postHandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Location location = sessionManager.getLocation();
            if (location != null) {
                shown = true;
                handleLocation(location);
            }

            if (!shown) {
                postHandler.postDelayed(runnable, 2500);
            }
        }
    };

    private SupportMapFragment mapFragment;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    public DashboardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        context = getContext();
        sessionManager = SessionManager.getInstance(context);

        buildGoogleApiClient();
        createLocationRequest();

        //Init map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }
        try {
            mapFragment.getMapAsync(this);
        } catch (Exception e) {}

        subscribeToBus();
        /*Driver driver = sessionManager.getLoggedDriver();
        driver.setOnline(false);
        sessionManager.updateDriver(driver);
        Toast.makeText(context, "Message : " + String.valueOf(sessionManager.getLoggedDriver().isOnline()), Toast.LENGTH_LONG).show();*/

        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                postHandler.post(runnable);
            }
        }, 2500);*/

        return rootView;
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
    }

    private void handleLocation(Location location) {
        displayLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        if (currentLocation == null) {
            final Driver driver = sessionManager.getLoggedDriver();
            DatabaseReference drivers = FirebaseDatabase.getInstance().getReference("drivers");
            driver.setOnline(true);
            drivers.child(driver.getUid())
                    .setValue(driver)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sessionManager.updateDriver(driver);
                            //showToast(getString(R.string.you_are_online));
                        }
                    });
        }
        currentLocation = location;
    }

    private void subscribeToBus() {
        EventBus.subscribe(EventBus.SUBJECT_DASHBOARD, this, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (o instanceof Event) {
                    Event event = (Event) o;
                    switch (event.getSubject()) {
                        case Event.SUBJECT_DASHBOARD_LOCATION:
                            Location location = (Location) event.getData();
                            displayLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                            if (currentLocation == null) {
                                final Driver driver = sessionManager.getLoggedDriver();
                                DatabaseReference drivers = FirebaseDatabase.getInstance().getReference("drivers");
                                driver.setOnline(true);
                                drivers.child(driver.getUid())
                                        .setValue(driver)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                sessionManager.updateDriver(driver);
                                                //showToast(getString(R.string.you_are_online));
                                            }
                                        });
                            }
                            currentLocation = location;
                            break;
                    }
                }
            }
        });
    }

    private void displayLocation(LatLng latLng) {
        if (map != null) {
            if (currentMarker != null)
                currentMarker.remove();

            currentMarker = map.addMarker(new MarkerOptions()
                    .title("You")
                    .position(latLng)
                    .icon(vectorToBitmap(R.drawable.ic_place, getResources().getColor(R.color.colorPrimary))));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);

        } else {
            //Toast.makeText(context, "Map is still null!", Toast.LENGTH_SHORT).show();
        }
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.unregister(this);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient !=null && googleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            handleLocation(location);
        }
    }
}
