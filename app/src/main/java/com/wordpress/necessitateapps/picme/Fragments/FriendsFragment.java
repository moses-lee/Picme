package com.wordpress.necessitateapps.picme.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Getters.FriendsGetter;
import com.wordpress.necessitateapps.picme.R;

/**
 * Created by spotzdevelopment on 2/2/2018.
 */

public class FriendsFragment extends Fragment {

    private RecyclerView mRecycle;
    private String userUID;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);


        //recycle set reverse
        mRecycle= view.findViewById(R.id.mRecycle);
        mRecycle.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecycle.setLayoutManager(mLayoutManager);

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        DatabaseReference databaseFriends= FirebaseDatabase.getInstance().getReference().child("Friends").child(userUID);
        databaseFriends.keepSynced(true);


        FirebaseRecyclerAdapter<FriendsGetter, FriendsFragment.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<FriendsGetter, ImageViewHolder>(
                FriendsGetter.class,
                R.layout.friends_list,
                FriendsFragment.ImageViewHolder.class,
                databaseFriends

        ) {
            @Override
            protected void populateViewHolder(FriendsFragment.ImageViewHolder viewHolder, FriendsGetter model, int position) {
                viewHolder.setUserUID(getActivity(), model.getUserUID());

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
                    Picasso.with(ctx).load(profilepic).fit().centerCrop().into(post_image);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }





}
