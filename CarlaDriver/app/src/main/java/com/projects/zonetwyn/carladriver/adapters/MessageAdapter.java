package com.projects.zonetwyn.carladriver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle;
        public TextView txtDate;
        public TextView txtContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtContent = itemView.findViewById(R.id.txtContent);
        }
    }

    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View messageView = inflater.inflate(R.layout.item_message, viewGroup, false);
        ViewHolder holder = new ViewHolder(messageView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder viewHolder, int i) {
        Message message = messages.get(i);

        String date = message.getCreatedAt();
        date = date.substring(0, 16).replace(" ", "  ");
        viewHolder.txtDate.setText(date);

        viewHolder.txtTitle.setText(message.getTitle());
        viewHolder.txtContent.setText(message.getContent());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}

