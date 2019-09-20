package com.projects.zonetwyn.carladriver.fragments;

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
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.adapters.PaymentAdapter;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentsFragment extends Fragment {

    private Context context;
    private RecyclerView rcv;
    private LinearLayout lnlProgress;

    private boolean dataShown;

    private List<Payment> paymentList;
    private PaymentAdapter adapter;

    private SessionManager sessionManager;
    private Driver driver;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference payments;

    public PaymentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        driver = sessionManager.getLoggedDriver();

        View rootView = inflater.inflate(R.layout.fragment_payments, container, false);

        rcv = rootView.findViewById(R.id.rcv);
        lnlProgress = rootView.findViewById(R.id.lnlProgress);

        lnlProgress.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        dataShown = false;

        database = FirebaseDatabase.getInstance();
        payments = database.getReference("payments");

        fetchData();

        return rootView;
    }

    private void fetchData() {
        Query query = payments.orderByChild("driverUid").equalTo(driver.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Payment> paymentList = new ArrayList<>();
                for (DataSnapshot paymentSnap : dataSnapshot.getChildren()) {
                    Payment payment = paymentSnap.getValue(Payment.class);
                    if (payment != null) {
                        paymentList.add(payment);
                    }
                }
                if (!paymentList.isEmpty()) {
                    if (dataShown) {
                        addData(shuffle(paymentList));
                    } else {
                        showData(shuffle(paymentList));
                    }
                } else {
                    showToast(getString(R.string.no_payments));
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

    private void addData(List<Payment> paymentList) {
        this.paymentList.addAll(paymentList);
        adapter.notifyDataSetChanged();
    }

    private void showData(List<Payment> paymentList) {

        this.paymentList = paymentList;

        lnlProgress.setVisibility(View.GONE);
        rcv.setVisibility(View.VISIBLE);

        adapter = new PaymentAdapter(context, this.paymentList);
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setAdapter(adapter);

        dataShown = true;
    }

    private List<Payment> shuffle(List<Payment> paymentList) {
        List<Payment> result = new ArrayList<>();
        int last = paymentList.size() - 1;
        for (int i=last; i>=0; i--) {
            result.add(paymentList.get(i));
        }

        return result;
    }

}
