package com.wordpress.necessitateapps.picme;

import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyfishjy.library.RippleBackground;
import com.squareup.picasso.Picasso;
import com.wordpress.necessitateapps.picme.Fragments.FriendsFragment;
import com.wordpress.necessitateapps.picme.Fragments.HistoryFragment;
import com.wordpress.necessitateapps.picme.Fragments.HomeFragment;
import com.wordpress.necessitateapps.picme.Fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Fragment fragment;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //sets ExploreFragment when started
        fragmentManager.beginTransaction().add(R.id.frame, new HomeFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_home);

        DatabaseReference databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        //getInfo(navigationView, databaseUsers);


    }

 /*   private void getInfo(NavigationView navigationView, DatabaseReference databaseUsers) {
        //grabs user's id
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUID = mAuth.getCurrentUser().getUid();

        View nView =  navigationView.getHeaderView(0);
        final ImageView imageNav=nView.findViewById(R.id.image_nav);

        databaseUsers.child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profilepic = (String) dataSnapshot.child("profilepic").getValue();
                Picasso.with(MainActivity.this).load(profilepic).fit().centerCrop().into(imageNav);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

        @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();


        if (id == R.id.nav_home) {
            fragment= new HomeFragment();
            // Set action bar title
            setTitle(item.getTitle());
        } else if (id == R.id.nav_received) {
            fragment= new HistoryFragment();
            setTitle(item.getTitle());

        } else if (id == R.id.nav_friends) {
            fragment= new FriendsFragment();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_settings) {
            fragment= new SettingsFragment();
            setTitle(item.getTitle());
        }



        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        // Insert the fragment by replacing any existing fragment
        fragmentManager.beginTransaction().replace(R.id.frame, fragment).addToBackStack("fragBack").commit();


        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
