package com.adirmor.newlogin.loginAndRegister;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adirmor.newlogin.Notification.AlertReceiver;
import com.adirmor.newlogin.inApp.FragmentDisplayActivity;
import com.adirmor.newlogin.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputEditText Email, Password;
    private String email, password;
    private ImageView loginImage;
    private TextView Go;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);

        variables ();
    }
    private void variables(){
        Email = findViewById (R.id.EmailAddress);
        Password = findViewById (R.id.Password);
        progressBar = findViewById (R.id.ProgressBarLogin);
        loginImage = findViewById (R.id.loginImage);
        Go = findViewById (R.id.Go);
        mAuth = FirebaseAuth.getInstance ();

        loginImage.setAnimation (AnimationUtils.loadAnimation (this, R.anim.top_animation));
    }
    public boolean validEmail(){
        email = String.valueOf (Email.getText ());

        if(TextUtils.isEmpty (email)){
            Email.setError ("cannot be empty");
            return false;
        }else {
            Email.setError (null);
            return true;
        }
    }
    public boolean validPassword(){
        password = String.valueOf (Password.getText ());

        if(TextUtils.isEmpty (password)){
            Password.setError ("cannot be empty");
            return false;
        }else {
            Password.setError (null);
            return true;
        }
    }
    public void login(View view){
        progressBar.setVisibility (View.VISIBLE);
        Go.setVisibility (View.GONE);

        if(!validEmail () || !validPassword ())
            return;

        checkUser();
        progressBar.setVisibility (View.GONE);
        Go.setVisibility (View.VISIBLE);

    }
    public void checkUser(){
        email = String.valueOf (Email.getText ());
        password = String.valueOf (Password.getText ());

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        CheckBox rememberMeButton = findViewById (R.id.rememberMeButton);
                        setSharedPreferences (rememberMeButton.isChecked ());
                        startActivity (new Intent (Login.this, FragmentDisplayActivity.class));
                        finish ();
                    } else {
                        Toast.makeText (Login.this,"One or more of the details are incorrect", Toast.LENGTH_SHORT).show ();
                    }
                });
    }
    public void GoTORegisterPage(View view) {
        Intent intent = new Intent (getApplicationContext (), Register.class);
        startActivity(intent);
        finish();
    }
    public void ForgotPassword(View view) {
        EditText emailToResetPassword = new EditText (view.getContext ());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder (view.getContext ());
        passwordResetDialog.setTitle ("Reset Password?");
        passwordResetDialog.setMessage ("Enter Your Email To Received Reset Link.");
        passwordResetDialog.setView (emailToResetPassword);

        passwordResetDialog.setPositiveButton ("Yes", (dialogInterface, i) -> {
            String mail = emailToResetPassword.getText ().toString ();
            mAuth.sendPasswordResetEmail (mail).addOnSuccessListener (unused -> Toast.makeText (getApplicationContext (), "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show ()).addOnFailureListener (e -> Toast.makeText (getApplicationContext (), "Error! Reset Link is Not Sent." + e.getMessage (), Toast.LENGTH_SHORT).show ());
        });
        passwordResetDialog.show ();
    }
    public void setSharedPreferences(boolean b) {
        SharedPreferences sharedPreferences = getSharedPreferences ("isAlreadySignIn", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit ();
        editor.putBoolean ("remember", b);
        editor.apply ();
    }

    @Override
    protected void onStart() {
        super.onStart ();
        SharedPreferences preferences = getSharedPreferences ("isAlreadySignIn", MODE_PRIVATE);
        boolean check = preferences.getBoolean ("remember", false);

        if(check){
            startActivity (new Intent (getApplicationContext (), FragmentDisplayActivity.class));
            finish ();
        }
    }
}