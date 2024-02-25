package com.adirmor.newlogin.loginAndRegister;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.adirmor.newlogin.R;

public class splashScreen extends AppCompatActivity {

/*    Animation top, bottom;

    ImageView image;

    TextView appName, copyright;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView (R.layout.activity_splash_screen);
/*
        top = AnimationUtils.loadAnimation (this, R.anim.top_animation);
        bottom = AnimationUtils.loadAnimation (this, R.anim.bottom_animation);

        image = findViewById (R.id.image);

        appName = findViewById (R.id.appName);
        copyright = findViewById (R.id.copyright);

        image.setAnimation(top);
        appName.setAnimation (bottom);
        copyright.setAnimation (bottom);*/


        new Handler ().postDelayed (new Runnable () {
            @Override
            public void run() {
                startActivity (new Intent (splashScreen.this, Login.class));
                finish ();
            }
        }, 500);
    }
}