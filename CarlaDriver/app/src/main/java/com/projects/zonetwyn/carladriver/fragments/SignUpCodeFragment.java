package com.projects.zonetwyn.carladriver.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class SignUpCodeFragment extends Fragment {

    public static String ARG_PHONE = "Phone";

    private Context context;

    private Button btnNext;

    private String phone;

    private EditText edtCode;

    private FirebaseAuth auth;

    private String verificationId;

    public SignUpCodeFragment() {
        // Required empty public constructor
    }

    public static SignUpCodeFragment newInstance(String phone) {
        SignUpCodeFragment fragment = new SignUpCodeFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_sign_up_code, container, false);

        btnNext = rootView.findViewById(R.id.btnNext);
        edtCode = rootView.findViewById(R.id.edtCode);

        handleEvents();

        //Init Firebase
        auth = FirebaseAuth.getInstance();

        if (phone != null) {
            sendCode();
        }

        return rootView;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (auth.getCurrentUser() != null) {
                                auth.signOut();
                                goToInformations(phone);
                            } else {
                                goToInformations(phone);
                            }
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showToast("Wrong code!");
                            }
                        }
                    }
                });
    }


    private void handleEvents() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code  = (edtCode.getText() != null) ? edtCode.getText().toString() : "";
                if (!code.isEmpty()) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    showToast(getString(R.string.phone_can_not_be_empty));
                }
            }
        });
    }

    private void goToInformations(String verifiedPhone) {
        Event event = new Event(Event.SUBJECT_SIGN_UP_GO_TO_INFORMATIONS, verifiedPhone);
        EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
    }

    private void sendCode() {

        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.checking_your_number)).build();
        waitingDialog.show();

        PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();

        phoneAuthProvider.verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, getActivity(), new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                edtCode.setText(phoneAuthCredential.getSmsCode());
                waitingDialog.dismiss();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToInformations(phone);
                    }
                }, 1000);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                waitingDialog.dismiss();
                edtCode.setText(e.getMessage());
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                waitingDialog.dismiss();
                verificationId = s;
                showToast(getString(R.string.enter_the_code_you_just_received));
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                waitingDialog.dismiss();
                verificationId = s;
                showToast(getString(R.string.enter_the_code_you_just_received));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
