package com.projects.zonetwyn.carla.activities;

import android.content.Intent;
import android.os.Handler;
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
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.adapters.PredictionAdapter;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.google.place.Prediction;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class SetLocationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText edtQuery;

    private PredictionAdapter adapter;
    private List<Prediction> predictions;

    private GeoDataClient geoDataClient;

    private RecyclerView rcvPredictions;

    private int locationType = 1; //1 For Home, 2 For Office, 3 For Favorite

    private SessionManager sessionManager;

    private FirebaseDatabase database;
    private DatabaseReference clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        Intent intent = getIntent();
        if (intent.hasExtra(PickPlaceActivity.HOME_LOCATION)) {
            locationType = 1;
        } else if (intent.hasExtra(PickPlaceActivity.OFFICE_LOCATION)) {
            locationType = 2;
        } else if (intent.hasExtra(PickPlaceActivity.FAVORITE_LOCATION)) {
            locationType = 3;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
        toolbar.setTitle(getString(R.string.pick_place));

        rcvPredictions = findViewById(R.id.rcvPredictions);
        edtQuery = findViewById(R.id.edtQuery);

        sessionManager = SessionManager.getInstance(getApplicationContext());

        database = FirebaseDatabase.getInstance();
        clients = database.getReference("clients");

        //Init GeoData
        geoDataClient = com.google.android.gms.location.places.Places.getGeoDataClient(this, null);

        handleEvents();
    }

    private void showPredictions(List<Prediction> data) {
        predictions = data;

        adapter = new PredictionAdapter(this, predictions);
        rcvPredictions.setLayoutManager(new LinearLayoutManager(this));
        rcvPredictions.setAdapter(adapter);
        rcvPredictions.addOnItemTouchListener(new RecyclerItemClickListener(SetLocationActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
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

                        saveLocation(point);
                    }
                    places.release();
                } else {
                }
            }
        });
    }

    private void saveLocation(Point point) {
        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage(getString(R.string.updating_in_progress)).build();
        waitingDialog.show();
        final Client client = sessionManager.getLoggedClient();
        String jsonPoint = new Gson().toJson(point);
        switch (locationType) {
            case 1:
                client.setHomeAddress(jsonPoint);
                break;
            case 2:
                client.setOfficeAddress(jsonPoint);
                break;
            case 3:
                client.setFavoriteAddress(jsonPoint);
                break;
        }

        clients.child(client.getUid())
                .setValue(client)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.updateClient(client);
                        waitingDialog.dismiss();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 10);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Toast.makeText(SetLocationActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleEvents() {
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
