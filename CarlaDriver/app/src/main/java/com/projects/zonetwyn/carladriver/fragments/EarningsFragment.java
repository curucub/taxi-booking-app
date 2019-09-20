package com.projects.zonetwyn.carladriver.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Payment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class EarningsFragment extends Fragment {

    private Context context;

    private List<Payment> paymentList;

    private SessionManager sessionManager;
    private Driver driver;

    private BarChart chart;
    private TextView txtDayGains;
    private TextView txtMonthGains;
    private TextView txtWeekGains;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference payments;

    private List<BarEntry> entries;

    public EarningsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        driver = sessionManager.getLoggedDriver();

        View rootView = inflater.inflate(R.layout.fragment_earnings, container, false);

        chart = rootView.findViewById(R.id.chart);
        txtDayGains = rootView.findViewById(R.id.txtDayGains);
        txtWeekGains = rootView.findViewById(R.id.txtWeekGains);
        txtMonthGains = rootView.findViewById(R.id.txtMonthGains);

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
                paymentList = new ArrayList<>();
                for (DataSnapshot paymentSnap : dataSnapshot.getChildren()) {
                    Payment payment = paymentSnap.getValue(Payment.class);
                    if (payment != null) {
                        paymentList.add(payment);
                    }
                }
                if (!paymentList.isEmpty()) {
                   addData();
                   updateUI();
                } else {
                    showToast(getString(R.string.no_payments));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateUI() {
        String monthCheck = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(new Date());
        double monthGains = 0;
        String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        Payment currentPayment = null;
        for (Payment payment : paymentList) {
            String createdAt = payment.getCreatedAt();
            if (createdAt.contains(monthCheck)) {
                monthGains += payment.getAmount();
            }
            if (createdAt.contains(currentDate)) {
                currentPayment = payment;
            }
        }

        txtMonthGains.setText(String.valueOf(getTruncate(String.valueOf(monthGains)) + " €"));
        String week = (driver.getEarningsWeekly() > 0) ? getTruncate(String.valueOf(driver.getEarningsWeekly())) + " €" : "0 €";
        txtWeekGains.setText(week);

        String today = (currentPayment != null) ? getTruncate(String.valueOf(currentPayment.getAmount())) + " €" : "0 €";
        txtDayGains.setText(today);
    }

    private String getTruncate(String price) {
        if (price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
    }

    private void addData() {
        entries = new ArrayList<>();

        float monday = 0;
        float tuesday = 0;
        float wednesday = 0;
        float thursday = 0;
        float friday = 0;
        float saturday = 0;
        float sunday = 0;

        //Filtering data
        for (Payment payment : paymentList) {
            String createdAt = payment.getCreatedAt();
            try {
                Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).parse(createdAt);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                switch (dayOfWeek) {
                    case Calendar.MONDAY:
                        monday = (float) payment.getAmount();
                        break;
                    case Calendar.TUESDAY:
                        tuesday = (float) payment.getAmount();
                        break;
                    case Calendar.WEDNESDAY:
                        wednesday = (float) payment.getAmount();
                        break;
                    case Calendar.THURSDAY:
                        thursday = (float) payment.getAmount();
                        break;
                    case Calendar.FRIDAY:
                        friday = (float) payment.getAmount();
                        break;
                    case Calendar.SATURDAY:
                        saturday = (float) payment.getAmount();
                        break;
                    case Calendar.SUNDAY:
                        sunday = (float) payment.getAmount();
                        break;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        entries.add(new BarEntry(0f, monday));
        entries.add(new BarEntry(1f, tuesday));
        entries.add(new BarEntry(2f, wednesday));
        entries.add(new BarEntry(3f, thursday));
        entries.add(new BarEntry(5f, friday));
        entries.add(new BarEntry(6f, saturday));
        entries.add(new BarEntry(6f, sunday));

        BarDataSet barDataSet = new BarDataSet(entries, "Gains");
        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);
        chart.setData(data);
        chart.setFitBars(true);
        chart.invalidate();

        //Init Axis
        final String[] quarters = new String[] { "LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM" };
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }
        };

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
