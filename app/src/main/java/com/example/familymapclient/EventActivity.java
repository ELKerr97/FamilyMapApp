package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import model.Event;

public class EventActivity extends AppCompatActivity {

    public static final String EVENT_KEY = "ReceivedEventKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Iconify.with( new FontAwesomeModule());

        DataCache dataCache = DataCache.getInstance();
        Intent intent = getIntent();
        Event event = dataCache.getEvent(intent.getStringExtra(EVENT_KEY));

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        MapFragment newMapFragment = new MapFragment();

        newMapFragment.setObservedEvent(event);

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragmentFrameLayout_Event, newMapFragment)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.up_only_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

}