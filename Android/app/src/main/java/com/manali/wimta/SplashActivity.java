package com.manali.wimta;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.droidsonroids.gif.GifTextView;

public class SplashActivity extends AppCompatActivity {
    //variables
    private RelativeLayout rlayout;
    private GifTextView gif;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //set values to variables
        rlayout = (RelativeLayout) findViewById(R.id.rlay);
        gif = (GifTextView) findViewById(R.id.gif);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent sharedIntent = new Intent(SplashActivity.this, ActivityWelcome.class);

                //Create Transition between activities
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(gif, "imageTransition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, pairs);

                //If user is already logged in then send them to MainActivty
                if(firebaseUser != null){
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                //Otherwise go to Welcome Activity
                }else {
                    startActivity(sharedIntent, options.toBundle());
                    finish();
                }

            }
        }, secondsDelayed * 2250);
    }
}
