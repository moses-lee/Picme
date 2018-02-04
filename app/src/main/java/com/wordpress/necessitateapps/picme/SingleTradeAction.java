package com.wordpress.necessitateapps.picme;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skyfishjy.library.RippleBackground;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class SingleTradeAction extends AppCompatActivity {
    private final static int PERMISSION_ACCESS_EXTERNAL = 10;
    private final static int GALLERY_CODE = 0;
    private String key=null, type=null, userUID2=null;
    private String userUID;
    private DatabaseReference databaseRequests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_trade_action);

        key=getIntent().getExtras().getString("key");
        type=getIntent().getExtras().getString("type");
        userUID2=getIntent().getExtras().getString("userUID2");

        ImageView imageDelete=findViewById(R.id.image_delete);
        if(!key.isEmpty()){
            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteRequest();


                }
            });
        }else{
            imageDelete.setVisibility(View.GONE);
        }

        if(key.isEmpty()){
            DatabaseReference databaseRequestsTemp= FirebaseDatabase.getInstance().getReference().child("Requests");
            databaseRequestsTemp.keepSynced(true);
            DatabaseReference requestKey=databaseRequestsTemp.push();
            key=requestKey.getKey();

        }
        databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests").child(userUID2).child(key);
        databaseRequests.keepSynced(true);

        FancyButton buttonSend=findViewById(R.id.button_trade_send);
        FancyButton buttonRequest=findViewById(R.id.button_trade_request);

        if(type.equals("send")){
            buttonRequest.setVisibility(View.GONE);
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPermissions();
                }
            });
        }
        if(type.equals("request")){
            buttonSend.setVisibility(View.GONE);
            buttonRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestImage();
                }
            });
        }

        ImageView imageBack=findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        RippleBackground ripple=findViewById(R.id.ripple);
        ripple.startRippleAnimation();
    }

    private void requestImage(){
        new MaterialDialog.Builder(this)
                .title("Request Image")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Enter Message", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        String msg=input.toString();
                        sendRequest(msg);
                    }
                }).show();


    }

    private void sendRequest(final String msg){
        databaseRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseRequests.child("from").setValue(userUID);
                databaseRequests.child("id").setValue(key);
                databaseRequests.child("type").setValue("request");
                databaseRequests.child("msg").setValue(msg).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Request Sent!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void deleteRequest(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage("Delete Request?");
        mBuilder.setCancelable(true);

        mBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        databaseRequests.removeValue();
                        finish();
                    }
                });

        mBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_ACCESS_EXTERNAL);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    //no to permission
                    Snackbar.make(findViewById(R.id.layout),"Permission Denied", Snackbar.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void openGallery(){
        Matisse.from(SingleTradeAction.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(15)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new PicassoEngine())
                .forResult(GALLERY_CODE);
    }

    List<Uri> mSelected;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);

            try {
                convert(mSelected);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    ArrayList<String> references = new ArrayList<>();
    private void convert(final List<Uri> mSelected) throws FileNotFoundException {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Sending...")
                .content("Please Wait")
                .progress(true, 0)
                .show();

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("images/");

        for(int i=0;i<mSelected.size();i++) {
            final int finalI = i;
            Uri imageUri = mSelected.get(i);

            StorageReference filepath= mStorage.child(userUID+String.valueOf(System.currentTimeMillis()));

            UploadTask uploadTask = filepath.putFile(imageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                @SuppressWarnings("VisibleForTests")public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri=null;

                    //while(downloadUri==null){
                    downloadUri=taskSnapshot.getDownloadUrl().toString();

                    //}

                    references.add(downloadUri);
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(finalI ==mSelected.size()-1&&references!=null) {
                        dialog.dismiss();
                        uploadtoDatabase(references);
                    }
                }
            });

        }

    }

    private void uploadtoDatabase(final ArrayList<String> imagesRef){

        final DatabaseReference databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);
        final DatabaseReference imageID=databaseImages.push();


        databaseRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < imagesRef.size(); i++) {
                    imageID.child("key").setValue(key);
                    imageID.child("image").setValue(imagesRef.get(i));
                }

                databaseRequests.child("from").setValue(userUID);
                databaseRequests.child("id").setValue(key);
                databaseRequests.child("replied").setValue(false);
                databaseRequests.child("type").setValue("send").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Images Sent!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
