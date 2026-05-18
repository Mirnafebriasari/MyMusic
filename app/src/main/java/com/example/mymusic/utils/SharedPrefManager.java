package com.example.mymusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "MyMusicPrefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences prefs;

    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Default FALSE = light mode saat pertama kali buka app
    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean isDark) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply();
    }
}