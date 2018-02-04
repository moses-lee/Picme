package com.wordpress.necessitateapps.picme;

import android.*;
import android.app.Activity;
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Fragments.FriendsFragment;
import com.wordpress.necessitateapps.picme.Getters.FriendsGetter;
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
    private RecyclerView mRecycle;
    String userUID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        databaseFriends= FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseFriends.keepSynced(true);
        databaseNames= FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);
        databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequests.keepSynced(true);

        editFriends=findViewById(R.id.edit_friends);

        editFriends.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                return true;
            }
        });
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

        //recycle set reverse
        mRecycle= findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecycle.getContext(),
                mLayoutManager.getOrientation());
        mRecycle.addItemDecoration(dividerItemDecoration);

        mRecycle.setLayoutManager(mLayoutManager);

        ImageView imageExit=findViewById(R.id.image_exit);
        imageExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getFriends();
        loadList();
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
                final String[] arr = trimmedname.split(",");
                DatabaseReference push=databaseRequests.push();
                final String newRequestKey=push.getKey();

                int j;
                for(j=0;j<arr.length;j++){
                    String userUID2=(String)dataSnapshot.child(arr[j]).child("userUID").getValue();

                    databaseRequests.child(userUID2).child(newRequestKey).child("from").setValue(userUID);
                    databaseRequests.child(userUID2).child(newRequestKey).child("id").setValue(newRequestKey);
                    databaseRequests.child(userUID2).child(newRequestKey).child("type").setValue("request");
                    final int finalJ = j;
                    databaseRequests.child(userUID2).child(newRequestKey).child("msg").setValue(msg).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(finalJ ==arr.length-1){
                                doneAction("Request");
                            }
                        }
                    });

                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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



    String friendList=null;
    private void uploadtoDatabase(final ArrayList<String> imagesRef){
        final DatabaseReference databaseImages= FirebaseDatabase.getInstance().getReference().child("Images");
        databaseImages.keepSynced(true);


        final DatabaseReference requestID=databaseRequests.push();
        final String sendIDKey=requestID.getKey();
        databaseNames.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendList= editFriends.getText().toString().substring(0, editFriends.getText().toString().length() - 1);
                final String[] arr = friendList.split(",");

                for(int i=0;i<imagesRef.size();i++){
                    DatabaseReference imageID=databaseImages.push();
                    imageID.child("key").setValue(sendIDKey);
                    imageID.child("image").setValue(imagesRef.get(i));
                }

                for(int j=0;j<arr.length;j++){
                    String userUID2=(String)dataSnapshot.child(arr[j]).child("userUID").getValue();

                    databaseRequests.child(userUID2).child(sendIDKey).child("from").setValue(userUID);
                    databaseRequests.child(userUID2).child(sendIDKey).child("id").setValue(sendIDKey);
                    databaseRequests.child(userUID2).child(sendIDKey).child("replied").setValue(false);
                    final int finalJ = j;
                    databaseRequests.child(userUID2).child(sendIDKey).child("type").setValue("send").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(finalJ ==arr.length-1){
                                doneAction("Images");
                            }
                        }
                    });




                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void doneAction(String msg) {
        Toast.makeText(this, msg+" Sent!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void loadList(){
        DatabaseReference databaseFriends= FirebaseDatabase.getInstance().getReference().child("Friends").child(userUID);
        databaseFriends.keepSynced(true);


        FirebaseRecyclerAdapter<FriendsGetter, RequestActivity.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<FriendsGetter, RequestActivity.ImageViewHolder>(
                FriendsGetter.class,
                R.layout.friends_list,
                RequestActivity.ImageViewHolder.class,
                databaseFriends

        ) {
            @Override
            protected void populateViewHolder(RequestActivity.ImageViewHolder viewHolder, FriendsGetter model, int position) {
                viewHolder.setUserUID(RequestActivity.this, model.getUserUID());


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }
        };
        mRecycle.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        View mView;
        FirebaseAuth mAuth;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();

        }


        public void setUserUID(final Context ctx, final String userUID) {
            DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseUsers.keepSynced(true);
            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String profilepic = (String) dataSnapshot.child(userUID).child("profilepic").getValue();
                    String name = (String) dataSnapshot.child(userUID).child("name").getValue();

                    TextView nName = mView.findViewById(R.id.text_friend);
                    nName.setText(name);
                    ImageView post_image = mView.findViewById(R.id.image_friend);
                    if(profilepic!=null&&!profilepic.isEmpty())
                        Picasso.with(ctx).load(profilepic).fit().centerCrop().into(post_image);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

}
