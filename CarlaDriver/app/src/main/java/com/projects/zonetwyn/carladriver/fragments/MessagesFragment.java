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
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.adapters.MessageAdapter;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private Context context;
    private RecyclerView rcv;
    private LinearLayout lnlProgress;

    private boolean dataShown;

    private List<Message> messageList;
    private MessageAdapter adapter;

    private SessionManager sessionManager;
    private Driver driver;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference messages;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        driver = sessionManager.getLoggedDriver();

        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        rcv = rootView.findViewById(R.id.rcv);
        lnlProgress = rootView.findViewById(R.id.lnlProgress);

        lnlProgress.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        dataShown = false;

        database = FirebaseDatabase.getInstance();
        messages = database.getReference("messages");

        fetchData();

        return rootView;
    }

    private void fetchData() {
        messages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messageList = new ArrayList<>();
                for (DataSnapshot messageSnap : dataSnapshot.getChildren()) {
                    Message message = messageSnap.getValue(Message.class);
                    if (message != null) {
                        if (message.getTarget().equals("DRIVERS")
                                || message.getTarget().equals("USERS")
                                || (message.getTarget().equals("DRIVER") && message.getDriverUid().equals(driver.getUid()))) {
                            messageList.add(message);
                        }
                    }
                }
                if (!messageList.isEmpty()) {
                    if (dataShown) {
                        addData(shuffle(messageList));
                    } else {
                        showData(shuffle(messageList));
                    }
                } else {
                    showToast(getString(R.string.no_messages));
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

    private void addData(List<Message> messageList) {
        this.messageList.addAll(messageList);
        adapter.notifyDataSetChanged();
    }

    private void showData(List<Message> messageList) {

        this.messageList = messageList;

        lnlProgress.setVisibility(View.GONE);
        rcv.setVisibility(View.VISIBLE);

        adapter = new MessageAdapter(context, this.messageList);
        rcv.setLayoutManager(new LinearLayoutManager(context));
        rcv.setAdapter(adapter);

        dataShown = true;
    }

    private List<Message> shuffle(List<Message> messageList) {
        List<Message> result = new ArrayList<>();
        int last = messageList.size() - 1;
        for (int i=last; i>=0; i--) {
            result.add(messageList.get(i));
        }

        return result;
    }
}
