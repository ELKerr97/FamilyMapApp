package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        DataCache dataCache = DataCache.getInstance();

        Switch lifeStorySwitch = findViewById(R.id.lifeStoryOption_switch);
        Switch familyTreeSwitch = findViewById(R.id.familyTreeOption_switch);
        Switch spouseLinesSwitch = findViewById(R.id.spouseLinesOption_switch);
        Switch fatherSideSwitch = findViewById(R.id.fatherSideOption_switch);
        Switch motherSideSwitch = findViewById(R.id.motherSideOption_switch);
        Switch maleEventsSwitch = findViewById(R.id.maleEventOption_switch);
        Switch femaleEventsSwitch = findViewById(R.id.femaleEventOption_switch);
        RelativeLayout logout = findViewById(R.id.logoutOption);

        lifeStorySwitch.setChecked(dataCache.isShowLifeStoryLines());
        spouseLinesSwitch.setChecked(dataCache.isShowFamilyTreeLines());
        spouseLinesSwitch.setChecked(dataCache.isShowSpouseLines());
        fatherSideSwitch.setChecked(dataCache.isShowDadSideEvents());
        motherSideSwitch.setChecked(dataCache.isShowMomSideEvents());
        maleEventsSwitch.setChecked(dataCache.isShowMaleEvents());
        femaleEventsSwitch.setChecked(dataCache.isShowFemaleEvents());

        // Logout option
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataCache.setUserLoggedIn(false);
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        lifeStorySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowLifeStoryLines(isChecked);
            }
        });

        familyTreeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowFamilyTreeLines(isChecked);
            }
        });

        spouseLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowSpouseLines(isChecked);
            }
        });

        fatherSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowDadSideEvents(isChecked);
            }
        });

        motherSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowMomSideEvents(isChecked);
            }
        });

        maleEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowMaleEvents(isChecked);
            }
        });

        femaleEventsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dataCache.setShowFemaleEvents(isChecked);
            }
        });

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