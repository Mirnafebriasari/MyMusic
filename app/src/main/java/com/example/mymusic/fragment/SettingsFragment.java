package com.example.mymusic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.mymusic.databinding.FragmentSettingsBinding;
import com.example.mymusic.utils.SharedPrefManager;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;
    SharedPrefManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(
                inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new SharedPrefManager(requireContext());

        // Set state switch sesuai preferensi tersimpan
        binding.switchTheme.setChecked(prefManager.isDarkMode());

        // Listener switch dark mode
        binding.switchTheme.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefManager.setDarkMode(isChecked);

                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}