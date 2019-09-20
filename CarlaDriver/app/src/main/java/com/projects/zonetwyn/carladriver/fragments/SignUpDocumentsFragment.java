package com.projects.zonetwyn.carladriver.fragments;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projects.zonetwyn.carladriver.R;
import com.projects.zonetwyn.carladriver.models.Document;
import com.projects.zonetwyn.carladriver.utils.Event;
import com.projects.zonetwyn.carladriver.utils.EventBus;
import com.projects.zonetwyn.carladriver.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpDocumentsFragment extends Fragment {

    private Context context;

    private RecyclerView rcvDocuments;

    private CheckBox chbIdCard;
    private CheckBox chbLicence;
    private CheckBox chbVtcCard;
    private CheckBox chbVtcInsurance;
    private CheckBox chbGrayCard;
    private CheckBox chbRecord;
    private CheckBox chbKbis;

    private LinearLayout lnlUpload;
    private TextView txtTitle;

    private FrameLayout frlFile;
    private ImageView imgFile;

    private FloatingActionButton btnPicture;
    private FloatingActionButton btnGallery;
    private FloatingActionButton btnFile;

    private Button btnSave;
    private Button btnNext;

    private List<String> titles;
    private int currentCheckBox;

    private List<CheckBox> checkBoxes;

    private List<Document> documents;

    private ImageUtils imageUtils;

    public final int REQUEST_SELECT_PICTURE = 0x01;
    public final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public final int REQUEST_SELECT_FILE = 0x3;

    private String currentPhotoPath;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private android.app.AlertDialog waitingDialog;

    public static String ARG_PHONE = "Phone";
    private String phone;

    public SignUpDocumentsFragment() {

    }

    public static SignUpDocumentsFragment newInstance(String phone) {
        SignUpDocumentsFragment fragment = new SignUpDocumentsFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sign_up_documents, container, false);

        context = getContext();

        rcvDocuments = rootView.findViewById(R.id.rcvDocuments);

        chbIdCard = rootView.findViewById(R.id.chbIdCard);
        chbLicence = rootView.findViewById(R.id.chbLicence);
        chbVtcCard = rootView.findViewById(R.id.chbVtcCard);
        chbVtcInsurance = rootView.findViewById(R.id.chbVtcInsurance);
        chbGrayCard = rootView.findViewById(R.id.chbGrayCard);
        chbRecord = rootView.findViewById(R.id.chbRecord);
        chbKbis = rootView.findViewById(R.id.chbKbis);

        lnlUpload = rootView.findViewById(R.id.lnlUpload);
        txtTitle = rootView.findViewById(R.id.txtTitle);

        frlFile = rootView.findViewById(R.id.frlFile);
        imgFile = rootView.findViewById(R.id.imgFile);

        btnPicture = rootView.findViewById(R.id.btnPicture);
        btnGallery = rootView.findViewById(R.id.btnGallery);
        btnFile = rootView.findViewById(R.id.btnFile);

        btnSave = rootView.findViewById(R.id.btnSave);
        btnNext = rootView.findViewById(R.id.btnNext);

        //Init Components
        currentCheckBox = 0;
        titles = new ArrayList<>();
        titles.add(context.getResources().getString(R.string.id_card));
        titles.add(context.getResources().getString(R.string.license));
        titles.add(context.getResources().getString(R.string.vtc_card));
        titles.add(context.getResources().getString(R.string.vtc_insurance));
        titles.add(context.getResources().getString(R.string.gray_card));
        titles.add(context.getResources().getString(R.string.criminal_record_extract));
        titles.add(context.getResources().getString(R.string.kbis_extract));

        checkBoxes = new ArrayList<>();
        checkBoxes.add(chbIdCard);
        checkBoxes.add(chbLicence);
        checkBoxes.add(chbVtcCard);
        checkBoxes.add(chbVtcInsurance);
        checkBoxes.add(chbGrayCard);
        checkBoxes.add(chbRecord);
        checkBoxes.add(chbKbis);

        applyCurrentCheckBox();

        handleEvents();

        documents = new ArrayList<>();
        imageUtils = ImageUtils.getInstance();

        waitingDialog = new SpotsDialog.Builder().setContext(context).setMessage(getString(R.string.uploading_data)).build();

        //Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init document list
        documents = new ArrayList<>();

        return rootView;
    }

    private void handleEvents() {
        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (documents.size() >= 7) {
                    Event event = new Event(Event.SUBJECT_SIGN_UP_GO_TO_ACCEPTANCE, documents);
                    EventBus.publish(EventBus.SUBJECT_SIGN_UP, event);
                } else {
                    showToast(getString(R.string.some_document_are_missing));
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });
    }

    private void onSaveClicked() {

    }

    private void updateUI() {
        checkBoxes.get(currentCheckBox).setChecked(true);
        checkBoxes.get(currentCheckBox).setTextColor(getResources().getColor(R.color.black));

        if (currentCheckBox != 6) {
            currentCheckBox++;
            applyCurrentCheckBox();
        } else {
            lnlUpload.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void applyCurrentCheckBox() {
        String title = "( " + titles.get(currentCheckBox) + " )";
        txtTitle.setText(title);

        checkBoxes.get(currentCheckBox).setTextColor(getResources().getColor(R.color.colorSecondaryDark));
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)),
                REQUEST_SELECT_PICTURE);
    }

    private void openCamera(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(context.getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Uri photoURI;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                photoURI = FileProvider.getUriForFile(context,
                        "com.projects.zonetwyn.carladriver",
                        photoFile);
            } else {
                photoURI = Uri.fromFile(photoFile);
            }
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(pictureIntent, REQUEST_CODE_TAKE_PICTURE);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_SELECT_FILE);
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
                uploadPicture(data.getData());
            }
        }
    }

    private void uploadPicture(byte[] data) {
        phone = phone.replace("+", "");
        String fileName = "documents/" + phone + new Timestamp(System.currentTimeMillis()).getTime();
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
                                createDocument(uri.toString(), "png/jpeg");
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

    private void uploadDocument(Uri uri) {
        phone = phone.replace("+", "");
        String fileName = "documents/" + phone + new Timestamp(System.currentTimeMillis()).getTime();
        final StorageReference pathReference = storageReference.child(fileName);

        final UploadTask uploadTask = pathReference.putFile(uri);
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
                                createDocument(uri.toString(), "pdf");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                //showToast(e.getMessage());
                            }
                        });
            }
        });
    }

    private void createDocument(String url, String type) {
        Document document = new Document();
        document.setName(titles.get(currentCheckBox));
        document.setType(type);
        document.setUrl(url);

        documents.add(document);
        updateUI();

        //showToast(document.toString());
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
                    String path = imageUtils.getPathFromURI(context, uri);
                    waitingDialog.show();
                    new CompressPictureTask().execute(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                try {
                    File file = new  File(currentPhotoPath);
                    if(file.exists()) {
                        waitingDialog.show();
                        new CompressPictureTask().execute(currentPhotoPath);
                    }
                }catch (Exception e){
                    Log.e("error camera",e.getMessage());
                }
                break;
            case REQUEST_SELECT_FILE:
                if (data != null) {
                    waitingDialog.show();
                    Uri uri = data.getData();
                    uploadDocument(uri);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
