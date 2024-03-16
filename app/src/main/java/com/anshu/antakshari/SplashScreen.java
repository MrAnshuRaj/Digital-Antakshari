package com.anshu.antakshari;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences getShared = getSharedPreferences("digANT", MODE_PRIVATE);
                        boolean loggedIn = getShared.getBoolean("Logged", false) || getShared.getBoolean("GoogleSignIn", false);
                        Intent i;
                        if (loggedIn) {
                            i = new Intent(SplashScreen.this, GameMode.class);
                        } else {
                            i = new Intent(SplashScreen.this, MainActivity2.class);
                        }
                        startActivity(i);
                        finish();
                    }
                }, 1700);

    }
}