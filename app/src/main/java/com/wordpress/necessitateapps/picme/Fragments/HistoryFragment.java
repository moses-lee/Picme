package com.wordpress.necessitateapps.picme.Fragments;

/**
 * Created by spotzdevelopment on 2/3/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.wordpress.necessitateapps.picme.Getters.HistoryGetter;
import com.wordpress.necessitateapps.picme.R;

public class HistoryFragment extends Fragment {

    private RecyclerView mRecycle;
    private String userUID;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

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


        FirebaseRecyclerAdapter<HistoryGetter, HistoryFragment.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<HistoryGetter, HistoryFragment.ImageViewHolder>(
                HistoryGetter.class,
                R.layout.request_list,
                HistoryFragment.ImageViewHolder.class,
                databaseFriends

        ) {
            @Override
            protected void populateViewHolder(HistoryFragment.ImageViewHolder viewHolder, HistoryGetter model, int position) {

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



    }




}