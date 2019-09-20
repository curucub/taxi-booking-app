package com.projects.zonetwyn.carladriver.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.components.CircularImageView;
import com.projects.zonetwyn.carladriver.database.SessionManager;
import com.projects.zonetwyn.carladriver.models.Bill;
import com.projects.zonetwyn.carladriver.models.Client;
import com.projects.zonetwyn.carladriver.models.Driver;
import com.projects.zonetwyn.carladriver.models.Notification;
import com.projects.zonetwyn.carladriver.models.Payment;
import com.projects.zonetwyn.carladriver.models.Point;
import com.projects.zonetwyn.carladriver.models.Ride;
import com.squareup.picasso.Picasso;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

import static android.content.Context.NOTIFICATION_SERVICE;

public class RequestFragment extends Fragment {

    private TextView txtError;
    private NestedScrollView nscContent;

    private RadioGroup radGps;
    private RadioButton btnGoogleMap;
    private RadioButton btnWaze;

    private CircularImageView imgPicture;
    private TextView txtUsername;
    private TextView txtFrom;
    private TextView txtTo;
    private TextView txtPrice;

    private Button btnPickUpClient;
    private Button btnReachedToClient;
    private Button btnStartRide;
    private Button btnEndRide;

    private SessionManager sessionManager;
    private Context context;

    private Ride ride;

    private boolean isGoogle;

    private FirebaseDatabase database;
    private DatabaseReference rides;
    private DatabaseReference drivers;
    private DatabaseReference notifications;
    private DatabaseReference bills;
    private DatabaseReference payments;
    private DatabaseReference points;
    private DatabaseReference clients;

    private NotificationManager notificationManager;

    private static final String CHANNEL_ID = "carla-driver-id-2";
    private static final String CHANNEL_NAME = "carla-driver-channel-2";

    private android.app.AlertDialog waitingDialog;

    private Driver driver;

    private Client client;

    private double amount;
    private String rideUid;

    private static final String URL = "http://apicarlademo.eu-4.evennode.com/api/payments";

    public RequestFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        View rootView = inflater.inflate(R.layout.fragment_request, container, false);

        sessionManager = SessionManager.getInstance(context.getApplicationContext());
        ride = sessionManager.getCurrentRide();

        txtError = rootView.findViewById(R.id.txtError);
        nscContent = rootView.findViewById(R.id.nsvContent);

        radGps = rootView.findViewById(R.id.radGps);
        btnGoogleMap = rootView.findViewById(R.id.btnGoogleMap);
        btnWaze = rootView.findViewById(R.id.btnWaze);

        imgPicture = rootView.findViewById(R.id.imgPicture);
        txtUsername = rootView.findViewById(R.id.txtUsername);
        txtFrom = rootView.findViewById(R.id.txtPlaceFrom);
        txtTo = rootView.findViewById(R.id.txtPlaceTo);
        txtPrice = rootView.findViewById(R.id.txtPrice);

        btnPickUpClient = rootView.findViewById(R.id.btnPickUpClient);
        btnReachedToClient = rootView.findViewById(R.id.btnReachedToClient);
        btnStartRide = rootView.findViewById(R.id.btnStartRide);
        btnEndRide = rootView.findViewById(R.id.btnEndRide);

        database = FirebaseDatabase.getInstance();
        rides = database.getReference("rides");
        drivers = database.getReference("drivers");
        notifications = database.getReference("notifications");
        bills = database.getReference("bills");
        payments = database.getReference("payments");
        points = database.getReference("points");
        clients = database.getReference("clients");

        driver = sessionManager.getLoggedDriver();

        if (ride != null) {
            handleEvents();
            txtError.setVisibility(View.GONE);
            nscContent.setVisibility(View.VISIBLE);
            updateUI();
        } else {
            nscContent.setVisibility(View.GONE);
            txtError.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager != null) {
            String gpsPreference = sessionManager.readGpsPreference();
            if (gpsPreference != null && !gpsPreference.isEmpty()) {
                setupRadio(gpsPreference);
            }
        }
    }

    private void updateUI() {
        Picasso.get().load(ride.getClient().getPictureUrl()).into(imgPicture);
        String username = ride.getClient().getSurname() + ride.getClient().getName();
        txtUsername.setText(username);

        txtFrom.setText(ride.getStartingPoint().getAddress());
        txtTo.setText(ride.getArrivalPoint().getAddress());

        String price = "" + getTruncate(String.valueOf(ride.getPrice())) + " €";
        txtPrice.setText(price);

        client = new Client();
        client.setCardCode(ride.getClient().getCardCode());
        client.setCardNumber(ride.getClient().getCardNumber());
        client.setCardExpirationDate(ride.getClient().getCardExpirationDate());
        client.setCardExpirationDate(ride.getClient().getCardExpirationDate());
        client.setName(ride.getClient().getName());
        client.setSurname(ride.getClient().getSurname());
        client.setUid(ride.getClient().getUid());

        amount = ride.getPrice();
        rideUid = ride.getUid();
    }

    private String getTruncate(String price) {
        if (price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
    }

    private void setupRadio(String gpsPreference) {
        if (gpsPreference.contains("GOOGLE")) {
            btnGoogleMap.setChecked(true);
            btnWaze.setChecked(false);
            isGoogle = true;
        } else {
            btnGoogleMap.setChecked(false);
            btnWaze.setChecked(true);
            isGoogle = false;
        }
    }

    private void handleEvents() {
        //Gps
        radGps.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.btnGoogleMap:
                        applyGpsPreference("GOOGLE MAP");
                        break;
                    case R.id.btnWaze:
                        applyGpsPreference("WAZE");
                        break;
                }
            }
        });

        //Buttons
        btnPickUpClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null) {
                    setupPickClient();
                }
            }
        });

        btnReachedToClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null) {
                    setupReachedToClient();
                }
            }
        });

        btnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null) {
                    setupStartRide();
                }
            }
        });

        btnEndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null) {
                    setupEndRide();
                }
            }
        });
    }

    private void setupReachedToClient() {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Notification notification = new Notification();
        notification.setUserUid(ride.getClientUid());
        notification.setCreatedAt(date);
        notification.setTitle(getString(R.string.app_name));
        notification.setContent(getString(R.string.driver_reach));
        notification.setType(Notification.TYPE_DRIVER_REACH);
        notification.setStatus("IN_WAITING");

        notification.setUid(notifications.push().getKey());
        notifications.child(notification.getUid())
                .setValue(notification);
    }

    private void setupEndRide() {
        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.updating_data)).build();
        waitingDialog.show();
        updateRide("ENDED");
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        final Notification notification = new Notification();
        notification.setUserUid(ride.getClientUid());
        notification.setCreatedAt(date);
        notification.setTitle(getString(R.string.app_name));
        notification.setContent(getString(R.string.ride_ended));
        notification.setType(Notification.TYPE_RIDE_END);
        notification.setStatus("IN_WAITING");

        notification.setUid(notifications.push().getKey());
        notifications.child(notification.getUid())
                     .setValue(notification)
                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                         @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             String message = getString(R.string.you_won) + " " + getTruncate(String.valueOf(ride.getPrice())) + " €";
                             notification.setContent(message);
                             showNotification(notification);
                             final Driver driver = sessionManager.getLoggedDriver();
                             driver.setOnline(true);
                             drivers.child(driver.getUid())
                                     .setValue(driver)
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             updateBill(driver.getUid());
                                         }
                                     });
                         }
                     });
    }

    private void updateBill(final String uid) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Bill bill = new Bill();
        bill.setClientUid(ride.getClientUid());
        bill.setCreatedAt(date);
        bill.setRideUid(ride.getUid());
        bill.setStatus("NOT_CONSULTED");

        bill.setUid(bills.push().getKey());
        bills.child(bill.getUid())
                .setValue(bill)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Query query = payments.orderByChild("driverUid").equalTo(uid);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                List<Payment> paymentList = new ArrayList<>();
                                for (DataSnapshot paymentSnap : dataSnapshot.getChildren()) {
                                    Payment payment = paymentSnap.getValue(Payment.class);
                                    paymentList.add(payment);
                                }
                                updatePayment(paymentList, uid);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePayment(List<Payment> paymentList, String uid) {
        String date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        if (!paymentList.isEmpty()) {
            Payment currentPayment = null;
            for (Payment payment : paymentList) {
                if (payment.getCreatedAt().contains(date)) {
                    currentPayment = payment;
                    break;
                }
            }
            if (currentPayment != null) {
                double amount = currentPayment.getAmount() + ride.getPrice();
                currentPayment.setAmount(amount);

                payments.child(currentPayment.getUid())
                        .setValue(currentPayment)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                updateDriver();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                createPayment(uid);
            }
        } else {
            createPayment(uid);
        }
    }

    private void createPayment(String uid) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Payment payment = new Payment();
        payment.setAmount(ride.getPrice());
        payment.setDriverUid(uid);
        payment.setCreatedAt(date);
        payment.setStatus("NOT_CONSULTED");

        payment.setUid(payments.push().getKey());
        payments.child(payment.getUid())
                .setValue(payment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateDriver();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDriver() {
        double todayEarnings =  driver.getEarningsDaily() + ride.getPrice();
        double weekEarnings = driver.getEarningsWeekly() + ride.getPrice();
        driver.setEarningsDaily(todayEarnings);
        driver.setEarningsWeekly(weekEarnings);
        int ridesCount = driver.getRidesCount() + 1;
        driver.setRidesCount(ridesCount);
        drivers.child(driver.getUid())
                .setValue(driver)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.updateDriver(driver);
                        sessionManager.cleanRide();
                        processPayment();
                    }
                });
    }

    private void processPayment() {
        String date = client.getCardExpirationDate();
        int year = Integer.parseInt(date.split("/")[0]);
        int month = Integer.parseInt(date.split("/")[1]);
        Card card = new Card(client.getCardNumber(), month, year, client.getCardCode());
        Stripe stripe = new Stripe(context, "pk_test_i05lHwIG9NXYdSMXX0VOUnol");
        stripe.createToken(
                card,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        String amountString = getTruncate(String.valueOf(amount));
                        int amountInt = Integer.parseInt(amountString);
                        String tokenId = token.getId();

                        //Send token ID to server using volley
                        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024*1024);
                        Network network = new BasicNetwork(new HurlStack());
                        RequestQueue requestQueue = new RequestQueue(cache, network);
                        requestQueue.start();

                        JSONObject object = new JSONObject();
                        try {
                            object.put("token", tokenId);
                            object.put("amount", amount);
                            object.put("rideUid", rideUid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, object, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                if (waitingDialog.isShowing())
                                    waitingDialog.dismiss();
                                //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (waitingDialog.isShowing())
                                    waitingDialog.dismiss();
                                Toast.makeText(context, "Désolé, nous n'avons pas pu effectuer votre paiement", Toast.LENGTH_LONG).show();
                            }
                        });
                        requestQueue.add(request);
                    }
                    public void onError(Exception error) {
                        if (waitingDialog.isShowing())
                            waitingDialog.dismiss();
                        Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setupStartRide() {
        if (btnGoogleMap.isChecked()) {
            updateRide("IN_PROGRESS");
            openGoogleMap(ride.getArrivalPoint());
        } else if (btnWaze.isChecked()) {
            updateRide("IN_PROGRESS");
            openWaze(ride.getArrivalPoint());
        } else {
            Toast.makeText(context, getString(R.string.please_check), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPickClient() {
        if (btnGoogleMap.isChecked()) {
            updateRide("STARTED");
            openGoogleMap(ride.getStartingPoint());
        } else if (btnWaze.isChecked()) {
            updateRide("STARTED");
            openWaze(ride.getStartingPoint());
        } else {
            Toast.makeText(context, getString(R.string.please_check), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRide(String status) {
        ride.setStatus(status);

        Ride rideCopy = copyRide(ride);
        rides.child(ride.getUid())
                .setValue(rideCopy)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.updateRide(ride);
                        nscContent.setVisibility(View.GONE);
                        txtError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private Ride copyRide(Ride ride) {
        Ride copy = new Ride();
        copy.setUid(ride.getUid());
        copy.setCreatedAt(ride.getCreatedAt());
        copy.setDuration(ride.getDuration());
        copy.setDistance(ride.getDistance());
        copy.setPrice(ride.getPrice());
        copy.setStatus(ride.getStatus());
        copy.setStartingPointUid(ride.getStartingPointUid());
        copy.setArrivalPointUid(ride.getArrivalPointUid());
        copy.setClientUid(ride.getClientUid());
        copy.setDriverUid(ride.getDriverUid());

        return copy;
    }

    private void openWaze(Point point) {
        String url = "https://waze.com/ul?ll=" + point.getLatitude() + "," + point.getLongitude();
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
        startActivity( intent );
    }

    private void openGoogleMap(Point point) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + point.getLatitude() + "," + point.getLongitude() + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void applyGpsPreference(String gpsPreference) {
        sessionManager.saveGpsPreference(gpsPreference);
        setupRadio(gpsPreference);
    }

    private NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(
                    NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private boolean checkVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void showNotification(Notification notification) {

        if (checkVersion()) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(context, CHANNEL_ID)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getContent())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true);

            getNotificationManager().notify(1000, notificationBuilder.build());
        } else {
            android.app.Notification n  = new android.app.Notification.Builder(context)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getContent())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .build();


            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(1000, n);
        }
    }
}
