package com.wordpress.necessitateapps.picme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Getters.RequestGetter;
import com.wordpress.necessitateapps.picme.LoginActivity;
import com.wordpress.necessitateapps.picme.R;
import com.wordpress.necessitateapps.picme.RequestActivity;
import com.wordpress.necessitateapps.picme.SingleTradeAction;


public class HomeFragment extends Fragment {
    private ImageView imageProfile;
    private TextView textName;
    private String userUID;
    private RecyclerView mRecycle;
    private DatabaseReference databaseRequests;
    private static String userUID2;
    private final static int PERMISSION_ACCESS_EXTERNAL = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        //recycle set reverse
        mRecycle= view.findViewById(R.id.mRecycle);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecycle.setLayoutManager(mLayoutManager);

        //view initializations
        imageProfile=view.findViewById(R.id.image_profile);
        textName=view.findViewById(R.id.text_name);
        FloatingActionButton fabRequest=view.findViewById(R.id.fab_request);

        //databases
        DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);
        databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequests.keepSynced(true);

        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        getInfo(databaseUsers);

        fabRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getActivity(), RequestActivity.class);
                startActivity(i);
            }
        });
        RippleBackground ripple=view.findViewById(R.id.ripple);
        ripple.startRippleAnimation();

        getPermission();

        return view;
    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_ACCESS_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //no to permission
                    Snackbar.make(getActivity().findViewById(R.id.layout),"Permission Required For This App", Snackbar.LENGTH_SHORT).show();
                }

                break;
        }
    }
    private void getInfo(DatabaseReference databaseUsers){


       databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String profilepic=(String)dataSnapshot.child("profilepic").getValue();


                if(name!=null)
                    textName.setText(name);

                if(profilepic!=null&&!profilepic.isEmpty())
                 Picasso.with(getActivity()).load(profilepic).fit().centerCrop().into(imageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();



        Query requestQuery=databaseRequests.child(userUID).orderByChild("type").equalTo("request");

        FirebaseRecyclerAdapter<RequestGetter, HomeFragment.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<RequestGetter, HomeFragment.ImageViewHolder>(
                RequestGetter.class,
                R.layout.list_requests,
                HomeFragment.ImageViewHolder.class,
                requestQuery

        ) {
            @Override
            protected void populateViewHolder(HomeFragment.ImageViewHolder viewHolder, RequestGetter model, int position) {
                viewHolder.setFrom(getActivity(), model.getFrom());
                viewHolder.setMsg(model.getMsg());

                final String receivedKey=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(getActivity(), SingleTradeAction.class);
                        i.putExtra("key",receivedKey);
                        i.putExtra("userUID2",userUID2);
                        i.putExtra("type","send");
                        startActivity(i);
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


        public void setFrom(final Context ctx, final String from) {
            userUID2=from;
            DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseUsers.keepSynced(true);
            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String profilepic = (String) dataSnapshot.child(from).child("profilepic").getValue();
                    String name = (String) dataSnapshot.child(from).child("name").getValue();

                    TextView textRequestName=mView.findViewById(R.id.text_request_name);
                    textRequestName.setText(name+" sent a photo request");
                    ImageView imageRequest = mView.findViewById(R.id.image_request);

                    if(profilepic!=null&&!profilepic.isEmpty())
                        Picasso.with(ctx).load(profilepic).fit().centerCrop().into(imageRequest);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setMsg(final String msg) {
            TextView textRequest=mView.findViewById(R.id.text_request_msg);
            textRequest.setText('"'+msg+'"');
        }
    }

}