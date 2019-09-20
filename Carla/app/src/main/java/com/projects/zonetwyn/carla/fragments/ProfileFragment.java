package com.projects.zonetwyn.carla.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.components.CircularImageView;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private Context context;
    private SessionManager sessionManager;

    private CircularImageView imgPicture;
    private TextView txtUsername;
    private TextView txtEmail;
    private TextView txtPhone;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        imgPicture = rootView.findViewById(R.id.imgPicture);
        txtUsername = rootView.findViewById(R.id.txtUsername);
        txtEmail = rootView.findViewById(R.id.txtEmail);
        txtPhone = rootView.findViewById(R.id.txtPhone);

        updateUI();

        return rootView;
    }

    private void updateUI() {
        Client client = sessionManager.getLoggedClient();
        Picasso.get().load(client.getPictureUrl()).error(R.drawable.ic_person).into(imgPicture);
        String username = client.getSurname() + " " + client.getName();
        txtUsername.setText(username);

        txtPhone.setText(client.getPhone());
        txtEmail.setText(client.getEmail());
    }
}
