package com.example.familymapclient;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;

import model.Event;
import model.Person;

public class LineBuilder {

    /**
     * Lines will be made based on this event
     */
    private Event currentEvent;

    /**
     * Person ID associated with the observed event
     */
    private String personID;

    /**
     * DataCache from which we pull MapEvents
     */
    private DataCache dataCache = DataCache.getInstance();

    /**
     * MapEvents that are available for line-drawing
     */
    private ArrayList<Event> filteredEvents;

    public LineBuilder (Event currentEvent, String personID, ArrayList<Event> filteredEvents){
        this.currentEvent = currentEvent;
        this.personID = personID;
        this.filteredEvents = filteredEvents;
    }

    public LineBuilder() {}

    /**
     * Determine if a line between two events should be drawn.
     * Return true if both events are shown on map
     * Return false if events are not shown on map
     * @param mapEvent1 first event
     * @param mapEvent2 second event
     * @return
     */
    public boolean shouldShowLine(Event mapEvent1, Event mapEvent2, ArrayList<Event> events){
        for(Event filteredEvent : events){
            if(filteredEvent.getEventID().equals(mapEvent1.getEventID())){
                for(Event filteredEvent1 : events){
                    if(filteredEvent1.getEventID().equals(mapEvent2.getEventID())){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Event getSpouseEvent() {
        ArrayList<Event> spouseEvents = dataCache.getSpouseEvents(personID);
        if(spouseEvents.size() != 0){
            return (dataCache.getEarliestEvent(spouseEvents));
        }
        return null;
    }


}
