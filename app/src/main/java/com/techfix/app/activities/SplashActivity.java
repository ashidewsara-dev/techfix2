package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(
                    SplashActivity.this,
                    LoginActivitymain.class
            );

            startActivity(intent);

            finish();

        }, SPLASH_TIME);
    }
}