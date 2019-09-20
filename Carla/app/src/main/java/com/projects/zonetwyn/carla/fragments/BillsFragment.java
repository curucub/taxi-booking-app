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
import com.projects.zonetwyn.carla.adapters.BillAdapter;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.models.Bill;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BillsFragment extends Fragment {

    private Context context;
    private RecyclerView rcv;
    private LinearLayout lnlProgress;

    private boolean dataShown;

    private List<Bill> billList;
    private BillAdapter adapter;

    private SessionManager sessionManager;
    private Client client;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference bills;

    public BillsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        client = sessionManager.getLoggedClient();

        View rootView = inflater.inflate(R.layout.fragment_bills, container, false);

        rcv = rootView.findViewById(R.id.rcv);
        lnlProgress = rootView.findViewById(R.id.lnlProgress);

        lnlProgress.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        dataShown = false;

        database = FirebaseDatabase.getInstance();
        bills = database.getReference("bills");

        fetchData();

        return rootView;
    }

    private void fetchData() {
        Query query = bills.orderByChild("clientUid").equalTo(client.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Bill> billList = new ArrayList<>();
                for (DataSnapshot billSnap : dataSnapshot.getChildren()) {
                    Bill bill = billSnap.getValue(Bill.class);
                    if (bill != null) {
                        billList.add(bill);
                    }
                }
                if (!billList.isEmpty()) {
                    if (dataShown) {
                        addData(shuffle(billList));
                    } else {
                        showData(shuffle(billList));
                    }
                } else {
                    showToast(getString(R.string.no_bills));
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

    private void addData(List<Bill> billList) {
        this.billList.addAll(billList);
        adapter.notifyDataSetChanged();
    }

    private void showData(List<Bill> billList) {

        this.billList = billList;

        lnlProgress.setVisibility(View.GONE);
        rcv.setVisibility(View.VISIBLE);

        adapter = new BillAdapter(context, this.billList);
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setAdapter(adapter);

        dataShown = true;
    }

    private List<Bill> shuffle(List<Bill> billList) {
        List<Bill> result = new ArrayList<>();
        int last = billList.size() - 1;
        for (int i=last; i>=0; i--) {
            result.add(billList.get(i));
        }

        return result;
    }

}