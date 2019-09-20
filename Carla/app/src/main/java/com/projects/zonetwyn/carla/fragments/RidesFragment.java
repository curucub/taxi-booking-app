package com.projects.zonetwyn.carla.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.adapters.RideAdapter;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RidesFragment extends Fragment {
    
    private Context context;
    private RecyclerView rcv;
    private LinearLayout lnlProgress;
    
    private boolean dataShown;
    
    private List<Ride> rideList;
    private RideAdapter adapter;

    private SessionManager sessionManager;
    private Client client;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference rides;

    public RidesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        client = sessionManager.getLoggedClient();
        
        View rootView = inflater.inflate(R.layout.fragment_rides, container, false);
        
        rcv = rootView.findViewById(R.id.rcv);
        lnlProgress = rootView.findViewById(R.id.lnlProgress);
        
        lnlProgress.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        dataShown = false;

        database = FirebaseDatabase.getInstance();
        rides = database.getReference("rides");

        fetchData();
        
        return rootView;
    }

    private void fetchData() {
        Query query = rides.orderByChild("clientUid").equalTo(client.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rideList = new ArrayList<>();
                for (DataSnapshot rideSnap : dataSnapshot.getChildren()) {
                    Ride ride = rideSnap.getValue(Ride.class);
                    if (ride != null) {
                        rideList.add(ride);
                    }
                }
                if (!rideList.isEmpty()) {
                    if (dataShown) {
                        addData(shuffle(rideList));
                    } else {
                        showData(shuffle(rideList));
                    }
                } else {
                    showToast(getString(R.string.no_rides));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void addData(List<Ride> rideList) {
        this.rideList.addAll(rideList);
        adapter.notifyDataSetChanged();
    }

    private void showData(List<Ride> rideList) {

        this.rideList = rideList;

        lnlProgress.setVisibility(View.GONE);
        rcv.setVisibility(View.VISIBLE);

        adapter = new RideAdapter(context, this.rideList);
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setAdapter(adapter);

        dataShown = true;
    }

    private List<Ride> shuffle(List<Ride> rideList) {
        List<Ride> result = new ArrayList<>();
        int last = rideList.size() - 1;
        for (int i=last; i>=0; i--) {
            result.add(rideList.get(i));
        }

        return result;
    }

}
