package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private final float BIRTH_COLOR = BitmapDescriptorFactory.HUE_BLUE;
    private final float MARRIAGE_COLOR = BitmapDescriptorFactory.HUE_ORANGE;
    private final float DEATH_COLOR = BitmapDescriptorFactory.HUE_RED;
    private Map<String, Float> colorMap;

    private ArrayList<Polyline> mapLines;
    private DataCache dataCache = DataCache.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);

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
        LatLng location = new LatLng(50, 50);
        map.animateCamera(CameraUpdateFactory.newLatLng(location));

        colorMap = new HashMap<>();
        ArrayList<MapEvent> filteredEvents = getFilteredEvents(dataCache.getUserPersonID());
        // Get all map events and display them
        for(MapEvent mapEvent : filteredEvents){
            float color;
            String eventType = mapEvent.getEvent().getEventType();
            if(colorMap.size() == 0){
                color = (float) (0.0 + Math.random() * (360.0 - 0.0));
                colorMap.put(eventType, color);
            } else if(!colorMap.containsKey(eventType)){
                color = (float) (0.0 + Math.random() * (360.0 - 0.0));
                colorMap.put(eventType, color);
            } else {
                color = colorMap.get(eventType);
            }

            LatLng eventLocation = new LatLng(mapEvent.getEvent().getLatitude(),
                    mapEvent.getEvent().getLongitude());

            Marker eventMarker = map.addMarker(
                            new MarkerOptions().
                            position(eventLocation).
                            icon(BitmapDescriptorFactory.defaultMarker(color)));
            assert eventMarker != null;
            eventMarker.setTag(mapEvent);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    removeLines();
                    // Display the person associated with the event on screen
                    MapEvent markerEvent = (MapEvent) marker.getTag();
                    Person person = dataCache.getPerson(markerEvent.getEvent().getPersonID());

                    mapTextView.setText(
                            person.getFirstName().substring(1, person.getFirstName().length() - 1)
                            + " " +
                            person.getLastName().substring(1, person.getLastName().length() - 1) + "\n" +
                            markerEvent.getEvent().getEventType().toUpperCase() + ": " +
                            markerEvent.getEvent().getCity() + ", " +
                            markerEvent.getEvent().getCountry() + " (" + markerEvent.getEvent().getYear() + ")");

                    if(person.getGender().equalsIgnoreCase("f")){
                        personIcon.setImageDrawable(femaleIcon);
                    } else {
                        personIcon.setImageDrawable(maleIcon);
                    }

                    // Draw lines based on filters
                    LineBuilder lineBuilder = new LineBuilder(markerEvent, person.getPersonID(), filteredEvents);

                    Event spouseEvent = lineBuilder.getSpouseEvent();

                    if(spouseEvent != null && lineBuilder.shouldShowLine(markerEvent.getEvent(), spouseEvent, filteredEvents)){
                        drawLine(markerEvent.getEvent(), spouseEvent, Color.RED, (float) 0.1);
                    }

                    drawFamilyTreeLines(markerEvent, filteredEvents);

                    drawLifeStoryLines(person, filteredEvents);

                    return false;
                }
            });
        }
    }

    private void drawLifeStoryLines(Person person, ArrayList<MapEvent> filteredEvents){
        LineBuilder lineBuilder = new LineBuilder();
        // Get all events for person
        ArrayList<Event> personEvents = dataCache.getPersonEvents(person.getPersonID());

        // Sort events in linked list
        LinkedList<Event> lifeEvents = new LinkedList<>();

        Event earliestEvent = dataCache.getEarliestEvent(personEvents);
        if(earliestEvent == null){
            return;
        }

        lifeEvents.add(dataCache.getEarliestEvent(personEvents));


        personEvents.remove(earliestEvent);

        if(personEvents.size() != 0){
            lifeEvents = lifeStory_Helper(personEvents, lifeEvents);
        }

        for(int i = 0; i < lifeEvents.size() - 1; i ++){
            Event start = lifeEvents.get(i);
            Event end = lifeEvents.get(i + 1);
            if(lineBuilder.shouldShowLine(start, end, filteredEvents)){
                drawLine(start, end, Color.GREEN, (float)0.1);
            }
        }
    }

    private LinkedList<Event> lifeStory_Helper(ArrayList<Event> eventsLeft,
                                               LinkedList<Event> sortedEvents){

        Event nextEarlEvent = dataCache.getEarliestEvent(eventsLeft);
        sortedEvents.addLast(nextEarlEvent);
        eventsLeft.remove(nextEarlEvent);

        if(eventsLeft.size() == 0){
            return sortedEvents;
        } else {
            return lifeStory_Helper(eventsLeft, sortedEvents);
        }
    }


    private void drawFamilyTreeLines(MapEvent currentMapEvent, ArrayList<MapEvent> filteredEvents){
        LineBuilder lineBuilder = new LineBuilder();
        Event currentEvent = currentMapEvent.getEvent();
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

    private void famTreeLines_Helper(Event currentEvent, Person person, ArrayList<MapEvent> filteredEvents, float gen, LineBuilder lineBuilder){

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

    /**
     * Get filtered map events based on settings found in DataCache
     * @return
     */
    private ArrayList<MapEvent> getFilteredEvents(String personID){

        DataCache dataCache = DataCache.getInstance();

        // Add spouse events
        ArrayList<MapEvent> allMapEvents = new ArrayList<>();

        // Add spouse events as map events
        for(Event event : dataCache.getSpouseEvents(personID)){
            MapEvent newEvent;
            boolean isMale = dataCache.getPerson(event.getPersonID()).getGender().equalsIgnoreCase("m");
            newEvent = new MapEvent(event, isMale);
            allMapEvents.add(newEvent);
        }

        // Add mom and dad's side of events
        if(dataCache.isShowDadSideEvents()){
            allMapEvents.addAll(dataCache.getDadSideEvents());
        }
        if(dataCache.isShowMomSideEvents()){
            allMapEvents.addAll(dataCache.getMomSideEvents());
        }

        ArrayList<MapEvent> filteredEvents = new ArrayList<>();

        // Add user events here to avoid father/mother side complications
        allMapEvents.addAll(dataCache.getUserEvents());

        // Filter events based on gender
        if(!dataCache.isShowFemaleEvents()){
            for(MapEvent mapEvent : allMapEvents){
                if(!mapEvent.isMaleEvent()){
                    filteredEvents.add(mapEvent);
                }
            }
        }
        if(!dataCache.isShowMaleEvents()){
            for(MapEvent mapEvent : allMapEvents){
                if(mapEvent.isMaleEvent()){
                    filteredEvents.add(mapEvent);
                }
            }
        }

        // Remove filtered events
        allMapEvents.removeAll(filteredEvents);

        return allMapEvents;
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