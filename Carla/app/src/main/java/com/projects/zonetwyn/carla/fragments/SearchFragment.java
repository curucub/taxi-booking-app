package com.projects.zonetwyn.carla.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.activities.AddCodeActivity;
import com.projects.zonetwyn.carla.activities.PickPlaceActivity;
import com.projects.zonetwyn.carla.components.CircularImageView;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.google.DistanceCalculator;
import com.projects.zonetwyn.carla.google.GeofireData;
import com.projects.zonetwyn.carla.google.matrix.Data;
import com.projects.zonetwyn.carla.google.matrix.Element;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Driver;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.models.Pricing;
import com.projects.zonetwyn.carla.models.Rate;
import com.projects.zonetwyn.carla.services.LocationReceiver;
import com.projects.zonetwyn.carla.services.NotificationService;
import com.projects.zonetwyn.carla.utils.Event;
import com.projects.zonetwyn.carla.utils.EventBus;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap map;
    private Context context;

    private LatLng currentLocation;
    private Marker currentMarker;
    private static final int DEFAULT_ZOOM = 15;

    public static final int PICK_FROM_REQUEST = 1600;
    public static final int PICK_TO_REQUEST = 1700;

    private LinearLayout lnlFrom;
    private LinearLayout lnlTo;
    private TextView txtFrom;
    private TextView txtTo;

    private Point startingPoint;
    private Point arrivalPoint;


    private List<Driver> driverList;
    private List<Pricing> pricingList;
    private List<GeofireData> geofireData;

    private FirebaseDatabase database;
    private DatabaseReference drivers;
    private DatabaseReference points;
    private DatabaseReference requests;
    private DatabaseReference driversAvailable;
    private DatabaseReference pricings;
    private DatabaseReference rates;

    private Dialog pricingDialog;

    private android.app.AlertDialog waitingDialog;
    private Driver currentDriver;

    private SessionManager sessionManager;

    private Map<String, Double> distancesMap;

    private double price;

    private boolean showClient = true;

    private Rate rate;

    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().matches(LocationReceiver.ACTION_LOCATION_UPDATE)) {
                    String locationJson = intent.getStringExtra(LocationReceiver.UPDATED_LOCATION);
                    if (locationJson != null) {
                        try {
                            Location location = new Gson().fromJson(locationJson, Location.class);
                            displayLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            }
        }
    };

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        sessionManager = SessionManager.getInstance(context.getApplicationContext());

        lnlFrom = rootView.findViewById(R.id.lnlFrom);
        lnlTo = rootView.findViewById(R.id.lnlTo);

        txtFrom = rootView.findViewById(R.id.txtFrom);
        txtTo = rootView.findViewById(R.id.txtTo);

        subscribeToBus();

        handleEvents();

        //Init database
        database = FirebaseDatabase.getInstance();
        drivers = database.getReference("drivers");
        points = database.getReference("points");
        requests = database.getReference("requests");
        driversAvailable = database.getReference("drivers-available");
        pricings = database.getReference("pricings");
        rates = database.getReference("rates");

        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.getting_price)).build();

        fetchDrivers();
        fetchAvailableDrivers();
        fetchPricings();
        fetchRates();

        return rootView;
    }

    private void fetchPricings() {
        pricings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pricingList = new ArrayList<>();
                for (DataSnapshot pricingSnap : dataSnapshot.getChildren()) {
                    Pricing pricing = pricingSnap.getValue(Pricing.class);
                    if (pricing != null) {
                        pricingList.add(pricing);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchRates() {
        rates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

    private void fetchDrivers() {
        Query query = drivers.orderByChild("online").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverList = new ArrayList<>();
                for (DataSnapshot driverSnap : dataSnapshot.getChildren()) {
                    Driver driver = driverSnap.getValue(Driver.class);
                    if (driver != null) {
                        driverList.add(driver);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAvailableDrivers() {
        driversAvailable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                geofireData = new ArrayList<>();
                for (DataSnapshot locationSnap: dataSnapshot.getChildren()) {
                    GeofireData data = locationSnap.getValue(GeofireData.class);
                    if (data != null) {
                        data.setUid(locationSnap.getKey());
                        geofireData.add(data);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void handleEvents() {
        lnlFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(context, PickPlaceActivity.class);
                    getActivity().startActivityForResult(intent, PICK_FROM_REQUEST);
                }
            }
        });

        lnlTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent intent = new Intent(context, PickPlaceActivity.class);
                    getActivity().startActivityForResult(intent, PICK_TO_REQUEST);
                }
            }
        });
    }

    private void subscribeToBus() {
        EventBus.subscribe(EventBus.SUBJECT_SEARCH, this, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (o instanceof Event) {
                    Event event = (Event) o;
                    if (event.getData() != null && event.getSubject() != 0) {
                        switch (event.getSubject()) {
                            case Event.SUBJECT_SEARCH_LOCATION:
                                if (event.getData() instanceof LatLng) {
                                    LatLng latLng = (LatLng) event.getData();
                                    updateData(latLng);
                                }
                                break;
                            case Event.SUBJECT_SEARCH_FROM:
                                if (event.getData() instanceof Point) {
                                    startingPoint = (Point) event.getData();
                                    txtFrom.setText(startingPoint.getAddress());
                                }
                                break;
                            case Event.SUBJECT_SEARCH_TO:
                                if (event.getData() instanceof Point) {
                                    arrivalPoint = (Point) event.getData();
                                    txtTo.setText(getTruncate(arrivalPoint.getAddress()));
                                    showDrivers();
                                }
                                break;
                        }
                    }
                }
            }
        });
    }

    private void showDrivers() {
        if (startingPoint != null && arrivalPoint != null) {
            DistanceCalculator distanceCalculator = new DistanceCalculator();
            Map<String, Double> distancesMap = new HashMap<>();
            String key = "";
            double prevDistance = 0;
            Driver choosedDriver = new Driver();
            choosedDriver.setName("Nearest");
            choosedDriver.setSurname("Driver");
            if (driverList != null && !driverList.isEmpty()) {
                for (Driver driver : driverList) {
                    GeofireData data = getData(driver.getUid());
                    if (data != null) {
                        double distance = distanceCalculator.greatCircleInMeters(new LatLng(data.getL().get(0), data.getL().get(1)), new LatLng(startingPoint.getLatitude(), startingPoint.getLongitude()));
                        distancesMap.put(driver.getUid(), distance);
                        if (prevDistance == 0 || (prevDistance > distance)) {
                            prevDistance = distance;
                            key = driver.getUid();
                            choosedDriver = driver;
                            currentDriver = driver;
                        }
                    }
                }
                addDriverToMap(key, choosedDriver.getName() + " " + choosedDriver.getSurname());
                calculatePrice(key);
            } else {
                showToast(getString(R.string.no_driver));
            }
        } else {
            showToast(getString(R.string.select_locations));
        }
    }

    private void addDriverToMap(String key, final String name) {
        GeoFire geoFire = new GeoFire(driversAvailable);
        geoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    map.clear();
                    Marker marker = map.addMarker(new MarkerOptions()
                            .title(name)
                            .position(new LatLng(location.latitude, location.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pro)));
                    marker.setTag("nearest driver");
                    LatLngBounds.Builder builder = LatLngBounds.builder();

                    if (currentLocation != null) {
                        currentMarker = map.addMarker(new MarkerOptions()
                                .title("You")
                                .position(currentLocation)
                                .draggable(true)
                                .icon(vectorToBitmap(R.drawable.ic_place, getResources().getColor(R.color.colorPrimary))));
                        currentMarker.setTag("YOUR_MARKER");
                        builder.include(currentMarker.getPosition());
                    }
                    builder.include(marker.getPosition());

                    LatLngBounds bounds = builder.build();

                    showClient = false;
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                } else {
                    showToast("Désolé, nous n'avons pu trouvé aucun chauffeur disponble");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void calculatePrice(String key) {
        GeofireData data = getData(key);
        if (data != null) {
            waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.getting_price)).build();
            waitingDialog.show();
            try {
                RequestQueue requestQueue;

                Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);

                Network network = new BasicNetwork(new HurlStack());

                requestQueue = new RequestQueue(cache, network);

                requestQueue.start();

                JsonObjectRequest request = new JsonObjectRequest
                        (Request.Method.GET, getUrl(new LatLng(data.getL().get(0), data.getL().get(1)), new LatLng(startingPoint.getLatitude(), startingPoint.getLongitude())), null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                final Data data = new Gson().fromJson(response.toString(), Data.class);
                                Element element = data.getRows().get(0).getElements().get(0);
                                fetchPrice(element, new LatLng(startingPoint.getLatitude(), startingPoint.getLongitude()), new LatLng(arrivalPoint.getLatitude(), arrivalPoint.getLongitude()));
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                waitingDialog.dismiss();
                                showToast(error.getMessage());
                            }
                        });
                requestQueue.add(request);
            } catch (RuntimeException e) {

            }
        }
    }

    private GeofireData getData(String uid) {
        for (GeofireData data : geofireData) {
            if (data.getUid().equals(uid)) {
                return data;
            }
        }
        return null;
    }

    private void updateData(LatLng latLng) {
        Point point = new Point();
        point.setLatitude(latLng.latitude);
        point.setLongitude(latLng.longitude);
        Address address = getCountryInfo(latLng);
        if (address != null) {
            point.setAddress(address.getAddressLine(0));
        }
        if (startingPoint == null) {
            txtFrom.setText(getString(R.string.my_location));
            displayLocation(latLng);
        }
        startingPoint = point;
    }

    private String getTruncate(String input) {
        String output = input;
        if (input.length() > 35) {
            output = input.substring(0, 35);
        }
        return output;
    }

    private String getTruncatePrice(String price) {
        if (price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
    }

    private void displayLocation(LatLng latLng) {
        currentLocation = latLng;
        if (map != null) {
            if (currentMarker != null)
                currentMarker.remove();

            currentMarker = map.addMarker(new MarkerOptions()
                    .title("You")
                    .position(latLng)
                    .draggable(true)
                    .icon(vectorToBitmap(R.drawable.ic_place, getResources().getColor(R.color.colorPrimary))));
            currentMarker.setTag("YOUR_MARKER");

            if (showClient) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    return false;
                }
            });
        } else {
            Toast.makeText(context, "Map is still null!", Toast.LENGTH_SHORT).show();
        }
    }

    private Address getCountryInfo(LatLng latLng) {
        Address address = null;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        String errorMessage = "No error";
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException ioException) {
            errorMessage = "IOException>>" + ioException.getMessage();
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "IllegalArgumentException>>" + illegalArgumentException.getMessage();
        }
        if (addresses != null && !addresses.isEmpty()) {
            address = addresses.get(0);
        }
        return address;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
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

    private void fetchPrice(final Element lastElement, LatLng startPoint, LatLng endPoint) {

        try {
            RequestQueue requestQueue;

            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);

            Network network = new BasicNetwork(new HurlStack());

            requestQueue = new RequestQueue(cache, network);

            requestQueue.start();

            JsonObjectRequest request = new JsonObjectRequest
                    (Request.Method.GET, getUrl(startPoint, endPoint), null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            final Data data = new Gson().fromJson(response.toString(), Data.class);
                            waitingDialog.dismiss();
                            Element element = data.getRows().get(0).getElements().get(0);
                            showPrice(lastElement, element);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            waitingDialog.dismiss();
                            showToast(error.getMessage());
                        }
                    });
            requestQueue.add(request);
        } catch (RuntimeException e) {

        }
    }

    private void showPrice(Element lastElement, Element element) {

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        Pricing pricing = getPricing(time);
        if (pricing != null) {
            final long durationMinute = (element.getDuration().getValue()) / 60;
            final long distanceKilometer = (element.getDistance().getValue()) / 1000;
            price = pricing.getBase() + (durationMinute * pricing.getPerMinute()) + (distanceKilometer * pricing.getPerKilometer());

            if (price < 6) {
                price = 6;
            }

            pricingDialog = new Dialog(context);
            pricingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pricingDialog.setContentView(R.layout.dialog_pricing);
            Window window = pricingDialog.getWindow();
            window.setLayout(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);


            final ImageView imgClose = pricingDialog.findViewById(R.id.imgClose);
            TextView txtWaitingTime= pricingDialog.findViewById(R.id.txtWaitingTime);
            TextView txtRideTime = pricingDialog.findViewById(R.id.txtRideTime);
            TextView txtPrice = pricingDialog.findViewById(R.id.txtPrice);

            CircularImageView imgPicture = pricingDialog.findViewById(R.id.imgPicture);
            TextView txtUsername = pricingDialog.findViewById(R.id.txtUsername);
            TextView txtNote = pricingDialog.findViewById(R.id.txtNote);
            Button btnSendRequest = pricingDialog.findViewById(R.id.btnSendRequest);

            LinearLayout lnlAddCode = pricingDialog.findViewById(R.id.lnlAddCode);

            Picasso.get().load(currentDriver.getPictureUrl()).error(R.drawable.ic_person_blue).into(imgPicture);
            String username = currentDriver.getSurname() + " " + currentDriver.getName();
            String note = "" + currentDriver.getNote()  + " / " + String.valueOf(currentDriver.getRidesCount() * 4);

            txtUsername.setText(username);
            txtNote.setText(note);

            Rate rate = sessionManager.getRate();
            double percent = 0.8;
            if (rate != null) {
                percent = (100 - (double) rate.getRate()) / 100;
            }

            double number = price * percent;

            String priceText = "" + getTruncatePrice(String.valueOf(number)) + " €";
            txtPrice.setText(priceText);
            txtWaitingTime.setText(lastElement.getDuration().getText());
            txtRideTime.setText(element.getDuration().getText());

            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pricingDialog.dismiss();
                    Client client = sessionManager.getLoggedClient();
                    if (client.getCardExpirationDate() == null || client.getCardCode() == null || client.getCardNumber() == null) {
                        showToast("Vous devez enregistrer une carte pour pouvoir envoyer une requête");
                    } else {
                        sendRequest(durationMinute, distanceKilometer, price);
                    }
                }
            });

            imgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgClose.startAnimation(AnimationUtils.loadAnimation(context, R.anim.blink));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pricingDialog.dismiss();
                        }
                    }, 500);
                }
            });
            lnlAddCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentDriver != null && startingPoint != null && arrivalPoint != null) {
                        com.projects.zonetwyn.carla.models.Request request = buildRequest(durationMinute, distanceKilometer, price);
                        request.setStartingPoint(startingPoint);
                        request.setArrivalPoint(arrivalPoint);
                        Intent intent = new Intent(context, AddCodeActivity.class);
                        intent.putExtra("REQUEST", request);
                        startActivity(intent);
                    }
                }
            });

            pricingDialog.show();
        } else {
            showToast(getString(R.string.time_is_late));
        }
    }

    private com.projects.zonetwyn.carla.models.Request buildRequest(long duration, long distance, double price) {
        com.projects.zonetwyn.carla.models.Request request = new com.projects.zonetwyn.carla.models.Request();
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        request.setCreatedAt(date);
        request.setStatus("IN_WAITING");
        request.setPrice(price);
        request.setDistance(String.valueOf(distance) + " Km");
        request.setDuration(String.valueOf(duration) + " Min");

        Client client = sessionManager.getLoggedClient();
        request.setClientUid(client.getUid());
        request.setDriverUid(currentDriver.getUid());
        return request;
    }

    private Pricing getPricing(String time) {
        Pricing p = null;
        if (pricingList != null && !pricingList.isEmpty()) {
            for (Pricing pricing : pricingList) {
                if (betweenTimes(pricing.getStartTime(), pricing.getEndTime(), time)) {
                    p = pricing;
                    break;
                }
            }
        }
        return p;
    }

    private boolean betweenTimes(String time1, String time2, String time) {
        if (lessThan(time1, time) && lessThan(time, time2)) {
            return true;
        }
        return false;
    }

    private boolean lessThan(String time1, String time) {
        boolean lessThan = false;
        int hour1 = Integer.parseInt(time1.split(":")[0]);
        int minute1 = Integer.parseInt(time1.split(":")[1]);
        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);

        if (hour1 < hour) {
            lessThan = true;
        } else if (hour1 == hour) {
            if (minute1 <= minute) {
                lessThan = true;
            }
        }

        return lessThan;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private String getUrl(LatLng startPoint, LatLng endPoint) {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&language=fr&origins=" +
                startPoint.latitude + "," + startPoint.longitude + "&destinations=" +
                endPoint.latitude + "," + endPoint.longitude + "&key=AIzaSyAhqVPhLt4UpuMPiDTSscIPBxmdgiiDayM";

        return url;
    }

    private void sendRequest(long duration, long distance, double price) {
        if (currentDriver != null && startingPoint != null && arrivalPoint != null) {
            waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.sending_request)).build();
            waitingDialog.show();
            final com.projects.zonetwyn.carla.models.Request request = buildRequest(duration, distance, price);

            startingPoint.setUid(points.push().getKey());
            points.child(startingPoint.getUid())
                    .setValue(startingPoint)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            arrivalPoint.setUid(points.push().getKey());
                            points.child(arrivalPoint.getUid())
                                    .setValue(arrivalPoint)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            request.setStartingPointUid(startingPoint.getUid());
                                            request.setArrivalPointUid(arrivalPoint.getUid());
                                            request.setUid(requests.push().getKey());
                                            requests.child(request.getUid())
                                                    .setValue(request)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            waitingDialog.dismiss();
                                                            showToast(getString(R.string.successfully_sent));
                                                            startNotificationService();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

        } else {
            showToast("Seems like something is missing!");
        }
    }

    private void startNotificationService() {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("request", true);
        context.startService(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            EventBus.unregister(this);
            if (gpsLocationReceiver != null)
                context.unregisterReceiver(gpsLocationReceiver);
        } catch (Exception e) {}
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String uid = (String) marker.getTag();
        if (uid != null) {
            if (startingPoint == null || arrivalPoint == null) {
                Toast.makeText(context, getString(R.string.starting_empty), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(gpsLocationReceiver, new IntentFilter(LocationReceiver.ACTION_LOCATION_UPDATE));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.setZIndex(1.0f);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        marker.setZIndex(0f);
        displayLocation(marker.getPosition());
    }
}
