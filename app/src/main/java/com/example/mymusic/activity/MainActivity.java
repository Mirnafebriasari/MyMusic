package com.example.mymusic.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.mymusic.R;
import com.example.mymusic.databinding.ActivityMainBinding;
import com.example.mymusic.utils.SharedPrefManager;
import com.example.mymusic.utils.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply theme sebelum super.onCreate()
        SharedPrefManager prefManager = new SharedPrefManager(this);
        ThemeHelper.applyTheme(prefManager.isDarkMode());

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ambil NavController dari NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // Hubungkan BottomNavigationView dengan NavController
        // NavigationUI otomatis handle perpindahan fragment
        NavigationUI.setupWithNavController(
                binding.bottomNavigation,
                navController
        );
    }
}