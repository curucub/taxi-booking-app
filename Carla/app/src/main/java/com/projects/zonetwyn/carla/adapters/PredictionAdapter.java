package com.projects.zonetwyn.carla.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.google.place.Prediction;

import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }

    private Context context;
    private List<Prediction> predictions;

    public PredictionAdapter(Context context, List<Prediction> predictions) {
        this.context = context;
        this.predictions = predictions;
    }

    @NonNull
    @Override
    public PredictionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View predictionView = inflater.inflate(R.layout.item_prediction, viewGroup, false);
        ViewHolder holder = new ViewHolder(predictionView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionAdapter.ViewHolder viewHolder, int i) {
        Prediction prediction = predictions.get(i);
        viewHolder.txtDescription.setText(prediction.getDescription());
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }
}
