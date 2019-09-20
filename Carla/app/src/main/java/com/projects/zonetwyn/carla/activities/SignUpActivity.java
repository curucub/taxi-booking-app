package com.projects.zonetwyn.carla.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.fragments.SignUpCodeFragment;
import com.projects.zonetwyn.carla.fragments.SignUpInformationsFragment;
import com.projects.zonetwyn.carla.fragments.SignUpPhoneFragment;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.utils.CryptoUtils;
import com.projects.zonetwyn.carla.utils.Event;
import com.projects.zonetwyn.carla.utils.EventBus;

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

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference clients;

    private Dialog messageDialog;

    private Client client;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init Calligraphy
        /*CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Montserrat-Medium.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/

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

        navigationIndex = 0;

        setupFragment();
        subscribeToBus();

        findViewById(R.id.content).setBackgroundColor(Color.WHITE);

        //Initialize data
        client = new Client();

        //Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        clients = database.getReference("clients");

    }

    private void signUpClient() {
        //Init date
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        client.setCreatedAt(date);

        //Registration
        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage(getString(R.string.registration_in_progress)).build();
        waitingDialog.show();

        String password = CryptoUtils.hashWithSHA256(phone);

        auth.createUserWithEmailAndPassword(client.getEmail(), password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        client.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        clients.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(client)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        waitingDialog.dismiss();
                                        showMessage();
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

        messageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
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
                                client.setPhone(phone);
                                break;
                            case Event.SUBJECT_SIGN_UP_PROCESS:
                                Client c = (Client) event.getData();
                                if (c != null) {
                                    client.setName(c.getName());
                                    client.setSurname(c.getSurname());
                                    client.setEmail(c.getEmail());
                                    client.setPictureUrl(c.getPictureUrl());
                                }
                                signUpClient();
                                navigationIndex = -1;
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
