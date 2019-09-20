package com.projects.zonetwyn.carla.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.database.SessionManager;
import com.projects.zonetwyn.carla.models.Client;
import com.stripe.android.model.Card;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentFragment extends Fragment {

    private Context context;

    private RelativeLayout rllCard;
    private TextView txtCardNumber;
    private ImageView imgDelete;

    private EditText edtCardNumber;
    private EditText edtCardYear;
    private EditText edtCardMonth;
    private EditText edtCardCVV;

    private Button btnAddCard;

    private SessionManager sessionManager;
    private Client client;

    private FirebaseDatabase database;
    private DatabaseReference clients;

    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        sessionManager = SessionManager.getInstance(context.getApplicationContext());

        View rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        rllCard = rootView.findViewById(R.id.rllCard);
        txtCardNumber = rootView.findViewById(R.id.txtCardNumber);
        imgDelete = rootView.findViewById(R.id.imgDelete);

        edtCardNumber = rootView.findViewById(R.id.edtCardNumber);
        edtCardYear = rootView.findViewById(R.id.edtCardYear);
        edtCardMonth = rootView.findViewById(R.id.edtCardMonth);
        edtCardCVV = rootView.findViewById(R.id.edtCardCVV);

        btnAddCard = rootView.findViewById(R.id.btnAddCard);

        database = FirebaseDatabase.getInstance();
        clients = database.getReference("clients");

        handleEvents();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        client = sessionManager.getLoggedClient();
        if (client.getCardNumber() != null && client.getCardExpirationDate() != null && client.getCardCode() != null) {
            rllCard.setVisibility(View.VISIBLE);
            String cardNumber = "xxxx xxxx xxxx " + client.getCardNumber().substring(12, 16);
            txtCardNumber.setText(cardNumber);
        } else {
            rllCard.setVisibility(View.GONE);
        }
    }

    private void handleEvents() {
        btnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCard();
            }
        });

        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCard();
            }
        });
    }

    private void deleteCard() {
        client.setCardExpirationDate(null);
        client.setCardNumber(null);
        client.setCardCode(null);
        clients.child(client.getUid())
                .setValue(client)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        rllCard.setVisibility(View.GONE);
                        sessionManager.updateClient(client);
                    }
                });
    }

    private void verifyCard() {
        String number = (edtCardNumber.getText() != null) ? edtCardNumber.getText().toString() : "";
        String year = (edtCardYear.getText() != null) ? edtCardYear.getText().toString() : "";
        String month = (edtCardMonth.getText() != null) ? edtCardMonth.getText().toString() : "";
        String cvv = (edtCardCVV.getText() != null) ? edtCardCVV.getText().toString() : "";

        if (number.isEmpty() || year.isEmpty() || month.isEmpty() || cvv.isEmpty()) {
            showToast(getString(R.string.empty_field));
        } else {
            if (number.length() != 16) {
                 showToast(getString(R.string.number_not_valid));
            } else {
                Card card = new Card(number, Integer.parseInt(month), Integer.parseInt(year), cvv);
                if (!card.validateCard()) {
                    showToast(getString(R.string.card_not_valid));
                } else {
                    client.setCardNumber(number);
                    client.setCardCode(cvv);
                    client.setCardExpirationDate(year + "/" + month);
                    clients.child(client.getUid())
                            .setValue(client)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    rllCard.setVisibility(View.VISIBLE);
                                    String cardNumber = "xxxx xxxx xxxx " + client.getCardNumber().substring(12, 16);
                                    txtCardNumber.setText(cardNumber);
                                    edtCardNumber.setText("");
                                    edtCardYear.setText("");
                                    edtCardMonth.setText("");
                                    edtCardCVV.setText("");
                                    sessionManager.updateClient(client);
                                }
                            });
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
