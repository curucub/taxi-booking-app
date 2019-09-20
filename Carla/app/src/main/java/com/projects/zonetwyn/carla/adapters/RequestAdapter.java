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
import com.projects.zonetwyn.carla.models.Request;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtFrom;
        public TextView txtTo;
        public TextView txtStatus;
        public TextView txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFrom = itemView.findViewById(R.id.txtPlaceFrom);
            txtTo = itemView.findViewById(R.id.txtPlaceTo);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private Context context;
    private List<Request> requests;

    private FirebaseDatabase database;
    private DatabaseReference points;

    public RequestAdapter(Context context, List<Request> requests) {
        this.context = context;
        this.requests = requests;
        database = FirebaseDatabase.getInstance();
        points = database.getReference("points");
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View requestView = inflater.inflate(R.layout.item_request, viewGroup, false);
        ViewHolder holder = new ViewHolder(requestView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestAdapter.ViewHolder viewHolder, int i) {
        Request request = requests.get(i);

        String status = request.getStatus();
        if (status.equals("ACCEPTED")) {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.green));
        } else if (status.equals("REJECTED")) {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.red));
        } else if (status.equals("IN_WAITING")) {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.gray));
        }
        viewHolder.txtStatus.setText(getStatusFrench(request.getStatus()));

        String date = request.getCreatedAt();
        date = date.substring(0, 16).replace(" ", "  ");
        viewHolder.txtDate.setText(date);

        if (request.getStartingPointUid() != null) {
            Query query = points.orderByChild("uid").equalTo(request.getStartingPointUid());
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
        if (request.getArrivalPointUid() != null) {
            Query query = points.orderByChild("uid").equalTo(request.getArrivalPointUid());
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

    private String getStatusFrench(String status) {
        if (status.equals("ACCEPTED")) {
            return "ACCEPTEE";
        } else if (status.equals("REJECTED")) {
            return "REJETEE";
        } else if (status.equals("IN_WAITING")) {
            return "EN ATTENTE";
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
        return requests.size();
    }
}

