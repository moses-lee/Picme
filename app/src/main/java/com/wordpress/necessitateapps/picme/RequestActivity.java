package com.wordpress.necessitateapps.picme;

import android.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.Manifest;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

public class RequestActivity extends AppCompatActivity {

    private DatabaseReference databaseFriends,databaseNames,databaseRequests;
    private MultiAutoCompleteTextView editFriends;
    private final static int PERMISSION_ACCESS_EXTERNAL = 10;
    private final static int GALLERY_CODE = 0;
    String userUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        databaseFriends= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseFriends.keepSynced(true);
        databaseNames= FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);
        databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequests.keepSynced(true);

        editFriends=findViewById(R.id.edit_friends);
        FancyButton buttonSend=findViewById(R.id.button_send);
        FancyButton buttonRequest=findViewById(R.id.button_request);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editFriends.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.layout),"Select Friends First!", Snackbar.LENGTH_LONG).show();
                }else{
                    checkPermissions();
                }



            }
        });

        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editFriends.getText().toString().isEmpty()){
                    Snackbar.make(findViewById(R.id.layout),"Select Friends First!", Snackbar.LENGTH_LONG).show();
                }else{
                    requestImage();
                }


            }
        });

        ImageView imageExit=findViewById(R.id.image_exit);
        imageExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getFriends();

    }

    private String[] friends=null;
    public void getFriends() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUID=mAuth.getCurrentUser().getUid();
        databaseFriends= FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseFriends.keepSynced(true);

        databaseFriends.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int number=(int)dataSnapshot.getChildrenCount();

                if(number!=0){
                    friends= new String[number];
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                        String name = (String) dsp.child("name").getValue();
                        friends[number-1]=name;
                    }
                }

                getAutoComplete(friends);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void getAutoComplete(String[] friends){


        if(friends!=null){
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friends);
            editFriends.setAdapter(adapter);
            editFriends.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        }




    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        Matisse.from(RequestActivity.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(10)
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
        databaseNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String trimmedname= editFriends.getText().toString().substring(0, editFriends.getText().toString().length() - 1);
                String[] arr = trimmedname.split(",");
                Log.v("LOGGG:", String.valueOf(arr));
                DatabaseReference push=databaseRequests.push();
                final String newRequestKey=push.getKey();

                for(int j=0;j<arr.length;j++){
                    String userUID2=(String)dataSnapshot.child(arr[j]).child("userUID").getValue();
                    databaseRequests.child(userUID2).child(newRequestKey).child("type").setValue("request");
                    databaseRequests.child(userUID2).child(newRequestKey).child("from").setValue(userUID);
                    databaseRequests.child(userUID2).child(newRequestKey).child("msg").setValue(msg);
                    databaseRequests.child(userUID2).child(newRequestKey).child("replied").setValue(false);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    ArrayList<String> references = new ArrayList<>();
    private void convert(final List<Uri> mSelected) throws FileNotFoundException {
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

                        while(downloadUri==null){
                            downloadUri=taskSnapshot.getDownloadUrl().toString();

                        }

                        references.add(downloadUri);
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(finalI ==mSelected.size()-1&&references!=null)
                     uploadtoDatabase(references);
                }
            });

        }

    }



    String friendList=null;
    private void uploadtoDatabase(final ArrayList<String> imagesRef){

        Log.v("LOGG", String.valueOf(imagesRef));
        final DatabaseReference requestID=databaseRequests.push();
        final String sendIDKey=requestID.getKey();
        databaseNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                    friendList= editFriends.getText().toString().substring(0, editFriends.getText().toString().length() - 1);
                    String[] arr = friendList.split(",");

                    for(int j=0;j<arr.length;j++){
                        String userUID2=(String)dataSnapshot.child(arr[j]).child("userUID").getValue();
                        for(int i=0;i<imagesRef.size();i++) {
                            databaseRequests.child(userUID2).child(sendIDKey).child("images").child(String.valueOf(i)).setValue(imagesRef.get(i));

                        }


                        databaseRequests.child(userUID2).child(sendIDKey).child("type").setValue("send");
                        databaseRequests.child(userUID2).child(sendIDKey).child("from").setValue(userUID);
                        //databaseRequests.child(userUID2).child(sendIDKey).child("msg").setValue(msg);
                        databaseRequests.child(userUID2).child(sendIDKey).child("replied").setValue(false);

                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
