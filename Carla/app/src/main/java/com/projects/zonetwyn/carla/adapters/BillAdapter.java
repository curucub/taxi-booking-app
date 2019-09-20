package com.projects.zonetwyn.carla.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.models.Bill;
import com.projects.zonetwyn.carla.models.Ride;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtFrom;
        public TextView txtTo;
        public TextView txtPrice;
        public TextView txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFrom = itemView.findViewById(R.id.txtPlaceFrom);
            txtTo = itemView.findViewById(R.id.txtPlaceTo);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private Context context;
    private List<Bill> bills;

    private FirebaseDatabase database;
    private DatabaseReference points;
    private DatabaseReference rides;

    public BillAdapter(Context context, List<Bill> bills) {
        this.context = context;
        this.bills = bills;
        database = FirebaseDatabase.getInstance();
        points = database.getReference("points");
        rides = database.getReference("rides");
    }

    @NonNull
    @Override
    public BillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View billView = inflater.inflate(R.layout.item_bill, viewGroup, false);
        ViewHolder holder = new ViewHolder(billView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final BillAdapter.ViewHolder viewHolder, int i) {
        Bill bill = bills.get(i);

        String date = bill.getCreatedAt();
        date = date.substring(0, 16).replace(" ", "  ");
        viewHolder.txtDate.setText(date);

        if (bill.getRideUid() != null) {

            Query query = rides.orderByChild("uid").equalTo(bill.getRideUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot rideSnap : dataSnapshot.getChildren()) {
                        Ride ride = rideSnap.getValue(Ride.class);
                        if (ride != null) {
                            String price = "" + getTruncate(String.valueOf(ride.getPrice())) + " €";
                            viewHolder.txtPrice.setText(price);

                            if (ride.getStartingPointUid() != null) {
                                Query query = points.orderByChild("uid").equalTo(ride.getStartingPointUid());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot pointSnap : dataSnapshot.getChildren()) {
                                            Point point = pointSnap.getValue(Point.class);
                                            if (point != null) {
                                                viewHolder.txtFrom.setText(point.getAddress());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (ride.getArrivalPointUid() != null) {
                                Query query = points.orderByChild("uid").equalTo(ride.getArrivalPointUid());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot pointSnap : dataSnapshot.getChildren()) {
                                            Point point = pointSnap.getValue(Point.class);
                                            if (point != null) {
                                                viewHolder.txtTo.setText(point.getAddress());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private String getTruncate(String price) {
        if (price.length() > 5) {
            return price.substring(0, 5);
        }
        return price;
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }
}

