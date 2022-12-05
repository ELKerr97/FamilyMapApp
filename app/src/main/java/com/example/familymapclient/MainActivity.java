package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.Map;

import request.RegisterRequest;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener,
        OnMapReadyCallback {

    private LoginFragment loginFragment;
    DataCache dataCache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Iconify.with(new FontAwesomeModule());
        dataCache.setCurrentActivity(dataCache.MAIN_ACTIVITY);
        // Get pointer to fragment manager
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // Get pointer to fragment in FrameLayout
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayout);
        if(fragment == null){
            fragment = createFirstFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.fragmentFrameLayout, fragment)
                    .commit();
        } else {
            if(fragment instanceof LoginFragment){
                ((LoginFragment) fragment).registerListener(this);
            }
        }
    }

    private Fragment createFirstFragment() {
        if(!dataCache.userLoggedIn()){
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.registerListener(this);
            return loginFragment;
        } else {
            return new MapFragment();
        }
    }


    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment mapFragment = new MapFragment();

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentFrameLayout, mapFragment)
                .commit();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}