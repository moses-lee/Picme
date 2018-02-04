package com.wordpress.necessitateapps.picme;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import mehdi.sakout.fancybuttons.FancyButton;

public class ProfileActivity extends AppCompatActivity {

    private String userUID2=null;
    private String userUID;
    private FancyButton buttonSend, buttonRequest;
    private ImageView imageProfile, imageAdd,imageBack;
    private TextView textProfile;
    private DatabaseReference databaseFriends;
    private boolean isFriend=false;
    private boolean  processFriend=false;
    private String name=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        userUID2=getIntent().getExtras().getString("userUID2");

        buttonSend=findViewById(R.id.button_send);
        buttonRequest=findViewById(R.id.button_request);
        imageProfile=findViewById(R.id.image_profile);
        imageAdd=findViewById(R.id.image_add);
        textProfile=findViewById(R.id.text_profile);

        databaseFriends= FirebaseDatabase.getInstance().getReference().child("Friends").child(userUID);
        databaseFriends.keepSynced(true);

        imageBack=findViewById(R.id.image_back_profile);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i_send=new Intent(ProfileActivity.this, SingleTradeAction.class);
                i_send.putExtra("key","");
                i_send.putExtra("userUID2",userUID2);
                i_send.putExtra("type","send");
                startActivity(i_send);
            }
        });
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i_send=new Intent(ProfileActivity.this, SingleTradeAction.class);
                i_send.putExtra("key","");
                i_send.putExtra("userUID2",userUID2);
                i_send.putExtra("type","request");
                startActivity(i_send);
            }
        });

        checkUser();
    }

    private void checkUser(){
        if(userUID.equals(userUID2)){
            imageAdd.setVisibility(View.GONE);
            buttonRequest.setVisibility(View.GONE);
            buttonSend.setVisibility(View.GONE);
        }
        DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp();
            }
        });
        databaseUsers.child(userUID2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = (String) dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();

                textProfile.setText(name);
                if(profilepic!=null&&!profilepic.isEmpty())
                    Picasso.with(ProfileActivity.this).load(profilepic).fit().centerCrop().into(imageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateFriendIcon();
    }

    private void popUp(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        if(isFriend){
            mBuilder.setMessage("Unfriend?");

        }else{
            mBuilder.setMessage("Add to Friend?");
        }

        mBuilder.setCancelable(true);

        mBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addUser();
                        dialog.dismiss();
                    }
                });

        mBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = mBuilder.create();
        dialog.show();

    }


    private void addUser(){

        processFriend = true;

        databaseFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (processFriend) {
                    if (dataSnapshot.hasChild(userUID2)) {
                        databaseFriends.child(userUID2).removeValue();
                        processFriend = false;
                        Toast.makeText(ProfileActivity.this, "Unfriended", Toast.LENGTH_LONG).show();
                        return;
                    }
                    databaseFriends.child(userUID2).child("userUID").setValue(userUID2);
                    databaseFriends.child(userUID2).child("name").setValue(name);
                    processFriend = false;
                    Toast.makeText(ProfileActivity.this,"Added to Friends!", Toast.LENGTH_LONG).show();

                    updateFriendIcon();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateFriendIcon(){

        databaseFriends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userUID2)) {
                    imageAdd.setImageResource(R.drawable.ic_done);
                    isFriend=true;
                }else{
                    imageAdd.setImageResource(R.drawable.ic_add);
                    isFriend=false;
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
