package com.wordpress.necessitateapps.picme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Getters.FriendsGetter;
import com.wordpress.necessitateapps.picme.ProfileActivity;
import com.wordpress.necessitateapps.picme.R;

/**
 * Created by spotzdevelopment on 2/2/2018.
 */

public class FriendsFragment extends Fragment {

    private RecyclerView mRecycle;
    private String userUID;
    private EditText editSearch;
    private DatabaseReference databaseFriends,databaseNames;
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

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecycle.getContext(),
                mLayoutManager.getOrientation());
        mRecycle.addItemDecoration(dividerItemDecoration);

        mRecycle.setLayoutManager(mLayoutManager);

        databaseFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseFriends.keepSynced(true);
        databaseNames = FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);
        editSearch=view.findViewById(R.id.edit_search);

        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchFriend();
                return true;
            }
        });
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        return view;
    }

    private void searchFriend(){
        String search;
        search=editSearch.getText().toString().trim().toLowerCase();
        if(!search.isEmpty()){

            final String finalSearch = search;
            databaseNames.child(search).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String userUID2=(String)dataSnapshot.child("userUID").getValue();
                        Intent i=new Intent(getActivity(),ProfileActivity.class);
                        i.putExtra("userUID2",userUID2);
                        i.putExtra("name2",finalSearch);
                        startActivity(i);
                    }else{
                        Toast.makeText(getActivity(),"User Not Found", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            Toast.makeText(getActivity(),"User Not Found", Toast.LENGTH_LONG).show();
        }

    }

    private void addFriend(String userUID2, String name){

        Toast.makeText(getActivity(),"Friend Added!", Toast.LENGTH_LONG).show();
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

                final String userUID2=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(getActivity(), ProfileActivity.class);
                        i.putExtra("userUID2", userUID2);
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
