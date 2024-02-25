package com.adirmor.newlogin.loginAndRegister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adirmor.newlogin.Models.UserModel;
import com.adirmor.newlogin.Utils.FirebaseUtils;
import com.adirmor.newlogin.inApp.FragmentDisplayActivity;
import com.adirmor.newlogin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private TextInputEditText Email, Password, Username;
    private ProgressBar progressBar;
    private String email, password, username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_regiser);

        Email = findViewById (R.id.EmailAddress);
        Password = findViewById (R.id.Password);
        progressBar = findViewById (R.id.ProgressBar);
        ImageView registerImage = findViewById (R.id.registerImage);
        Username = findViewById (R.id.Username);

        registerImage.setAnimation (AnimationUtils.loadAnimation (this, R.anim.top_animation));
    }

    public boolean validDataEmail(){
        email = String.valueOf (Email.getText ());

        if(TextUtils.isEmpty (email)){
            Email.setError ("cannot be empty");
            progressBar.setVisibility (View.GONE);
            return false;
        }else{
            Email.setError (null);
            return true;
        }
    }
    public boolean validDataUsername(){
        username = String.valueOf (Username.getText ());

        if(TextUtils.isEmpty (username)){
            Username.setError ("cannot be empty");
            progressBar.setVisibility (View.GONE);
            return false;
        }else if (username.length () < 6){
            Username.setError ("Username too short");
            progressBar.setVisibility (View.GONE);
            return false;
        }else{
            Username.setError (null);
            return true;
        }
    }
    public boolean validDataPassword(){
        password = String.valueOf (Password.getText ());

        if(TextUtils.isEmpty (password)){
            Password.setError ("cannot be empty");
            progressBar.setVisibility (View.GONE);
            return false;
        }else if(password.length () < 4){
            Password.setError ("password to short");
            progressBar.setVisibility (View.GONE);
            return false;
        }else if(password.length () > 12){
            Password.setError ("password to long");
            progressBar.setVisibility (View.GONE);
            return false;
        }else{
            Password.setError (null);
            return true;
        }

    }

    public void CreateUser(View view) {
        progressBar.setVisibility (View.VISIBLE);

        if (!validDataEmail () || !validDataPassword () || !validDataUsername ())
            return;
        FirebaseAuth.getInstance ().createUserWithEmailAndPassword (email, password)
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful ()) {
                        sendVerifyEmailToEmail ();
                        createAccountByFirestore ();

                        startActivity (new Intent (Register.this, FragmentDisplayActivity.class));
                        finish ();

                        Toast.makeText (Register.this, "Account created", Toast.LENGTH_SHORT).show ();
                    } else {
                        Toast.makeText (Register.this, "Failed to create the account", Toast.LENGTH_SHORT).show ();
                    }
                });
        progressBar.setVisibility (View.GONE);
    }
    public void sendVerifyEmailToEmail(){
        FirebaseAuth.getInstance ().getCurrentUser ().sendEmailVerification ().addOnSuccessListener (new OnSuccessListener<Void> () {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText (getApplicationContext (), "Verification Email Has Been Sent.", Toast.LENGTH_SHORT).show ();
            }
        }).addOnFailureListener (new OnFailureListener () {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText (getApplicationContext (), "Email not sent " + e.getMessage (), Toast.LENGTH_SHORT).show ();
            }
        });
    }
    public void createAccountByFirestore(){
        email = String.valueOf (Email.getText ());
        password = String.valueOf (Password.getText ());
        username = String.valueOf (Username.getText ());

        UserModel userModel = new UserModel (email, password, username);


        FirebaseUtils.getUserModel ().set(userModel).addOnSuccessListener (unused -> Toast.makeText (Register.this, "Account created", Toast.LENGTH_SHORT).show ()).addOnFailureListener (e -> Toast.makeText (Register.this, "Failed to create the account", Toast.LENGTH_SHORT).show ());
    }
    public void GoToLoginPage(View view) {
        Intent intent = new Intent (getApplicationContext (), Login.class);
        startActivity(intent);
        finish();
    }
}