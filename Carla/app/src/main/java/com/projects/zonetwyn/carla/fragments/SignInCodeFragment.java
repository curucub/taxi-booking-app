package com.projects.zonetwyn.carla.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.activities.MainActivity;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.utils.CryptoUtils;

import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignInCodeFragment extends Fragment {

    public static String ARG_PHONE = "Phone";

    private String phone;

    private Context context;

    private EditText edtCode;
    private Button btnSignIn;

    private FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference clients;

    private Client client;

    private SessionManager sessionManager;

    private String verificationId;

    private android.app.AlertDialog waitingDialog;

    public SignInCodeFragment() {
        // Required empty public constructor
    }

    public static SignInCodeFragment newInstance(String phone) {
        SignInCodeFragment fragment = new SignInCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            phone = getArguments().getString(ARG_PHONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        sessionManager = SessionManager.getInstance(context.getApplicationContext());

        View rootView = inflater.inflate(R.layout.fragment_sign_in_code, container, false);

        edtCode = rootView.findViewById(R.id.edtCode);
        btnSignIn = rootView.findViewById(R.id.btnSignIn);

        handleEvents();

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        clients = database.getReference("clients");

        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.sign_in_progress)).build();

        verificationId = "";

        if (phone != null) {
            sendCode();
        }

        return rootView;
    }

    private void handleEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verificationId.isEmpty()) {
                    String code  = (edtCode.getText() != null) ? edtCode.getText().toString() : "";
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (auth.getCurrentUser() != null) {
                                auth.signOut();
                                signInWithEmail();
                            } else {
                                signInWithEmail();
                            }
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showToast("Wrong code!");
                            }
                        }
                    }
                });
    }

    private void processSignIn(Client client) {
        if (sessionManager != null) {
            sessionManager.signInClient(client);
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void signInWithEmail() {
        // Is what to change
        final String completedPhone = "+33" + phone.trim();
        if (client != null) {
            auth.signInWithEmailAndPassword(client.getEmail(), CryptoUtils.hashWithSHA256("+330766811008"))
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            waitingDialog.dismiss();
                            processSignIn(client);
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

    private void sendCode() {
        waitingDialog.show();

        final String completedPhone = "+33" + phone.trim();

        Query query = clients.orderByChild("phone").equalTo(completedPhone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                client = null;
                for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                    client = clientSnapshot.getValue(Client.class);
                }
                if (client != null) {
                    PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
                    phoneAuthProvider.verifyPhoneNumber(completedPhone, 60, TimeUnit.SECONDS, getActivity(), new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            edtCode.setText(phoneAuthCredential.getSmsCode());
                            signInWithEmail();
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            waitingDialog.dismiss();
                            edtCode.setText(e.getMessage());
                        }

                        @Override
                        public void onCodeAutoRetrievalTimeOut(String s) {
                            waitingDialog.dismiss();
                            if (verificationId.isEmpty()) {
                                verificationId = s;
                                showToast("Please enter the code that has been sent");
                            }
                        }

                        @Override
                        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            waitingDialog.dismiss();
                            if (verificationId.isEmpty()) {
                                verificationId = s;
                                showToast("Please enter the code that has been sent");
                            }
                        }
                    });
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

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
