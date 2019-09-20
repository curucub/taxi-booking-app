package com.projects.zonetwyn.carladriver.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.models.Vehicle;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpVehicleFragment extends Fragment {

    private Context context;

    private EditText edtBrand;
    private EditText edtModel;
    private EditText edtYear;
    private EditText edtRegistrationNumber;

    private Button btnNext;

    public SignUpVehicleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_sign_up_vehicle, container, false);

        edtBrand = rootView.findViewById(R.id.edtBrand);
        edtModel = rootView.findViewById(R.id.edtModel);
        edtYear = rootView.findViewById(R.id.edtYear);
        edtRegistrationNumber = rootView.findViewById(R.id.edtRegistrationNumber);

        btnNext = rootView.findViewById(R.id.btnNext);

        handleEvents();

        return rootView;
    }

    private void handleEvents() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClicked();
            }
        });
    }

    private void onNextClicked() {
        String brand = (edtBrand.getText() != null) ? edtBrand.getText().toString() : "";
        String model = (edtModel.getText() != null) ? edtModel.getText().toString() : "";
        String year = (edtYear.getText() != null) ? edtYear.getText().toString() : "";
        String registrationNumber = (edtRegistrationNumber.getText() != null) ? edtRegistrationNumber.getText().toString() : "";

        if (brand.isEmpty() || model.isEmpty() || year.isEmpty() || registrationNumber.isEmpty()) {
            showToast(getString(R.string.empty_field));
        } else {
            Vehicle vehicle = new Vehicle();
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setRegistrationNumber(registrationNumber);

            Event event = new Event(Event.SUBJECT_SIGN_UP_GO_TO_DOCUMENTS, vehicle);
            EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
