package com.projects.zonetwyn.carla.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projects.zonetwyn.carla.R;
import com.projects.zonetwyn.carla.models.Client;
import com.projects.zonetwyn.carla.utils.Event;
import com.projects.zonetwyn.carla.utils.EventBus;
import com.projects.zonetwyn.carla.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpInformationsFragment extends Fragment {


    private Context context;

    private ImageView imgPicture;
    private FloatingActionButton bntGallery;
    private FloatingActionButton btnPicture;

    private EditText edtName;
    private EditText edtSurname;
    private EditText edtEmail;
    private Button btnSignUp;

    //Pick Image
    public static final int REQUEST_SELECT_PICTURE = 0x01;
    public static final int REQUEST_IMAGE_CAPTURE = 0x03;
    private Bitmap selectedBitmap;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri selectedUri;

    private String pictureUrl;

    private ImageUtils imageUtils;

    public static String ARG_PHONE = "Phone";
    private String phone;

    private android.app.AlertDialog waitingDialog;

    private Dialog messageDialog;
    private CheckBox chbTerms;
    private TextView txtTerms;

    private String currentPhotoPath;

    public SignUpInformationsFragment() {

    }

    public static SignUpInformationsFragment newInstance(String phone) {
        SignUpInformationsFragment fragment = new SignUpInformationsFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_sign_up_informations, container, false);

        imgPicture = rootView.findViewById(R.id.imgPicture);
        bntGallery = rootView.findViewById(R.id.btnGallery);
        btnPicture = rootView.findViewById(R.id.btnPicture);

        edtName = rootView.findViewById(R.id.edtName);
        edtSurname = rootView.findViewById(R.id.edtSurname);
        edtEmail = rootView.findViewById(R.id.edtEmail);

        btnSignUp = rootView.findViewById(R.id.btnSignUp);

        chbTerms = rootView.findViewById(R.id.chbTerms);

        txtTerms = rootView.findViewById(R.id.txtTerms);

        handleEvents();

        imageUtils = ImageUtils.getInstance();
        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.uploading_data)).build();

        //Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        return rootView;
    }

    private void handleEvents() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpClicked();
            }
        });

        bntGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        txtTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsOfUse();
            }
        });

        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
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

    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)),
                REQUEST_SELECT_PICTURE);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    photoURI = FileProvider.getUriForFile(context,
                            "com.projects.zonetwyn.carla.fileprovider",
                            photoFile);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void galleryAddPicture() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void setPicture() {

        int targetW = imgPicture.getWidth();
        int targetH = imgPicture.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imgPicture.setImageBitmap(bitmap);
    }

    private void onSignUpClicked() {
        String name = (edtName.getText() != null) ? edtName.getText().toString() : "";
        String surname = (edtSurname.getText() != null) ? edtSurname.getText().toString() : "";
        String email = (edtEmail.getText() != null) ? edtEmail.getText().toString() : "";

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty()) {
            showToast(getString(R.string.empty_field));
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (pictureUrl != null) {
                    if (chbTerms.isChecked()) {
                        Client client = new Client();
                        client.setName(name);
                        client.setSurname(surname);
                        client.setEmail(email);
                        client.setPictureUrl(pictureUrl);
                        Event event = new Event(Event.SUBJECT_SIGN_UP_PROCESS, client);
                        EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
                    } else {
                        showToast(getString(R.string.you_must_accept_terms));
                    }
                } else {
                    showToast(getString(R.string.picture_required));
                }
            } else {
                showToast(getString(R.string.invalid_email));
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressPictureTask extends AsyncTask<String, Void, ImageUtils.Data> {

        @Override
        protected ImageUtils.Data doInBackground(String... strings) {
            if (strings != null && strings.length > 0) {
                return imageUtils.getCompressedBitmap(strings[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ImageUtils.Data data) {
            super.onPostExecute(data);

            if (data != null) {
                selectedBitmap = data.getBitmap();
                uploadPicture(data.getData());
            }
        }
    }

    private void uploadPicture(byte[] data) {
        phone = phone.replace("+", "");
        String fileName = "profiles/" + phone + new Timestamp(System.currentTimeMillis()).getTime();
        final StorageReference pathReference = storageReference.child(fileName);

        final UploadTask uploadTask = pathReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pathReference.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                waitingDialog.dismiss();
                                pictureUrl = uri.toString();

                                updateUI();
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
        });
    }

    private void updateUI() {
        if (pictureUrl != null) {
            Picasso.get().load(pictureUrl).into(imgPicture);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            return;
        }

        switch (requestCode) {
            case REQUEST_SELECT_PICTURE:
                try {
                    Uri uri = data.getData();
                    this.selectedUri = uri;
                    String path = imageUtils.getPathFromURI(context, uri);
                    waitingDialog.show();
                    new CompressPictureTask().execute(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                //galleryAddPicture();
                //setPicture();
                waitingDialog.show();
                new CompressPictureTask().execute(currentPhotoPath);
                /*Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgPicture.setImageBitmap(imageBitmap);*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
