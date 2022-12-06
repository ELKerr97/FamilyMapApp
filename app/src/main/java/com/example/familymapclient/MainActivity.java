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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.Map;

import model.Event;
import request.RegisterRequest;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener,
        OnMapReadyCallback {

    private LoginFragment loginFragment;
    private MapFragment mapFragment;
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

    @Override
    protected void onResume() {
        super.onResume();
        if(dataCache.userLoggedIn()){
            mapFragment.drawMarkers();
            Event event = mapFragment.getObservedEvent();
            if (event != null){
                mapFragment.drawEventLines(event);
                mapFragment.centerOnEvent(event);
            }
        }
    }

    private Fragment createFirstFragment() {
        if(!dataCache.userLoggedIn()){
            loginFragment = new LoginFragment();
            loginFragment.registerListener(this);
            return loginFragment;
        } else {
            mapFragment = new MapFragment();
            return mapFragment;
        }
    }


    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        mapFragment = new MapFragment();

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentFrameLayout, mapFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if(dataCache.userLoggedIn()){
            inflater.inflate(R.menu.main_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search_menu_item:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings_menu_item:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}