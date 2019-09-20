package com.projects.zonetwyn.carladriver.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.activities.MainActivity;
import com.projects.zonetwyn.carladriver.activities.SignUpActivity;
import com.projects.zonetwyn.carladriver.adapters.CountryAdapter;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.utils.CryptoUtils;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignInPhoneFragment extends Fragment {

    private Context context;

    private Button btnSignIn, btnSignUp;

    private Spinner spnCountry;

    private EditText edtPhone;

    private BaseAdapter adapter;
    private static final int[] NAMES = new int[]{R.string.france, R.string.spain, R.string.canada, R.string.argentina};
    private static final int[] ICONS = new int[]{R.drawable.fr, R.drawable.es, R.drawable.ca, R.drawable.ar};

    private FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference drivers;

    private Driver driver;

    private SessionManager sessionManager;

    private android.app.AlertDialog waitingDialog;

    public SignInPhoneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in_phone, container, false);

        context = getContext();

        sessionManager = SessionManager.getInstance(context.getApplicationContext());

        btnSignIn = rootView.findViewById(R.id.btnSignIn);
        btnSignUp = rootView.findViewById(R.id.btnSignUp);

        spnCountry = rootView.findViewById(R.id.spnCountry);

        edtPhone = rootView.findViewById(R.id.edtPhone);

        handleEvents();
        setupSpinner();

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        drivers = database.getReference("drivers");

        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.sign_in_progress)).build();

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
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInClicked();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignUpActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void processSignIn(Driver driver) {
        if (sessionManager != null) {
            sessionManager.signInDriver(driver);
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void signInWithEmail() {
        if (driver != null) {
            auth.signInWithEmailAndPassword(driver.getEmail(), CryptoUtils.hashWithSHA256(driver.getPhone()))
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            waitingDialog.dismiss();
                            processSignIn(driver);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            waitingDialog.dismiss();
                            showToast(e.getMessage());
                        }
                    });
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void onSignInClicked() {
        String phone = (edtPhone.getText() != null) ? edtPhone.getText().toString() : "";
        if (phone.isEmpty()) {
            showToast(getString(R.string.phone_can_not_be_empty));
        } else {
            waitingDialog.show();

            String completedPhone = "+33" + phone;

            Query query = drivers.orderByChild("phone").equalTo(completedPhone);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    driver = null;
                    for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                        driver = driverSnapshot.getValue(Driver.class);
                    }
                    if (driver != null) {
                        if (driver.isActivated()) {
                            signInWithEmail();
                        } else {
                            waitingDialog.dismiss();
                            showToast(getString(R.string.your_account_not_activated));
                        }
                    } else {
                        waitingDialog.dismiss();
                        showToast(getString(R.string.your_phone_not_recognized));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
