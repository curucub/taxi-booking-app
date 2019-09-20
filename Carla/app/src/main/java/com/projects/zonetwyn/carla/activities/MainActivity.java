package com.projects.zonetwyn.carla.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.components.CircularImageView;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.fragments.AboutFragment;
import com.projects.zonetwyn.carla.fragments.BillsFragment;
import com.projects.zonetwyn.carla.fragments.MessagesFragment;
import com.projects.zonetwyn.carla.fragments.PaymentFragment;
import com.projects.zonetwyn.carla.fragments.ProfileFragment;
import com.projects.zonetwyn.carla.fragments.RequestsFragment;
import com.projects.zonetwyn.carla.fragments.RidesFragment;
import com.projects.zonetwyn.carla.fragments.SearchFragment;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Message;
import com.projects.zonetwyn.carla.models.Notification;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.models.Rate;
import com.projects.zonetwyn.carla.services.NotificationService;
import com.projects.zonetwyn.carla.utils.Event;
import com.projects.zonetwyn.carla.utils.EventBus;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private CircularImageView picture;
    private TextView username;

    private Toolbar toolbar;
    private TextView toolbarTitle;

    //For navigation
    private int navigationIndex = 0;
    private List<String> titles;

    private TextView messagesCount;

    private SessionManager sessionManager;

    private LatLng lastLocation;

    private FirebaseDatabase database;
    private DatabaseReference notifications;
    private DatabaseReference messages;
    private DatabaseReference rates;

    private static final int REQUEST_CHECK_SETTINGS = 0x5;
    private static final int PERMISSION_REQUEST_CODE = 7000;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private Client client;

    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().matches(BROADCAST_ACTION)) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                createLocationRequest();
                            }
                        }, 10);

                    }

                }
            }
        }
    };


    public MainActivity() {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        //Init Calligraphy
        /*CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Medium.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/

        setContentView(R.layout.activity_main);

        sessionManager = SessionManager.getInstance(getApplicationContext());
        sessionManager.checkLoggedIn();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getResources().getString(R.string.app_name));

        drawerLayout = findViewById(R.id.drawer_layout);
        setupDrawerLayout();

        navigationView = findViewById(R.id.nav_view);
        View navigationHeader = navigationView.getHeaderView(0);
        username = navigationHeader.findViewById(R.id.header_username);
        picture = navigationHeader.findViewById(R.id.header_picture);

        setupNavigationView();

        //For navigation
        titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.search));
        titles.add(getResources().getString(R.string.rides));
//        titles.add(getResources().getString(R.string.requests));
        titles.add(getResources().getString(R.string.payment));
        titles.add(getResources().getString(R.string.bills));
        titles.add(getResources().getString(R.string.messages));
        titles.add(getResources().getString(R.string.profile));
        titles.add(getResources().getString(R.string.about));
        titles.add(getResources().getString(R.string.share));
        titles.add(getResources().getString(R.string.logout));

        //Initialize Menu

        //Init Messages TextView
        messagesCount = (TextView) navigationView.getMenu().findItem(R.id.nav_messages).getActionView();
        initMessagesTextView();

        database = FirebaseDatabase.getInstance();
        notifications = database.getReference("notifications");

        //Init Nav Header
        if (sessionManager.isLoggedIn()) {
            messages = database.getReference("messages");
            rates = database.getReference("rates");

            fetchData();
            updateUI();
            startNotificationService();
            if (intent != null) {
                if (intent.hasExtra("ACTION_GO_TO_REQUESTS") && intent.getBooleanExtra("ACTION_GO_TO_REQUESTS", true)) {
                    Notification notification = intent.getParcelableExtra("NOTIFICATION_ID");
                    notification.setStatus("READ");
                    notifications.child(notification.getUid())
                            .setValue(notification)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadFragment(2);
                                }
                            });
                } else if (intent.hasExtra("ACTION_GO_TO_RIDES") && intent.getBooleanExtra("ACTION_GO_TO_RIDES", true)) {
                    Notification notification = intent.getParcelableExtra("NOTIFICATION_ID");
                    notification.setStatus("READ");
                    notifications.child(notification.getUid())
                            .setValue(notification)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadFragment(1);
                                }
                            });
                } else if (intent.hasExtra("ACTION_GO_TO_SEARCH") && intent.getBooleanExtra("ACTION_GO_TO_SEARCH", true)) {
                    Notification notification = intent.getParcelableExtra("NOTIFICATION_ID");
                    notification.setStatus("READ");
                    notifications.child(notification.getUid())
                            .setValue(notification)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadFragment(0);
                                }
                            });
                } else {
                    loadFragment(0);
                }
            } else {
                loadFragment(0);
            }

            setupLocation();
        }
    }

    private void fetchData() {
        client = sessionManager.getLoggedClient();
        messages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messageList = new ArrayList<>();
                for (DataSnapshot messageSnap : dataSnapshot.getChildren()) {
                    Message message = messageSnap.getValue(Message.class);
                    if (message != null) {
                        if (message.getTarget().equals("CLIENTS")
                                || message.getTarget().equals("USERS")
                                || (message.getTarget().equals("CLIENT") && message.getClientUid().equals(client.getUid()))) {
                            messageList.add(message);
                        }
                    }
                }
                sessionManager.updateMessagesCount(messageList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        rates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Rate rate = null;
                for (DataSnapshot rateSnap: dataSnapshot.getChildren()) {
                    rate = rateSnap.getValue(Rate.class);
                    if (rate != null) {
                    } else {
                        showToast("NULL RATE");
                    }
                }
                if (rate != null) {
                    sessionManager.updateRate(rate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gpsLocationReceiver != null)
            unregisterReceiver(gpsLocationReceiver);
    }

    private void startNotificationService() {
        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("request", true);
        startService(intent);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //showToast("Location settings are good");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {

                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void initMessagesTextView() {
        messagesCount.setGravity(Gravity.CENTER_VERTICAL);
        messagesCount.setTypeface(null, Typeface.BOLD);
        messagesCount.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private void updateUI() {
        Client client = sessionManager.getLoggedClient();
        Picasso.get().load(client.getPictureUrl()).error(R.drawable.rhino).into(picture);
        String usernameText = client.getSurname() + " " + client.getName();
        username.setText(usernameText);
    }


    private void setupDrawerLayout() {
        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);

                        onNavigationItemClicked(menuItem);

                        return true;
                    }
                });
    }

    private void onNavigationItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                loadFragment(0);
                break;
            case R.id.nav_rides:
                loadFragment(1);
                break;
//            case R.id.nav_requests:
//                loadFragment(2);
//                break;
            case R.id.nav_payment:
                loadFragment(3);
                break;
            case R.id.nav_bills:
                loadFragment(4);
                break;
            case R.id.nav_messages:
                loadFragment(5);
                break;
            case R.id.nav_profile:
                loadFragment(6);
                break;
            case R.id.nav_about:
                loadFragment(7);
                break;
            case R.id.nav_share:
                sharedContent();
                break;
            case R.id.nav_logout:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                }
                sessionManager.logout();
                break;
            default:
                break;
        }
    }

    private void sharedContent() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Informations about carla");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

    private void loadFragment(int position) {
        Fragment fragment = null;
        String title = titles.get(position);
        navigationIndex = position;
        switch (position) {
            case 0:
                fragment = new SearchFragment();
                break;
            case 1:
                fragment = new RidesFragment();
                break;
//            case 2:
//                fragment = new RequestsFragment();
//                break;
            case 3:
                fragment = new PaymentFragment();
                break;
            case 4:
                fragment = new BillsFragment();
                break;
            case 5:
                fragment = new MessagesFragment();
                break;
            case 6:
                fragment = new ProfileFragment();
                break;
            case 7:
                fragment = new AboutFragment();
                break;
        }

        try {
            getSupportFragmentManager().popBackStack();
        } catch (Exception e) { }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
            toolbarTitle.setText(title);

            //Check navigation item
            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.getItem(position);
            menuItem.setChecked(true);

            //Uncheck others
            for (int i=0; i<menu.size(); i++) {
                if (i != position) {
                    MenuItem item = menu.getItem(i);
                    item.setChecked(false);
                }
            }


            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            }
        }
    }

    @Override
    protected void onRestart() {
        if (navigationIndex == 3) {
            loadFragment(0);
        }
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                updateDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDrawer() {
        messagesCount.setText(String.valueOf(sessionManager.getMessagesCount()));
    }

    private void killProcess() {

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        //Process.killProcess(Process.myPid());
    }

    @Override
    public void onBackPressed() {
        killProcess();
    }

    private void sendLocation() {
        if (lastLocation != null) {
            Event event = new Event(Event.SUBJECT_SEARCH_LOCATION, lastLocation);
            EventBus.publish(EventBus.SUBJECT_SEARCH, event);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchFragment.PICK_FROM_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Point point = data.getParcelableExtra("PLACE_RESULT");
                Event event = new Event(Event.SUBJECT_SEARCH_FROM, point);
                EventBus.publish(EventBus.SUBJECT_SEARCH, event);
            }
        } else if (requestCode == SearchFragment.PICK_TO_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Point point = data.getParcelableExtra("PLACE_RESULT");
                Event event = new Event(Event.SUBJECT_SEARCH_TO, point);
                EventBus.publish(EventBus.SUBJECT_SEARCH, event);
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                buildGoogleApiClient();
                createLocationRequest();
            } else {
                Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_CODE);
        } else {
            buildGoogleApiClient();
            createLocationRequest();
        }
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /*private void createLocationRequest() {
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);
    }*/

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));
        if (googleApiClient != null && googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
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
            lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
            sendLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    createLocationRequest();
                }
        }
    }
}
