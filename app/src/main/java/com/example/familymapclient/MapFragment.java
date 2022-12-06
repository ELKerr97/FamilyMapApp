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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Random;

import model.Event;
import model.Person;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap map;
    private ArrayList<Polyline> mapLines;
    private DataCache dataCache = DataCache.getInstance();
    private TextView mapTextView;
    private ImageView personIcon;
    private Drawable maleIcon;
    private Drawable femaleIcon;
    private Event observedEvent;

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
        dataCache.setGoogleMap(googleMap);
        map.setOnMapLoadedCallback(this);

        mapTextView = getView().findViewById(R.id.mapEventDescription);
        personIcon = getView().findViewById(R.id.personIcon);
        personIcon.setImageResource(R.drawable.ic_launcher_foreground);

        maleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male)
                .colorRes(R.color.male_color).sizeDp(40);
        femaleIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female)
                .colorRes(R.color.female_color).sizeDp(40);

        drawMarkers();

        if(observedEvent != null){
            centerOnEvent(observedEvent);
            drawEventLines(observedEvent);
            setEventDescription(observedEvent);
        }

    }

    public void drawMarkers() {
        if(map  == null){
            map = dataCache.getGoogleMap();
        }
        map.clear();
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

                    setObservedEvent(markerEvent);

                    setEventDescription(markerEvent);

                    drawEventLines(markerEvent);

                    return false;
                }
            });
        }
    }

    public void setEventDescription(Event event){

        Person person = dataCache.getPerson(event.getPersonID());

        mapTextView.setText(
                dataCache.getPersonOfEvent(event) + "\n" +
                        dataCache.getEventDetails(event));

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
    }

    public void setObservedEvent(Event event){
        this.observedEvent = event;
    }

    public Event getObservedEvent() {
        return observedEvent;
    }

    public void centerOnEvent(Event event) {
        if(map == null){
            map = dataCache.getGoogleMap();
        }
        LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(location));
    }

    public void drawEventLines(Event event){

        ArrayList<Event> filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());

        Person person = dataCache.getPerson(event.getPersonID());
        // Draw lines based on filters
        LineBuilder lineBuilder = new LineBuilder(event, person.getPersonID(), filteredEvents);

        Event spouseEvent = lineBuilder.getSpouseEvent();

        if(dataCache.isShowSpouseLines()) {
            if (spouseEvent != null && lineBuilder.shouldShowLine(event, spouseEvent, filteredEvents)) {
                drawLine(event, spouseEvent, Color.RED, (float) 0.1);
            }
        }

        if(dataCache.isShowFamilyTreeLines()){
            drawFamilyTreeLines(event, filteredEvents);
        }

        if(dataCache.isShowLifeStoryLines()){
            drawLifeStoryLines(person.getPersonID());
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
        if(map  == null){
            map = dataCache.getGoogleMap();
        }
        LatLng startPoint = new LatLng(startEvent.getLatitude(), startEvent.getLongitude());
        LatLng endPoint = new LatLng(endEvent.getLatitude(), endEvent.getLongitude());
        PolylineOptions options = new PolylineOptions()
                .add(startPoint)
                .add(endPoint)
                .color(googleColor)
                .width((float)1.0 / width);
        Polyline line = map.addPolyline(options);
        if(mapLines == null){
            mapLines = new ArrayList<>();
        }
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