package com.wordpress.necessitateapps.picme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.LoginActivity;
import com.wordpress.necessitateapps.picme.R;
import com.wordpress.necessitateapps.picme.RequestActivity;


public class HomeFragment extends Fragment {
    private ImageView imageProfile;
    private TextView textName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        //view initializations
        imageProfile=view.findViewById(R.id.image_profile);
        textName=view.findViewById(R.id.text_name);
        FloatingActionButton fabRequest=view.findViewById(R.id.fab_request);

        //databases
        DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);
        DatabaseReference databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequests.keepSynced(true);



        getInfo(databaseUsers);

        fabRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), RequestActivity.class);
                startActivity(i);
            }
        });


        return view;
    }

    private void getInfo(DatabaseReference databaseUsers){
        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUID=mAuth.getCurrentUser().getUid();

       databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();


                if(name!=null)
                    textName.setText(name);

                Picasso.with(getActivity()).load(profilepic).fit().centerCrop().into(imageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }



}