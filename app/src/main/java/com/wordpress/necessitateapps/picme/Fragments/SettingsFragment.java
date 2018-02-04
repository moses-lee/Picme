package com.wordpress.necessitateapps.picme.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wordpress.necessitateapps.picme.LoginActivity;
import com.wordpress.necessitateapps.picme.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by spotzdevelopment on 2/2/2018.
 */

public class SettingsFragment extends Fragment {

    private static final int GALLERY_REQUEST=5;
    private Uri eImageUri=null;
    private CircleImageView imageProfile;
    private DatabaseReference databaseUsers;
    private String userUID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
       userUID=mAuth.getCurrentUser().getUid();

        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        TextView textLogOut=view.findViewById(R.id.text_logout);
        TextView textPic=view.findViewById(R.id.text_pic);


        //getting the version name
        TextView textVersion=view.findViewById(R.id.text_version);
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            textVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


       /* Switch switchAuto=view.findViewById(R.id.switch_auto);



        SharedPreferences sharedPref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor  editor = sharedPref.edit();
        Boolean onAuto=sharedPref.getBoolean("auto", true);
        switchAuto.setChecked(onAuto);

        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean click) {
                if(click){
                    editor.putBoolean("auto", true);
                    editor.apply();
                }else{
                    editor.putBoolean("auto", false);
                    editor.apply();
                }
            }
        });*/
        textLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut(mAuth);

            }
        });

        textPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissions();
            }
        });

        imageProfile=view.findViewById(R.id.image_profile);

        getUser();
        return view;
    }

    private void logOut(final FirebaseAuth mAuth){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setMessage("Sign Out?");
        mBuilder.setCancelable(true);

        mBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mAuth.signOut();
                        Intent i=new Intent(getActivity(), LoginActivity.class);
                        i.putExtra("signedout", true);
                        startActivity(i);
                        dialog.cancel();
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

    //asks permission for reading storage
    private void permissions(){
        Intent gallery_intent=new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent,GALLERY_REQUEST);


        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        GALLERY_REQUEST);

            }


        }
    }

    //gets user's current profile pic
    private void getUser(){
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        databaseUsers.child(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();
                if(profilepic!=null&&!profilepic.isEmpty())
                    Picasso.with(getActivity()).load(profilepic).fit().centerCrop().into(imageProfile);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    //saves the image to storage & database
    private void saveEdit() {

        if(eImageUri!=null){
            StorageReference mStorage= FirebaseStorage.getInstance().getReference().child("profilepics/");
            StorageReference filepath= mStorage.child(userUID);
            filepath.putFile(eImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                @SuppressWarnings("VisibleForTests")public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri=taskSnapshot.getDownloadUrl().toString();
                    databaseUsers.child(userUID).child("profilepic").setValue(downloadUri);

                }
            });


        }
    }

    //activates cropper after permission is granted
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_REQUEST && resultCode== Activity.RESULT_OK&& data != null){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setMinCropResultSize(200,200)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(getContext(), this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK) {
                eImageUri = result.getUri();

                imageProfile.setImageURI(eImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("CropError", String.valueOf(error));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, GALLERY_REQUEST);


            }
            else {
                Toast.makeText(getActivity(), "Needs Access to Gallery to Update Image", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveEdit();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveEdit();
    }
}