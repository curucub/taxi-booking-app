package com.projects.zonetwyn.carladriver.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.components.CircularImageView;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.fragments.AboutFragment;
import com.projects.zonetwyn.carladriver.fragments.DashboardFragment;
import com.projects.zonetwyn.carladriver.fragments.EarningsFragment;
import com.projects.zonetwyn.carladriver.fragments.MessagesFragment;
import com.projects.zonetwyn.carladriver.fragments.PaymentsFragment;
import com.projects.zonetwyn.carladriver.fragments.ProfileFragment;
import com.projects.zonetwyn.carladriver.fragments.RequestFragment;
import com.projects.zonetwyn.carladriver.fragments.RidesFragment;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Payment;
import com.projects.zonetwyn.carladriver.models.Rate;
import com.projects.zonetwyn.carladriver.services.LocationService;
import com.projects.zonetwyn.carladriver.services.RequestService;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private CircularImageView picture;
    private TextView username;
    private TextView todayEarnings;
    private TextView weekEarnings;
    private Switch schOnline;

    private Toolbar toolbar;
    private TextView toolbarTitle;

    //For navigation
    private int navigationIndex = 0;
    private List<String> titles;

    private TextView messagesCount;

    private SessionManager sessionManager;

    private FirebaseDatabase database;
    private DatabaseReference drivers;
    private DatabaseReference payments;
    private DatabaseReference rates;

    private Driver driver;

    private boolean shown;

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
        todayEarnings = navigationHeader.findViewById(R.id.header_today);
        weekEarnings = navigationHeader.findViewById(R.id.header_week);
        schOnline = navigationHeader.findViewById(R.id.schOnline);

        setupNavigationView();

        //For navigation
        titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.dashboard));
        titles.add(getResources().getString(R.string.rides));
        titles.add(getResources().getString(R.string.request));
        titles.add(getResources().getString(R.string.earnings));
        titles.add(getResources().getString(R.string.payments));
        titles.add(getResources().getString(R.string.messages));
        titles.add(getResources().getString(R.string.profile));
        titles.add(getResources().getString(R.string.about));
        titles.add(getResources().getString(R.string.logout));
        messagesCount = (TextView) navigationView.getMenu().findItem(R.id.nav_messages).getActionView();

        database = FirebaseDatabase.getInstance();
        drivers = database.getReference("drivers");
        payments = database.getReference("payments");
        rates = database.getReference("rates");

        //Init Nav Header
        if (sessionManager.isLoggedIn()) {
            driver = sessionManager.getLoggedDriver();
            //Init Messages TextView
            //initMessagesTextView();
            handleEvents();
            updateUI();
            updateGains();
            //Initialize Menu
            if (intent != null) {
                if (intent.hasExtra("ACTION_GO_TO_RIDE") && intent.getBooleanExtra("ACTION_GO_TO_RIDE", false)) {
                    loadFragment(2);
                } else {
                    loadFragment(0);
                }
            } else {
                loadFragment(0);
            }
        }
    }

    private void updateGains() {
        Query query = payments.orderByChild("driverUid").equalTo(driver.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Payment> paymentList = new ArrayList<>();
                for (DataSnapshot paymentSnap: dataSnapshot.getChildren()) {
                    Payment payment = paymentSnap.getValue(Payment.class);
                    paymentList.add(payment);
                }
                if (paymentList.isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.MONDAY) {
                        driver.setEarningsWeekly(0);
                    }
                    driver.setEarningsDaily(0);
                    drivers.child(driver.getUid())
                            .setValue(driver)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    sessionManager.updateDriver(driver);
                                }
                            });
                } else {
                    Payment currentPayment = null;
                    for (Payment payment : paymentList) {
                        String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                        if (payment.getCreatedAt().contains(currentDate)) {
                            currentPayment = payment;
                        }
                    }
                    if (currentPayment != null) {
                        driver.setEarningsDaily(currentPayment.getAmount());
                        sessionManager.updateDriver(driver);
                    } else {
                        driver.setEarningsDaily(0);
                        sessionManager.updateDriver(driver);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void handleEvents() {
        schOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handleAvailable();
                } else {
                    handleUnavailable();
                }
            }
        });
    }

    private void handleAvailable() {
        startLocationUpdates();
    }

    private void handleUnavailable() {
        stopLocationUpdates();
        driver.setOnline(false);
        driver.setLongitude(0);
        driver.setLatitude(0);
        drivers.child(driver.getUid())
                .setValue(driver)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DatabaseReference driversAvailable = database.getReference("drivers-available");
                        GeoFire geoFire = new GeoFire(driversAvailable);
                        driversAvailable.child(driver.getUid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                sessionManager.updateDriver(driver);
                                                //showToast(getString(R.string.you_are_offline));
                                            }
                                        });
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        startService(new Intent(this, LocationService.class).putExtra("request", true));
        startService(new Intent(this, RequestService.class).putExtra("request", true));
    }

    private void stopLocationUpdates() {
        startService(new Intent(this, LocationService.class).putExtra("remove", true));
        startService(new Intent(this, RequestService.class).putExtra("remove", true));
    }

    private void initMessagesTextView() {
        messagesCount.setGravity(Gravity.CENTER_VERTICAL);
        messagesCount.setTypeface(null, Typeface.BOLD);
        messagesCount.setTextColor(getResources().getColor(R.color.colorAccent));
        messagesCount.setText("99+");
    }

    private void updateUI() {
        Driver driver = sessionManager.getLoggedDriver();
        Picasso.get().load(driver.getPictureUrl()).error(R.drawable.rhino).into(picture);
        String usernameText = driver.getSurname() + " " + driver.getName();
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
            case R.id.nav_dashboard:
                loadFragment(0);
                break;
            case R.id.nav_rides:
                loadFragment(1);
                break;
            case R.id.nav_request:
                loadFragment(2);
                break;
            case R.id.nav_earnings:
                loadFragment(3);
                break;
            case R.id.nav_payments:
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
            case R.id.nav_logout:
                sessionManager.logout();
                break;
            default:
                break;
        }

    }

    private void loadFragment(int position) {
        Fragment fragment = null;
        String title = titles.get(position);
        navigationIndex = position;
        switch (position) {
            case 0:
                fragment = new DashboardFragment();
                break;
            case 1:
                fragment = new RidesFragment();
                break;
            case 2:
                fragment = new RequestFragment();
                break;
            case 3:
                fragment = new EarningsFragment();
                break;
            case 4:
                fragment = new PaymentsFragment();
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
    protected void onStart() {
        super.onStart();
        try {
            getSharedPreferences(SessionManager.PREFERENCE_FILE, SessionManager.PRIVATE_MODE)
                    .registerOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            getSharedPreferences(SessionManager.PREFERENCE_FILE, SessionManager.PRIVATE_MODE)
                    .unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {}
    }

    @Override
    protected void onRestart() {
        if (navigationIndex == 3) {
            loadFragment(0);
        }
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        shown = false;
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            rates.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Rate rate = null;
                    for (DataSnapshot rateSnap: dataSnapshot.getChildren()) {
                        rate = rateSnap.getValue(Rate.class);
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
        Driver driver = sessionManager.getLoggedDriver();
        String today = (driver.getEarningsDaily() > 0) ? getTruncate(String.valueOf(driver.getEarningsDaily())) + " €" : "0 €";
        String week = (driver.getEarningsWeekly() > 0) ? getTruncate(String.valueOf(driver.getEarningsWeekly())) + " €" : "0 €";
        todayEarnings.setText(today);
        weekEarnings.setText(week);
        schOnline.setChecked(driver.isOnline());
    }

    private String getTruncate(String price) {
        if (price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
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
        try {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                killProcess();
            }
        } catch (Exception e) {}
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*if (key.equals(SessionManager.KEY_LOCATION)) {
            Location location = sessionManager.getLocation();
            Event event = new Event(Event.SUBJECT_DASHBOARD_LOCATION, location);
            EventBus.publish(EventBus.SUBJECT_DASHBOARD, event);
            shown = true;
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.unregister(this);
        } catch (Exception e) {}
    }
}

