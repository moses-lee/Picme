package com.wordpress.necessitateapps.picme.Fragments;

/**
 * Created by spotzdevelopment on 2/3/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Getters.HistoryGetter;
import com.wordpress.necessitateapps.picme.ImageDownloaderActivity;
import com.wordpress.necessitateapps.picme.R;
import com.wordpress.necessitateapps.picme.SingleTradeAction;

public class HistoryFragment extends Fragment {

    private RecyclerView mRecycle;
    private String userUID;
    private DatabaseReference databaseRequests;
    private static String userUID2=null;
    private TextView textWarning;
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

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecycle.getContext(),
                mLayoutManager.getOrientation());
        mRecycle.addItemDecoration(dividerItemDecoration);
        mRecycle.setLayoutManager(mLayoutManager);

        textWarning=view.findViewById(R.id.text_warning);

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        userUID=mAuth.getCurrentUser().getUid();

        databaseRequests= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequests.keepSynced(true);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        Query sentQuery=databaseRequests.child(userUID).orderByChild("type").equalTo("send");

        FirebaseRecyclerAdapter<HistoryGetter, HistoryFragment.ImageViewHolder >
                firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<HistoryGetter, HistoryFragment.ImageViewHolder>(
                HistoryGetter.class,
                R.layout.list_history,
                HistoryFragment.ImageViewHolder.class,
                sentQuery

        ) {
            @Override
            protected void populateViewHolder(HistoryFragment.ImageViewHolder viewHolder, HistoryGetter model, int position) {
                viewHolder.setFrom(getActivity(), model.getFrom());
                viewHolder.setMsg(model.getMsg());
                viewHolder.setReplied( model.isReplied());


                final String receivedKey=getRef(position).getKey();
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(getActivity(), ImageDownloaderActivity.class);
                        i.putExtra("key",receivedKey);
                        databaseRequests.child(userUID2).child(receivedKey).child("replied").setValue(true);
                        startActivity(i);
                    }
                });

            }
        };

        if(firebaseRecyclerAdapter.getItemCount()!=0){
            textWarning.setVisibility(View.GONE);
        }
        mRecycle.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        View mView;
        FirebaseAuth mAuth;
        TextView textMsg,textName;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();
            textMsg=mView.findViewById(R.id.text_msg);
            textName=mView.findViewById(R.id.text_name);

        }
        public void setFrom(final Context ctx, final String from){


            userUID2=from;
            DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            databaseUsers.keepSynced(true);

            databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String profilepic = (String) dataSnapshot.child(from).child("profilepic").getValue();

                    String name= (String) dataSnapshot.child(from).child("name").getValue();

                    textName.setText(name);
                    ImageView imageReceived = mView.findViewById(R.id.image_received);
                    Picasso.with(ctx).load(profilepic).fit().centerCrop().into(imageReceived);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setMsg(String msg){

            if(msg!=null){
                textMsg.setVisibility(View.VISIBLE);
                textMsg.setText('"'+msg+'"');
            }


        }
        public void setReplied(boolean replied){
            if(!replied){
                textName.setTypeface(null, Typeface.BOLD);
                if(textMsg.getVisibility()!=View.GONE)
                    textMsg.setTypeface(null, Typeface.BOLD);
            }else{
                textName.setTypeface(null, Typeface.NORMAL);
                if(textMsg.getVisibility()!=View.GONE)
                    textMsg.setTypeface(null, Typeface.NORMAL);
            }
        }




    }




}