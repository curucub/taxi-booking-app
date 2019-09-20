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
import com.projects.zonetwyn.carla.adapters.RequestAdapter;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private Context context;
    private RecyclerView rcv;
    private LinearLayout lnlProgress;

    private boolean dataShown;

    private List<Request> requestList;
    private RequestAdapter adapter;

    private SessionManager sessionManager;
    private Client client;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference requests;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        client = sessionManager.getLoggedClient();

        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);

        rcv = rootView.findViewById(R.id.rcv);
        lnlProgress = rootView.findViewById(R.id.lnlProgress);

        lnlProgress.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        dataShown = false;

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("requests");

        fetchData();

        return rootView;
    }

    private void fetchData() {
        Query query = requests.orderByChild("clientUid").equalTo(client.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Request> requestList = new ArrayList<>();
                for (DataSnapshot requestSnap : dataSnapshot.getChildren()) {
                    Request request = requestSnap.getValue(Request.class);
                    if (request != null) {
                        requestList.add(request);
                    }
                }
                if (!requestList.isEmpty()) {
                    if (dataShown) {
                        addData(shuffle(requestList));
                    } else {
                        showData(shuffle(requestList));
                    }
                } else {
                    showToast(getString(R.string.no_requests));
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

    private void addData(List<Request> requestList) {
        this.requestList.addAll(requestList);
        adapter.notifyDataSetChanged();
    }

    private void showData(List<Request> requestList) {

        this.requestList = requestList;

        lnlProgress.setVisibility(View.GONE);
        rcv.setVisibility(View.VISIBLE);

        adapter = new RequestAdapter(context, this.requestList);
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setAdapter(adapter);

        dataShown = true;
    }

    private List<Request> shuffle(List<Request> requestList) {
        List<Request> result = new ArrayList<>();
        int last = requestList.size() - 1;
        for (int i=last; i>=0; i--) {
            result.add(requestList.get(i));
        }

        return result;
    }

}