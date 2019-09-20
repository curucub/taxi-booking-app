package com.projects.zonetwyn.carladriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.models.Client;
import com.projects.zonetwyn.carladriver.models.Point;
import com.projects.zonetwyn.carladriver.models.Ride;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtFrom;
        public TextView txtTo;
        public TextView txtDistance;
        public TextView txtDuration;
        public TextView txtPrice;
        public ImageView imgPicture;
        public TextView txtUsername;
        public TextView txtStatus;
        public TextView txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFrom = itemView.findViewById(R.id.txtPlaceFrom);
            txtTo = itemView.findViewById(R.id.txtPlaceTo);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgPicture = itemView.findViewById(R.id.imgPicture);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private Context context;
    private List<Ride> rides;
    
    private FirebaseDatabase database;
    private DatabaseReference points;
    private DatabaseReference clients;

    public RideAdapter(Context context, List<Ride> rides) {
        this.context = context;
        this.rides = rides;
        database = FirebaseDatabase.getInstance();
        points = database.getReference("points");
        clients = database.getReference("clients");
    }

    @NonNull
    @Override
    public RideAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View rideView = inflater.inflate(R.layout.item_ride, viewGroup, false);
        ViewHolder holder = new ViewHolder(rideView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RideAdapter.ViewHolder viewHolder, int i) {
        Ride ride = rides.get(i);

        viewHolder.txtDistance.setText(ride.getDistance());
        viewHolder.txtDuration.setText(ride.getDuration());
        String price = "" + getTruncate(String.valueOf(ride.getPrice())) + " €";
        viewHolder.txtPrice.setText(price);
        viewHolder.txtStatus.setText(getStatusFrench(ride.getStatus()));

        String date = ride.getCreatedAt();
        date = date.substring(0, 16).replace(" ", "  ");
        viewHolder.txtDate.setText(date);
        
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
        if (ride.getClientUid() != null) {
            Query query = clients.orderByChild("uid").equalTo(ride.getClientUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot clientSnap : dataSnapshot.getChildren()) {
                        Client client = clientSnap.getValue(Client.class);
                        if (client != null) {
                            Picasso.get().load(client.getPictureUrl()).error(R.drawable.ic_person).into(viewHolder.imgPicture);
                            viewHolder.txtUsername.setText(client.getSurname());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private String getStatusFrench(String status) {
        if (status.equals("ENDED")) {
            return "TERMINEE";
        } else if (status.equals("IN_PROGRESS")) {
            return "EN COURS";
        } else if (status.equals("IN_WAITING")) {
            return "EN ATTENTE";
        } else if (status.equals("STARTED")) {
            return "DEMAREE";
        } else {
            return status;
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
        return rides.size();
    }
}
