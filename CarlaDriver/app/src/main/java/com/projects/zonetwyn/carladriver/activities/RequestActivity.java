package com.projects.zonetwyn.carladriver.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.components.CircularImageView;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Notification;
import com.projects.zonetwyn.carladriver.models.Rate;
import com.projects.zonetwyn.carladriver.models.Request;
import com.projects.zonetwyn.carladriver.models.Ride;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RequestActivity extends Activity {

    private CircularImageView imgPicture;
    private TextView txtUsername;
    private TextView txtTimer;

    private TextView txtFrom;
    private TextView txtTo;
    private TextView txtDistance;
    private TextView txtDuration;
    private TextView txtPrice;

    private Button btnAccept;
    private Button btnReject;

    private Request request;

    private MediaPlayer mediaPlayer;

    private static final long START_TIME_IN_MILLIS = 20000;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long leftTimeInMillis = START_TIME_IN_MILLIS;

    private FirebaseDatabase database;
    private DatabaseReference requests;
    private DatabaseReference rides;
    private DatabaseReference notifications;
    private DatabaseReference drivers;
    private DatabaseReference rates;

    private android.app.AlertDialog waitingDialog;

    private SessionManager sessionManager;

    private Rate rate;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("REQUEST")) {
                request = intent.getParcelableExtra("REQUEST");
            }
        }

        imgPicture = findViewById(R.id.imgPicture);
        txtUsername = findViewById(R.id.txtUsername);
        txtTimer = findViewById(R.id.txtTimer);

        txtFrom = findViewById(R.id.txtPlaceFrom);
        txtTo = findViewById(R.id.txtPlaceTo);
        txtDistance = findViewById(R.id.txtDistance);
        txtDuration = findViewById(R.id.txtDuration);
        txtPrice = findViewById(R.id.txtPrice);

        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        waitingDialog = new SpotsDialog.Builder().setContext(this).setMessage(getString(R.string.sending_your_response)).build();

        //Init firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("requests");
        rides = database.getReference("rides");
        notifications = database.getReference("notifications");
        drivers = database.getReference("drivers");
        rates = database.getReference("rates");
        sessionManager = SessionManager.getInstance(getApplicationContext());
        rate = sessionManager.getRate();

        if (request != null) {
            updateUI();
            updateCountDownText();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTimer();
                }
            }, 500);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(leftTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                leftTimeInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                handleResponse(false);
            }
        }.start();

        timerRunning = true;
    }

    private void updateCountDownText() {
        int minutes = (int) (leftTimeInMillis / 1000) / 60;
        int seconds = (int) (leftTimeInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        txtTimer.setText(timeLeftFormatted);
    }

    private void updateUI() {
        mediaPlayer = MediaPlayer.create(RequestActivity.this, R.raw.ringtone);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (timerRunning) {
                    mediaPlayer.start();
                } else {
                    mediaPlayer.stop();
                }
            }
        });

        Picasso.get().load(request.getClient().getPictureUrl()).into(imgPicture);
        String username = request.getClient().getSurname() + request.getClient().getName();
        txtUsername.setText(username);

        txtFrom.setText(request.getStartingPoint().getAddress());
        txtTo.setText(request.getArrivalPoint().getAddress());
        txtDistance.setText(request.getDistance());
        txtDuration.setText(request.getDuration());

        rates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot rateSnap: dataSnapshot.getChildren()) {
                    rate = rateSnap.getValue(Rate.class);
                    if (rate != null) {
                    } else {
                        showToast("NULL RATE");
                    }
                }
                if (rate != null) {
                    sessionManager.updateRate(rate);
                    double percent = (100 - (double) rate.getRate()) / 100;
                    double p = (request.getPrice() * percent);
                    String price = "" + getTruncate(String.valueOf(p)) + " €";
                    txtPrice.setText(price);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResponse(true);
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResponse(false);
            }
        });
    }

    private String getTruncate(String price) {
        if (price.length() > 5) {
            return price.substring(0, 5);
        }
        return price;
    }

    private void handleResponse(final boolean accepted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        //Setting up timer and player
        mediaPlayer.stop();
        timerRunning = false;
        countDownTimer.cancel();

        waitingDialog.show();

        //Notification, Ride, Request
        final Notification notification = new Notification();
        notification.setCreatedAt(date);
        notification.setTitle(getString(R.string.request_report));
        notification.setStatus("IN_WAITING");
        notification.setUserUid(request.getClientUid());

        final Ride ride = new Ride();
        ride.setCreatedAt(date);
        ride.setDuration(request.getDuration());
        ride.setDistance(request.getDistance());

        double percent = 0.8;
        if (rate != null) {
            percent = (100 - (double) rate.getRate()) / 100;
        }
        ride.setPrice(request.getPrice() * percent);
        ride.setStatus("IN_WAITING");

        ride.setStartingPointUid(request.getStartingPointUid());
        ride.setArrivalPointUid(request.getArrivalPointUid());
        ride.setClientUid(request.getClientUid());
        ride.setDriverUid(request.getDriverUid());

        if (accepted) {
            notification.setType(Notification.TYPE_REQUEST_ACCEPT);
            notification.setContent(getString(R.string.request_accepted));
            request.setStatus("ACCEPTED");
        } else {
            notification.setType(Notification.TYPE_REQUEST_REJECT);
            notification.setContent(getString(R.string.request_rejected));
            request.setStatus("REJECTED");
        }

        Request requestCopy = copyRequest(request);
        //Saving data
        requests.child(request.getUid())
                .setValue(requestCopy)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        notification.setUid(notifications.push().getKey());
                        notifications.child(notification.getUid())
                            .setValue(notification)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (accepted) {
                                        ride.setUid(rides.push().getKey());
                                        rides.child(ride.getUid())
                                            .setValue(ride)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Driver driver = sessionManager.getLoggedDriver();
                                                    driver.setOnline(false);
                                                    drivers.child(driver.getUid())
                                                            .setValue(driver)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    ride.setClient(request.getClient());
                                                                    ride.setStartingPoint(request.getStartingPoint());
                                                                    ride.setArrivalPoint(request.getArrivalPoint());
                                                                    sessionManager.updateRide(ride);
                                                                    onResponseSent(accepted);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    waitingDialog.dismiss();
                                                                    showToast(getString(R.string.error));
                                                                }
                                                            });

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    waitingDialog.dismiss();
                                                    showToast(getString(R.string.error));
                                                }
                                            });
                                    } else {
                                        waitingDialog.dismiss();
                                        onResponseSent(accepted);
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                    showToast(getString(R.string.error));
                                }
                            });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        showToast(getString(R.string.error));
                    }
                });

    }

    private Request copyRequest(Request request) {
        Request copy = new Request();
        copy.setUid(request.getUid());
        copy.setCreatedAt(request.getCreatedAt());
        copy.setDuration(request.getDuration());
        copy.setDistance(request.getDistance());
        copy.setPrice(request.getPrice());
        copy.setStatus(request.getStatus());
        copy.setStartingPointUid(request.getStartingPointUid());
        copy.setArrivalPointUid(request.getArrivalPointUid());
        copy.setClientUid(request.getClientUid());
        copy.setDriverUid(request.getDriverUid());

        return copy;
    }

    private void onResponseSent(boolean accepted) {
        showToast(getString(R.string.response_successfull));
        waitingDialog.dismiss();
        if (accepted) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                    intent.putExtra("ACTION_GO_TO_RIDE", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    try {
                        finish();
                    } catch (Exception e) {}
                }
            }, 200);
        } else {
            finish();
        }
    }

    private void showToast(String message) {
        Toast.makeText(RequestActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
