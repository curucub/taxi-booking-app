package com.projects.zonetwyn.carla.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Code;
import com.projects.zonetwyn.carla.models.Point;
import com.projects.zonetwyn.carla.models.Rate;
import com.projects.zonetwyn.carla.models.Request;
import com.projects.zonetwyn.carla.services.NotificationService;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AddCodeActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference points;
    private DatabaseReference requests;
    private DatabaseReference codes;

    private Request request;
    private Point startingPoint;
    private Point arrivalPoint;
    private List<Code> codeDatas;

    private android.app.AlertDialog waitingDialog;

    private Toolbar toolbar;
    private TextView toolbarTitle;

    private Button btnAddCode;
    private Button btnSendRequest;
    private EditText edtCode;

    private Dialog messageDialog;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_code);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("REQUEST")) {
            request = intent.getParcelableExtra("REQUEST");
            startingPoint = request.getStartingPoint();
            arrivalPoint = request.getArrivalPoint();
        }

        sessionManager = SessionManager.getInstance(getApplicationContext());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getResources().getString(R.string.code_verification));

        edtCode = findViewById(R.id.edtCode);
        btnAddCode = findViewById(R.id.btnAddCode);
        btnAddCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeVerification();
            }
        });

        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        database = FirebaseDatabase.getInstance();
        points = database.getReference("points");
        requests = database.getReference("requests");
        codes = database.getReference("codes");
    }

    private void codeVerification() {
        final String code = (edtCode.getText() != null) ? edtCode.getText().toString() : "";
        if (!code.isEmpty()) {
            Query query = codes.orderByChild("value").equalTo(code);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    codeDatas = new ArrayList<>();
                    for (DataSnapshot codeSnapshot: dataSnapshot.getChildren()) {
                        Code codeData = codeSnapshot.getValue(Code.class);
                        if (codeData != null) {
                            codeDatas.add(codeData);
                        }
                    }
                    if (codeDatas.size() > 0) {
                        Code codeDat = codeDatas.get(0);
                        if (codeDat != null) {
                            double price = request.getPrice() * ((100 - codeDat.getRate()) / 100);

                            Rate rate = sessionManager.getRate();
                            double percent = 0.8;
                            if (rate != null) {
                                percent = (100 - (double) rate.getRate()) / 100;
                            }
                            price = price * percent;
                            request.setPrice(price);

                            String message = getString(R.string.code_sucess) + " <h1>" + getTruncate(String.valueOf(price)) + " €</h1>";
                            showMessage(message);
                            btnAddCode.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(AddCodeActivity.this, getString(R.string.code_wrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(AddCodeActivity.this, getString(R.string.code_needed), Toast.LENGTH_SHORT).show();
        }
    }

    private String getTruncate(String data) {
        if (data.length() > 5) {
            return data.substring(0, 5);
        }
        return data;
    }

    private void showMessage(String message) {
        messageDialog = new Dialog(AddCodeActivity.this);
        messageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        messageDialog.setContentView(R.layout.dialog_message);
        Window window = messageDialog.getWindow();
        window.setLayout(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        TextView txtMessageTitle = messageDialog.findViewById(R.id.txtMessageTitle);
        txtMessageTitle.setText(getString(R.string.message));
        final ImageView imgMessageClose = messageDialog.findViewById(R.id.imgMessageClose);
        TextView txtMessageContent= messageDialog.findViewById(R.id.txtMessageContent);
        txtMessageContent.setText(Html.fromHtml(message));

        imgMessageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgMessageClose.startAnimation(AnimationUtils.loadAnimation(AddCodeActivity.this, R.anim.blink));
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


    private void sendRequest() {
        if (request != null && startingPoint != null && arrivalPoint != null) {
            waitingDialog = new SpotsDialog.Builder().setContext(AddCodeActivity.this).setMessage(getString(R.string.sending_request)).build();
            waitingDialog.show();
            startingPoint.setUid(points.push().getKey());
            points.child(startingPoint.getUid())
                    .setValue(startingPoint)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            arrivalPoint.setUid(points.push().getKey());
                            points.child(arrivalPoint.getUid())
                                    .setValue(arrivalPoint)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            request.setStartingPointUid(startingPoint.getUid());
                                            request.setArrivalPointUid(arrivalPoint.getUid());
                                            request.setUid(requests.push().getKey());
                                            requests.child(request.getUid())
                                                    .setValue(request)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            waitingDialog.dismiss();
                                                            showToast(getString(R.string.successfully_sent));
                                                            startNotificationService();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

        } else {
            showToast("Seems like something is missing!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(AddCodeActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void startNotificationService() {
        Intent intent = new Intent(AddCodeActivity.this, NotificationService.class);
        intent.putExtra("request", true);
        startService(intent);
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
}
