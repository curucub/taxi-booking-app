package com.projects.zonetwyn.carladriver.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.components.CircularImageView;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.squareup.picasso.Picasso;

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
        Driver driver = sessionManager.getLoggedDriver();
        Picasso.get().load(driver.getPictureUrl()).error(R.drawable.ic_person).into(imgPicture);
        String username = driver.getSurname() + " " + driver.getName();
        txtUsername.setText(username);

        txtPhone.setText(driver.getPhone());
        txtEmail.setText(driver.getEmail());
    }
}

