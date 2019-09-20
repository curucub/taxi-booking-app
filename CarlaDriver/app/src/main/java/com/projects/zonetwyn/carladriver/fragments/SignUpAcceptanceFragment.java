package com.projects.zonetwyn.carladriver.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpAcceptanceFragment extends Fragment {

    private Context context;
    
    private TextView txtTerms;

    private Dialog messageDialog;

    private CheckBox chbVtc;
    private CheckBox chbTerms;
    
    private Button btnSignUp;

    public SignUpAcceptanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_sign_up_acceptance, container, false);
        
        chbVtc = rootView.findViewById(R.id.chbVtc);
        chbTerms = rootView.findViewById(R.id.chbTerms);

        txtTerms = rootView.findViewById(R.id.txtTerms);
        
        btnSignUp = rootView.findViewById(R.id.btnSignUp);
        
        handleEvents();
        
        return rootView;
    }

    private void handleEvents() {
        txtTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsOfUse();
            }
        });
        
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpClicked();
            }
        });
    }

    private void onSignUpClicked() {
        if (chbTerms.isChecked()) {
            Event event = new Event(Event.SUBJECT_SIGN_UP_PROCESS, new Object());
            EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
        } else {
            showToast(getString(R.string.you_must_accept_terms));
        }
    }

    private void showTermsOfUse() {
        messageDialog = new Dialog(context);
        messageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        messageDialog.setContentView(R.layout.dialog_message);
        Window window = messageDialog.getWindow();
        window.setLayout(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);

        TextView txtMessageTitle = messageDialog.findViewById(R.id.txtMessageTitle);
        txtMessageTitle.setText(getString(R.string.terms));
        final ImageView imgMessageClose = messageDialog.findViewById(R.id.imgMessageClose);
        TextView txtMessageContent= messageDialog.findViewById(R.id.txtMessageContent);
        txtMessageContent.setText(getString(R.string.terms_content));

        imgMessageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgMessageClose.startAnimation(AnimationUtils.loadAnimation(context, R.anim.blink));
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

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
