package com.projects.zonetwyn.carladriver.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.activities.SignInActivity;
import com.projects.zonetwyn.carladriver.adapters.CountryAdapter;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpPhoneFragment extends Fragment {

    private Context context;

    private Button btnNext, btnSignIn;

    private Spinner spnCountry;

    private EditText edtPhone;

    private BaseAdapter adapter;
    private static final int[] NAMES = new int[]{R.string.france, R.string.spain, R.string.canada, R.string.argentina};
    private static final int[] ICONS = new int[]{R.drawable.fr, R.drawable.es, R.drawable.ca, R.drawable.ar};

    public SignUpPhoneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_phone, container, false);

        context = getContext();
        btnNext = rootView.findViewById(R.id.btnNext);
        btnSignIn = rootView.findViewById(R.id.btnSignIn);

        spnCountry = rootView.findViewById(R.id.spnCountry);

        edtPhone = rootView.findViewById(R.id.edtPhone);

        handleEvents();
        setupSpinner();

        return rootView;
    }

    private void setupSpinner() {
        adapter = new CountryAdapter(context, NAMES, ICONS);
        spnCountry.setAdapter(adapter);
        spnCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void handleEvents() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignInActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void onNextClicked() {

        String phone = (edtPhone.getText() != null) ? edtPhone.getText().toString() : "";

        if (phone.isEmpty()) {
           showToast(context.getResources().getString(R.string.phone_can_not_be_empty));
        } else {
            String completedPhone = "+33" + phone;
            Event event = new Event(Event.SUBJECT_SIGN_UP_GO_TO_CODE, completedPhone);
            EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
