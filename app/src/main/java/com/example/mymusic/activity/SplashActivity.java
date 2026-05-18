package com.example.mymusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mymusic.R;
import com.example.mymusic.utils.SharedPrefManager;
import com.example.mymusic.utils.ThemeHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply theme sebelum setContentView
        SharedPrefManager prefManager = new SharedPrefManager(this);
        ThemeHelper.applyTheme(prefManager.isDarkMode());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 2000);
    }
}