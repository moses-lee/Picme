package com.wordpress.necessitateapps.picme;

import android.content.Context;
import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private boolean signedOut=false;
    private LinearLayout layoutLogin,layoutRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseUsers,databaseNames;
    private EditText editPassLogin, editNameLogin, editEmailRegister,editNameRegister,editPassRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try{
            signedOut=getIntent().getExtras().getBoolean("signedout");
        }catch (NullPointerException e){

        }


        TextView buttonLogin=findViewById(R.id.button_login);
        TextView buttonGoToRegister=findViewById(R.id.button_gotoregister);
        editPassLogin=findViewById(R.id.edit_pass_login);
        editNameLogin=findViewById(R.id.edit_name_login);
        editEmailRegister=findViewById(R.id.edit_email_register);
        editNameRegister=findViewById(R.id.edit_name_register);
        editPassRegister=findViewById(R.id.edit_pass_register);
        TextView buttonRegister = findViewById(R.id.button_register);
        ImageView imageClear = findViewById(R.id.image_clear);
        layoutLogin=findViewById(R.id.layout_login);
        layoutRegister=findViewById(R.id.layout_register);


        databaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        databaseNames= FirebaseDatabase.getInstance().getReference().child("Names");
        databaseNames.keepSynced(true);

        //login action
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
                //dismisses keyboard
                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });



        //edittext -> button
        editPassLogin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                logIn();
                return false;
            }
        });

        //register
        buttonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLogin.setVisibility(View.GONE);
                layoutRegister.setVisibility(View.VISIBLE);
            }
        });


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerCheck();
            }
        });

        imageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLogin.setVisibility(View.VISIBLE);
                layoutRegister.setVisibility(View.GONE);
            }
        });




    }

    private void logIn(){
        String name = editNameLogin.getText().toString().trim().toLowerCase();
        String password = editPassLogin.getText().toString().trim();

        //checks if fields are empty
        if (TextUtils.isEmpty(name)&&TextUtils.isEmpty(password)){
            Snackbar.make(layoutLogin, "Both fields are missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Snackbar.make(layoutLogin, "Username field is missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Snackbar.make(layoutLogin, "Password field is missing", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;
        }

        getEmailLogin(name, password);

    }

    private void getEmailLogin(String name, final String password){
        Query emailQuery=databaseNames.orderByKey().equalTo(name);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    String email = (String) dsp.child("email").getValue();
                    auth(email, password);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    //authenticate user
    private void auth(final String email, final String password){
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Logging In")
                .content("Please Wait")
                .progress(true, 0)
                .show();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Snackbar.make(layoutLogin, "Account Not Registered", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        dialog.dismiss();
                    } else {


                        Intent loginintent = new Intent(LoginActivity.this, MainActivity.class);
                        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loginintent);
                        dialog.dismiss();
                    }
                    }
                });

    }

    private void registerCheck(){
        String name = editNameRegister.getText().toString().trim().toLowerCase();
        String email = editEmailRegister.getText().toString().trim();
        String password = editPassRegister.getText().toString().trim();

        //checks for correct format
        if (TextUtils.isEmpty(name)) {
            editNameRegister.requestFocus();
            editNameRegister.setError("Name field is empty!");
            return;
        }
        if (name.length()<4) {
            editNameRegister.requestFocus();
            editNameRegister.setError("Name must be 4 characters minimum!");
            return;
        }
        if (name.length()>15) {
            editNameRegister.requestFocus();
            editNameRegister.setError("Name must be 15 characters max!");
            return;
        }
        if (name.contains(" ")) {
            editNameRegister.requestFocus();
            editNameRegister.setError("No Spaces!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmailRegister.requestFocus();
            editEmailRegister.setError("Email field is empty!");
            return;
        }
        if(!isEmailValid(email)){
            editEmailRegister.requestFocus();
            editEmailRegister.setError("Invalid Email!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassRegister.requestFocus();
            editPassRegister.setError("Password field is empty!");
            return;
        }
        if (password.length() < 8) {
            editPassRegister.requestFocus();
            editPassRegister.setError("Password must be 8 characters minimum!");
            return;
        }
        if (password.contains(" ")) {
            editPassRegister.requestFocus();
            editPassRegister.setError("No Spaces Allowed!");
            return;
        }

        //adds user to auth
        checkName(name, email, password);

    }

    private void checkName(final String name, final String email, final String password){
        databaseNames.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // use "username" already exists
                    // Let the user know he needs to pick another username.
                    Toast.makeText(LoginActivity.this, "Username is already taken!", Toast.LENGTH_SHORT).show();
                } else {
                    // User does not exist. NOW call createUserWithEmailAndPassword
                    createUser(name, email, password);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void createUser(final String name, final String email, final String password){
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Registering")
                .content("Please Wait")
                .progress(true, 0)
                .show();

        //Get Firebase auth instance
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //adds user to Database
                            dialog.dismiss();
                            register(mAuth, name, email);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Registration Failed" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void register(FirebaseAuth mAuth, String name, String email){

        String userUID = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);
        databaseUsers.keepSynced(true);


        //sets name
        databaseNames.child(name).child("email").setValue(email);
        databaseNames.child(name).child("userUID").setValue(userUID);
        //sets values in the database
        databaseUsers.child("name").setValue(name);
        databaseUsers.child("email").setValue(email);
        databaseUsers.child("profilepic").setValue(null);
        databaseUsers.child("userUID").setValue(userUID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent register = new Intent(LoginActivity.this, MainActivity.class);
                register.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(register);
            }
        });
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        if(!signedOut){
            super.onBackPressed();
        }

    }
}
