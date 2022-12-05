package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import model.Event;
import model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap map;
    private ArrayList<Polyline> mapLines;
    private DataCache dataCache = DataCache.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        setHasOptionsMenu(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapLines = new ArrayList<>();
        // Set google map
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        TextView mapTextView = getView().findViewById(R.id.mapEventDescription);
        ImageView personIcon = getView().findViewById(R.id.personIcon);
        personIcon.setImageResource(R.drawable.ic_launcher_foreground);
        Drawable maleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male)
                .colorRes(R.color.male_color).sizeDp(40);
        Drawable femaleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female)
                .colorRes(R.color.female_color).sizeDp(40);

        // Default login location (arbitrary)
        LatLng location;
        if(dataCache.getCurrentMapEvent() == null){
            location = new LatLng(50, 50);
        } else {
            Event currentEvent = dataCache.getCurrentMapEvent();
            location = new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude());
            Person person = dataCache.getPerson(currentEvent.getPersonID());

            if(person.getGender().equalsIgnoreCase("f")){
                personIcon.setImageDrawable(femaleIcon);
            } else {
                personIcon.setImageDrawable(maleIcon);
            }

            mapTextView.setText(
                    dataCache.getPersonOfEvent(currentEvent) + "\n" +
                            dataCache.getEventDetails(currentEvent));
            // Draw lines based on filters
            LineBuilder lineBuilder = new LineBuilder(currentEvent, person.getPersonID(), dataCache.getFilteredEvents(person.getPersonID()));

            Event spouseEvent = lineBuilder.getSpouseEvent();

            if(spouseEvent != null && lineBuilder.shouldShowLine(currentEvent, spouseEvent, dataCache.getFilteredEvents(person.getPersonID()))){
                drawLine(currentEvent, spouseEvent, Color.RED, (float) 0.1);
            }

            drawLifeStoryLines(person.getPersonID());
            drawFamilyTreeLines(currentEvent, dataCache.getFilteredEvents(person.getPersonID()));

            mapTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), PersonActivity.class);
                    intent.putExtra(PersonActivity.PERSON_KEY, person.getPersonID());
                    startActivity(intent);
                }
            });
        }

        map.animateCamera(CameraUpdateFactory.newLatLng(location));

        ArrayList<Event> filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());
        // Get all map events and display them
        for(Event mapEvent : filteredEvents){

            String eventType = mapEvent.getEventType().toLowerCase();

            LatLng eventLocation = new LatLng(mapEvent.getLatitude(),
                    mapEvent.getLongitude());

            Marker eventMarker = map.addMarker(
                            new MarkerOptions().
                            position(eventLocation).
                            icon(BitmapDescriptorFactory.defaultMarker(dataCache.getEventColor(eventType))));

            assert eventMarker != null;
            eventMarker.setTag(mapEvent);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    removeLines();
                    // Display the person associated with the event on screen
                    Event markerEvent = (Event) marker.getTag();
                    Person person = dataCache.getPerson(markerEvent.getPersonID());

                    mapTextView.setText(
                            dataCache.getPersonOfEvent(markerEvent) + "\n" +
                            dataCache.getEventDetails(markerEvent));

                    mapTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), PersonActivity.class);
                            intent.putExtra(PersonActivity.PERSON_KEY, person.getPersonID());
                            startActivity(intent);
                        }
                    });

                    if(person.getGender().equalsIgnoreCase("f")){
                        personIcon.setImageDrawable(femaleIcon);
                    } else {
                        personIcon.setImageDrawable(maleIcon);
                    }

                    // Draw lines based on filters
                    LineBuilder lineBuilder = new LineBuilder(markerEvent, person.getPersonID(), filteredEvents);

                    Event spouseEvent = lineBuilder.getSpouseEvent();

                    if(spouseEvent != null && lineBuilder.shouldShowLine(markerEvent, spouseEvent, filteredEvents)){
                        drawLine(markerEvent, spouseEvent, Color.RED, (float) 0.1);
                    }

                    drawFamilyTreeLines(markerEvent, filteredEvents);

                    drawLifeStoryLines(person.getPersonID());

                    return false;
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(dataCache.getCurrentActivity() == dataCache.MAIN_ACTIVITY){
            inflater.inflate(R.menu.main_menu, menu);
        } else {
            inflater.inflate(R.menu.up_only_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.search_menu_item:
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings_menu_item:
                // Go to settings activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void drawLifeStoryLines(String personID){
        LineBuilder lineBuilder = new LineBuilder();
        LinkedList<Event> sortedLifeEvents = dataCache.getSortedUserLifeEvents(personID);
        for(int i = 0; i < sortedLifeEvents.size() - 1; i ++){
            Event start = sortedLifeEvents.get(i);
            Event end = sortedLifeEvents.get(i + 1);
            if(lineBuilder.shouldShowLine(start, end, dataCache.getFilteredEvents(personID))){
                drawLine(start, end, Color.GREEN, (float)0.1);
            }
        }
    }

    private void drawFamilyTreeLines(Event currentEvent, ArrayList<Event> filteredEvents){
        LineBuilder lineBuilder = new LineBuilder();
        Person person = dataCache.getPerson(currentEvent.getPersonID());
        Person father = dataCache.getPerson(person.getFatherID());
        Person mother = dataCache.getPerson(person.getMotherID());

        // Add line for dad
        if(father != null){
            Event earliestFatherEvent = dataCache.getEarliestEvent(dataCache.getPersonEvents(father.getPersonID()));
            if(earliestFatherEvent != null){
                if(lineBuilder.shouldShowLine(currentEvent, earliestFatherEvent, filteredEvents)) {
                    drawLine(currentEvent, earliestFatherEvent, Color.BLUE, (float) 0.1);
                    famTreeLines_Helper(earliestFatherEvent, father, filteredEvents, (float) 0.2, lineBuilder);
                }
            }
        }

        // Add lines for mom
        if(mother != null){
            Event earliestMotherEvent = dataCache.getEarliestEvent(dataCache.getPersonEvents(mother.getPersonID()));
            if(earliestMotherEvent != null){
                if(lineBuilder.shouldShowLine(currentEvent, earliestMotherEvent, filteredEvents)){
                    drawLine(currentEvent, earliestMotherEvent, Color.BLUE, (float) 0.1);
                    famTreeLines_Helper(earliestMotherEvent, mother, filteredEvents, (float) 0.2, lineBuilder);
                }
            }
        }
    }

    private void famTreeLines_Helper(Event currentEvent, Person person, ArrayList<Event> filteredEvents, float gen, LineBuilder lineBuilder){

        // Find parents' birth events
        Person father = dataCache.getPerson(person.getFatherID());
        Person mother = dataCache.getPerson(person.getMotherID());

        // Add line for dad
        if(father != null){
            Event earliestFatherEvent = dataCache.getEarliestEvent(dataCache.getPersonEvents(father.getPersonID()));
            if(earliestFatherEvent != null){
                if(lineBuilder.shouldShowLine(currentEvent, earliestFatherEvent, filteredEvents)) {
                    drawLine(currentEvent, earliestFatherEvent, Color.BLUE, gen);
                    famTreeLines_Helper(earliestFatherEvent, father, filteredEvents, gen + (float) 0.1, lineBuilder);
                }
            }
        }

        // Add lines for mom
        if(mother != null){
            Event earliestMotherEvent = dataCache.getEarliestEvent(dataCache.getPersonEvents(mother.getPersonID()));
            if(earliestMotherEvent != null){
                if(lineBuilder.shouldShowLine(currentEvent, earliestMotherEvent, filteredEvents)){
                    drawLine(currentEvent, earliestMotherEvent, Color.BLUE, gen);
                    famTreeLines_Helper(earliestMotherEvent, mother, filteredEvents, gen + (float) 0.1, lineBuilder);
                }
            }
        }
    }

    /**
     * Remove all map lines
     */
    private void removeLines(){
        for(Polyline line : mapLines){
            line.remove();
        }
    }

    /**
     * Draw one line between two events and add line to mapLines
     * @param startEvent start event
     * @param endEvent end event
     * @param googleColor line color
     */
    private void drawLine(Event startEvent, Event endEvent, int googleColor, float width){
        LatLng startPoint = new LatLng(startEvent.getLatitude(), startEvent.getLongitude());
        LatLng endPoint = new LatLng(endEvent.getLatitude(), endEvent.getLongitude());
        PolylineOptions options = new PolylineOptions()
                .add(startPoint)
                .add(endPoint)
                .color(googleColor)
                .width((float)1.0 / width);
        Polyline line = map.addPolyline(options);
        mapLines.add(line);
    }



    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }
}