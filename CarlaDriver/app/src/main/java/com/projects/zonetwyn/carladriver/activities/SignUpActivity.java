package com.projects.zonetwyn.carladriver.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.fragments.SignUpAcceptanceFragment;
import com.projects.zonetwyn.carladriver.fragments.SignUpCodeFragment;
import com.projects.zonetwyn.carladriver.fragments.SignUpDocumentsFragment;
import com.projects.zonetwyn.carladriver.fragments.SignUpInformationsFragment;
import com.projects.zonetwyn.carladriver.fragments.SignUpPhoneFragment;
import com.projects.zonetwyn.carladriver.fragments.SignUpVehicleFragment;
import com.projects.zonetwyn.carladriver.models.Document;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Vehicle;
import com.projects.zonetwyn.carladriver.utils.CryptoUtils;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUpActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle;

    //For navigation
    private int navigationIndex;
    private List<String> titles;

    private TextView txtTitle;
    private TextView txtStep;

    private String phone;

    private Driver driver;
    private Vehicle vehicle;
    private List<Document> docs;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference drivers;
    DatabaseReference vehicles;
    DatabaseReference documents;

    private Dialog messageDialog;

    private android.app.AlertDialog waitingDialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.WHITE);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(Color.BLACK);
        toolbarTitle.setText(getResources().getString(R.string.sign_up_title));

        txtTitle = findViewById(R.id.txtTitle);
        txtStep = findViewById(R.id.txtStep);

        titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.phone_verification));
        titles.add(getResources().getString(R.string.code_verification));
        titles.add(getResources().getString(R.string.account_informations));
        titles.add(getResources().getString(R.string.vehicle_informations));
        titles.add(getResources().getString(R.string.documents));
        titles.add(getResources().getString(R.string.acceptance));

        navigationIndex = 0;

        setupFragment();
        subscribeToBus();

        findViewById(R.id.content).setBackgroundColor(Color.WHITE);

        //Initialize data
        driver = Driver.getInitializedDriver();
        vehicle = new Vehicle();
        docs = new ArrayList<>();

        //Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        drivers = database.getReference("drivers");
        vehicles = database.getReference("vehicles");
        documents = database.getReference("documents");

        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage(getString(R.string.registration_in_progress)).build();
    }

    private void signUpDriver() {

        //Init date
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        driver.setCreatedAt(date);
        vehicle.setCreatedAt(date);
        for (Document document : docs) {
            document.setCreatedAt(date);
        }

        //Registration
        waitingDialog.show();

        String password = CryptoUtils.hashWithSHA256(phone);

        auth.createUserWithEmailAndPassword(driver.getEmail(), password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        driver.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(driver)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        vehicle.setDriverUid(driver.getUid());
                                        vehicle.setUid(vehicles.push().getKey());
                                        vehicles.child(vehicle.getUid())
                                                .setValue(vehicle)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        for (int i=0; i<docs.size(); i++) {
                                                            Document document = docs.get(i);
                                                            document.setUid(documents.push().getKey());
                                                            document.setDriverUid(driver.getUid());
                                                            documents.child(document.getUid())
                                                                    .setValue(document);
                                                        }
                                                        driver.setVehiculeUid(vehicle.getUid());
                                                        drivers.child(driver.getUid())
                                                                .setValue(driver)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        waitingDialog.dismiss();
                                                                        showMessage();
                                                                    }
                                                                });
                                                    }
                                                });
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        showToast(e.getMessage());
                    }
                });
    }

    private void showMessage() {
        messageDialog = new Dialog(SignUpActivity.this);
        messageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        messageDialog.setContentView(R.layout.dialog_message);
        Window window = messageDialog.getWindow();
        if (window != null) {
            window.setLayout(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        }

        TextView txtMessageTitle = messageDialog.findViewById(R.id.txtMessageTitle);
        txtMessageTitle.setText(getString(R.string.message));
        final ImageView imgMessageClose = messageDialog.findViewById(R.id.imgMessageClose);
        TextView txtMessageContent= messageDialog.findViewById(R.id.txtMessageContent);
        txtMessageContent.setText(getString(R.string.successfully_registration));

        imgMessageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgMessageClose.startAnimation(AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.blink));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        messageDialog.dismiss();
                    }
                }, 100);
            }
        });

        messageDialog.show();
    }

    private void subscribeToBus() {
        EventBus.subscribe(EventBus.SUBJECT_SIGN_UP, this, new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (o instanceof Event) {
                    Event event = (Event) o;
                    if (event.getData() != null && event.getSubject() != 0) {
                        switch (event.getSubject()) {
                            case Event.SUBJECT_SIGN_UP_GO_TO_CODE:
                                navigationIndex = 1;
                                phone = (String) event.getData();
                                break;
                            case Event.SUBJECT_SIGN_UP_GO_TO_INFORMATIONS:
                                navigationIndex = 2;
                                phone = (String) event.getData();
                                driver.setPhone(phone);
                                break;
                            case Event.SUBJECT_SIGN_UP_GO_TO_VEHICLE:
                                Driver d = (Driver) event.getData();
                                if (d != null) {
                                    driver.setName(d.getName());
                                    driver.setSurname(d.getSurname());
                                    driver.setEmail(d.getEmail());
                                    driver.setPictureUrl(d.getPictureUrl());
                                }
                                navigationIndex = 3;
                                break;
                            case Event.SUBJECT_SIGN_UP_GO_TO_DOCUMENTS:
                                Vehicle v = (Vehicle) event.getData();
                                if (v != null) {
                                    vehicle = v;
                                }
                                navigationIndex = 4;
                                break;
                            case Event.SUBJECT_SIGN_UP_GO_TO_ACCEPTANCE:
                                List<Document> l = (List<Document>) event.getData();
                                if (l != null) {
                                    docs = l;
                                }
                                navigationIndex = 5;
                                break;
                            case Event.SUBJECT_SIGN_UP_PROCESS:
                                navigationIndex = -1;
                                signUpDriver();
                                break;
                        }
                        setupFragment();
                    }
                }
            }
        });
    }

    private void setupFragment() {
        if (navigationIndex != -1) {
            Fragment fragment = null;

            switch (navigationIndex) {
                case 0:
                    fragment = new SignUpPhoneFragment();
                    break;
                case 1:
                    fragment = SignUpCodeFragment.newInstance(phone);
                    break;
                case 2:
                    fragment = SignUpInformationsFragment.newInstance(phone);
                    break;
                case 3:
                    fragment = new SignUpVehicleFragment();
                    break;
                case 4:
                    fragment = SignUpDocumentsFragment.newInstance(phone);
                    break;
                case 5:
                    fragment = new SignUpAcceptanceFragment();
                    break;
            }

            try {
                getSupportFragmentManager().popBackStack();
            } catch (Exception e) { }

            if (fragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();

                txtTitle.setText(titles.get(navigationIndex));

                String step = String.valueOf(navigationIndex + 1);
                txtStep.setText(step);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unregister(this);
    }
}
