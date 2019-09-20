package com.projects.zonetwyn.carla.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.adapters.PredictionAdapter;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.google.place.Places;
import com.projects.zonetwyn.carla.google.place.Prediction;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.utils.RecyclerItemClickListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PickPlaceActivity extends AppCompatActivity {

    private LatLng latLng;

    private Toolbar toolbar;
    private EditText edtQuery;

    private LinearLayout lnlHomeLocation;
    private LinearLayout lnlOfficeLocation;
    private LinearLayout lnlFavoriteLocation;
    private RecyclerView rcvPredictions;

    private TextView txtHome;
    private TextView txtOffice;
    private TextView txtFavorite;

    private PredictionAdapter adapter;
    private List<Prediction> predictions;

    private GeoDataClient geoDataClient;

    private PlaceAutocompleteFragment places;

    public static final String HOME_LOCATION = "HOME LOCATION";
    public static final String OFFICE_LOCATION = "OFFICE LOCATION";
    public static final String FAVORITE_LOCATION = "FAVORITE LOCATION";

    private SessionManager sessionManager;
    private Client client;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pick_place);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        toolbar.setTitle(getString(R.string.pick_place));

        lnlHomeLocation = findViewById(R.id.lnlHomeLocation);
        lnlOfficeLocation = findViewById(R.id.lnlOfficeLocation);
        lnlFavoriteLocation = findViewById(R.id.lnlFavoriteLocation);

        txtHome = findViewById(R.id.txtHome);
        txtOffice = findViewById(R.id.txtOffice);
        txtFavorite = findViewById(R.id.txtFavorite);

        rcvPredictions = findViewById(R.id.rcvPredictions);
        edtQuery = findViewById(R.id.edtQuery);
        //Init session manager
        sessionManager = SessionManager.getInstance(getApplicationContext());
        client = sessionManager.getLoggedClient();

        //Init GeoData
        geoDataClient = com.google.android.gms.location.places.Places.getGeoDataClient(this, null);

        handleEvents();
    }

    private void initPlaces() {
        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.placeFragment);
        if (places != null && places.getView() != null) {
            ((EditText) places.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(15);
            ImageView searchIcon = (ImageView)((LinearLayout)places.getView()).getChildAt(0);

            searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_blue));

            places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    Point point = new Point();
                    point.setLatitude(place.getLatLng().latitude);
                    point.setLongitude(place.getLatLng().longitude);
                    point.setAddress(place.getAddress().toString());

                    Intent intent = new Intent();
                    intent.putExtra("PLACE_RESULT", point);
                    setResult(AppCompatActivity.RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onError(Status status) {
                    showToast(status.getStatusMessage());
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initLocations();
    }

    private void initLocations() {
        if (sessionManager != null) {
            client = sessionManager.getLoggedClient();
            if (client.getHomeAddress() != null && !client.getHomeAddress().isEmpty()) {
                Point point = new Gson().fromJson(client.getHomeAddress(), Point.class);
                txtHome.setText(point.getAddress());
            }
            if (client.getOfficeAddress() != null && !client.getOfficeAddress().isEmpty()) {
                Point point = new Gson().fromJson(client.getOfficeAddress(), Point.class);
                txtOffice.setText(point.getAddress());
            }
            if (client.getFavoriteAddress() != null && !client.getFavoriteAddress().isEmpty()) {
                Point point = new Gson().fromJson(client.getFavoriteAddress(), Point.class);
                txtFavorite.setText(point.getAddress());
            }
        }
    }

    private void showPredictions(List<Prediction> data) {
        predictions = data;

        adapter = new PredictionAdapter(this, predictions);
        rcvPredictions.setLayoutManager(new LinearLayoutManager(this));
        rcvPredictions.setAdapter(adapter);
        rcvPredictions.addOnItemTouchListener(new RecyclerItemClickListener(PickPlaceActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                fetchPlaceData(predictions.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    private void fetchPlaceData(Prediction prediction) {
        geoDataClient.getPlaceById(prediction.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    Place place = places.get(0);
                    if (place != null && place.getAddress() != null) {
                        Point point = new Point();
                        point.setLatitude(place.getLatLng().latitude);
                        point.setLongitude(place.getLatLng().longitude);
                        point.setAddress(place.getAddress().toString());

                        goBack(point);
                    }
                    places.release();
                } else {
                }
            }
        });
    }

    private void goBack(Point point) {
        Intent intent = new Intent();
        intent.putExtra("PLACE_RESULT", point);
        setResult(AppCompatActivity.RESULT_OK, intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(PickPlaceActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleEvents() {
        lnlHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.getHomeAddress() != null && !client.getHomeAddress().isEmpty()) {
                    Point point = new Gson().fromJson(client.getHomeAddress(), Point.class);
                    goBack(point);
                } else {
                    Intent intent = new Intent(PickPlaceActivity.this, SetLocationActivity.class);
                    intent.putExtra(HOME_LOCATION, "HOME LOCATION");
                    startActivity(intent);
                }
            }
        });

        lnlOfficeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.getOfficeAddress() != null && !client.getOfficeAddress().isEmpty()) {
                    Point point = new Gson().fromJson(client.getOfficeAddress(), Point.class);
                    goBack(point);
                } else {
                    Intent intent = new Intent(PickPlaceActivity.this, SetLocationActivity.class);
                    intent.putExtra(OFFICE_LOCATION, "OFFICE LOCATION");
                    startActivity(intent);
                }
            }
        });

        lnlFavoriteLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client.getFavoriteAddress() != null && !client.getFavoriteAddress().isEmpty()) {
                    Point point = new Gson().fromJson(client.getFavoriteAddress(), Point.class);
                    goBack(point);
                } else {
                    Intent intent = new Intent(PickPlaceActivity.this, SetLocationActivity.class);
                    intent.putExtra(FAVORITE_LOCATION, "OFFICE LOCATION");
                    startActivity(intent);
                }
            }
        });

        edtQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (!query.isEmpty() && query.length() >= 2) {
                    performSearch(query);
                } else {
                    if (predictions != null && predictions.size() > 0) {
                        predictions.clear();
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void performSearch(String query) {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("FR")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                .build();

        LatLngBounds bounds = new LatLngBounds(
                new LatLng(47.981432, 3.398961),
                new LatLng(49.25, 1.4167)
        );

        Task<AutocompletePredictionBufferResponse> results = geoDataClient.getAutocompletePredictions(query, bounds, typeFilter);
        results.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                if (task.isSuccessful()) {
                    AutocompletePredictionBufferResponse response = task.getResult();
                    if (response != null) {
                        Iterator<AutocompletePrediction> iterator = response.iterator();
                        List<Prediction> predictionList = new ArrayList<>();
                        while (iterator.hasNext()) {
                            AutocompletePrediction autocompletePrediction = iterator.next();
                            Prediction prediction = new Prediction();
                            prediction.setDescription(autocompletePrediction.getFullText(null).toString());
                            prediction.setPlaceId(autocompletePrediction.getPlaceId());
                            predictionList.add(prediction);
                        }
                        showPredictions(predictionList);
                    }
                }
            }
        });
    }

    private void fetchData(String query) {
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + query +
                     "&types=geocode" +
                     "&location=" + latLng.latitude+ "," + latLng.longitude +
                     "&radius=5000&strictbounds&language=fr&key=AIzaSyAhqVPhLt4UpuMPiDTSscIPBxmdgiiDayM";

        RequestQueue requestQueue;

        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        Network network = new BasicNetwork(new HurlStack());

        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Places places = new Gson().fromJson(response.toString(), Places.class);
                        showToast(response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast(error.getMessage());
                    }
                });
        requestQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
