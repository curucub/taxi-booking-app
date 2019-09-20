package com.projects.zonetwyn.carladriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.models.Payment;

import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtPrice;
        public TextView txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    private Context context;
    private List<Payment> payments;

    public PaymentAdapter(Context context, List<Payment> payments) {
        this.context = context;
        this.payments = payments;
    }

    @NonNull
    @Override
    public PaymentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View paymentView = inflater.inflate(R.layout.item_payment, viewGroup, false);
        ViewHolder holder = new ViewHolder(paymentView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PaymentAdapter.ViewHolder viewHolder, int i) {
        Payment payment = payments.get(i);

        String date = payment.getCreatedAt();
        date = date.substring(0, 16).replace(" ", "  ");
        viewHolder.txtDate.setText(date);

        String price = "" + getTruncate(String.valueOf(payment.getAmount())) + " €";
        viewHolder.txtPrice.setText(price);
    }

    private String getTruncate(String price) {
        if (price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }
}

