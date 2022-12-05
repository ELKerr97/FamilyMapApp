package com.example.familymapclient;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Event;
import model.Person;

public class DataCache {

    private ArrayList<Event> allEvents;
    private ArrayList<Event> momSideEvents;
    private ArrayList<Event> dadSideEvents;
    private ArrayList<Person> people;
    private ArrayList<String> lastNames;
    private String userPersonID;
    private String userAuthToken;
    private String serverHost;
    private String serverPort;
    private final Map<String, Float> eventColors;

    private boolean userLoggedIn;

    private boolean showMaleEvents;
    private boolean showFemaleEvents;
    private boolean showMomSideEvents;
    private boolean showDadSideEvents;

    private boolean showSpouseLines;
    private boolean showFamilyTreeLines;
    private boolean showLifeStoryLines;

    public final int MAIN_ACTIVITY = 0;
    public final int EVENT_ACTIVITY = 1;
    public final int SETTINGS_ACTIVITY = 2;
    public final int SEARCH_ACTIVITY = 3;
    private int currentActivity;

    private Event currentMapEvent;

    private static DataCache instance;

    public static void setInstance(DataCache instance){
        DataCache.instance = instance;
    }

    public static DataCache getInstance(){
        if(instance == null){
            instance = new DataCache();
        }
        return instance;
    }

    private DataCache() {
        this.showMaleEvents = true;
        this.showFemaleEvents = true;
        this.showDadSideEvents = true;
        this.showMomSideEvents = true;
        this.showSpouseLines = true;
        this.showFamilyTreeLines = true;
        this.showLifeStoryLines = true;
        this.currentMapEvent = null;
        this.userLoggedIn = false;
        this.currentActivity = MAIN_ACTIVITY;
        this.eventColors = new HashMap<>();
    }

    public boolean userLoggedIn(){
        return userLoggedIn;
    }

    public void setUserLoggedIn(boolean loggedIn){
        this.userLoggedIn = loggedIn;
    }

    public void setCurrentActivity(int activity){
        this.currentActivity = activity;
    }

    public int getCurrentActivity(){
        return currentActivity;
    }

    public Event getCurrentMapEvent () {
        return currentMapEvent;
    }

    public void setCurrentMapEvent(Event event){
        this.currentMapEvent = event;
    }

    /**
     * Get a Person object based on a personID string
     * @param personID String of person ID
     * @return Person object
     */
    public Person getPerson(String personID){
        for(Person person : people){
            if(person.getPersonID().equals(personID)){
                return person;
            }
        }

        return null;
    }

    public float getEventColor(String eventType){
        float eventFloat;
        try{
            eventFloat = eventColors.get(eventType);
            return eventFloat;
        } catch (NullPointerException ex){
            System.out.println("Color does not exist for event: " + eventType);
            ex.printStackTrace();
            return 0.0f;
        }
    }

    public void setEventColors() {
        int colorIndex = 0;
        List<Float> colors = new ArrayList<>();
        colors.add(BitmapDescriptorFactory.HUE_BLUE);
        colors.add(BitmapDescriptorFactory.HUE_ORANGE);
        colors.add(BitmapDescriptorFactory.HUE_YELLOW);
        colors.add(BitmapDescriptorFactory.HUE_RED);
        colors.add(BitmapDescriptorFactory.HUE_GREEN);
        colors.add(BitmapDescriptorFactory.HUE_AZURE);
        colors.add(BitmapDescriptorFactory.HUE_ROSE);
        colors.add(BitmapDescriptorFactory.HUE_CYAN);
        colors.add(BitmapDescriptorFactory.HUE_MAGENTA);
        colors.add(BitmapDescriptorFactory.HUE_VIOLET);
        for (Event event : allEvents) {
            float color;
            String eventType = event.getEventType().toLowerCase();
            if (eventColors.size() == 0) {
                color = colors.get(colorIndex);
                eventColors.put(eventType, color);
                colorIndex += 1;
            } else if (!eventColors.containsKey(eventType)) {
                // If all colors have been used, make random color
                if(colorIndex > colors.size() - 1){
                    color = (float) (0.0 + Math.random() * (360.0 - 0.0));
                } else {
                    color = colors.get(colorIndex);
                }
                eventColors.put(eventType, color);
                colorIndex += 1;
            }
        }
    }

    public Event getEarliestEvent (ArrayList<Event> mapEvents){
        Event currentEarlEvent;
        if(mapEvents.size() != 0) {
            currentEarlEvent = mapEvents.get(0);
            for (Event mapEvent : mapEvents) {
                if (mapEvent.getYear() < currentEarlEvent.getYear()) {
                    currentEarlEvent = mapEvent;
                }
            }
            return currentEarlEvent;
        } else {
            return null;
        }
    }

    public LinkedList<Event> getSortedUserLifeEvents (String personID){
        ArrayList<Event> filteredEvents = getFilteredEvents(personID);
        ArrayList<Event> personEvents = getPersonEvents(personID);
        LinkedList<Event> sortedLifeEvents = new LinkedList<>();

        Event earliestEvent = getEarliestEvent(personEvents);
        if(earliestEvent == null){
            return null;
        }

        sortedLifeEvents.add(earliestEvent);
        personEvents.remove(earliestEvent);

        if(personEvents.size() != 0){
            sortedLifeEvents = lifeStory_Helper(personEvents, sortedLifeEvents);
        }

        return sortedLifeEvents;
    }

    public ArrayList<Person> getPersonChildren (String personID) {
        ArrayList<Person> children = new ArrayList<>();
        for(Person person : people){
            if(person.getFatherID() != null){
                if(person.getFatherID().equals(personID)){
                    children.add(person);
                }
            }
            if(person.getMotherID() != null){
                if(person.getMotherID().equals(personID)){
                    children.add(person);
                }
            }
        }
        return children;
    }

    public Person getPersonFather (String personID){
        return getPerson(getPerson(personID).getFatherID());
    }

    public Person getPersonMother (String personID){
        return getPerson(getPerson(personID).getMotherID());
    }

    public Person getPersonSpouse (String personID){
        return getPerson(getPerson(personID).getSpouseID());
    }

    private LinkedList<Event> lifeStory_Helper(ArrayList<Event> eventsLeft,
                                               LinkedList<Event> sortedEvents){

        Event nextEarlEvent = getEarliestEvent(eventsLeft);
        sortedEvents.addLast(nextEarlEvent);
        eventsLeft.remove(nextEarlEvent);

        if(eventsLeft.size() == 0){
            return sortedEvents;
        } else {
            return lifeStory_Helper(eventsLeft, sortedEvents);
        }
    }

    public ArrayList<Event> getSpouseEvents(String personID){
        ArrayList<Event> spouseMapEvents = new ArrayList<>();
        if(!getPerson(personID).getSpouseID().equals("")){
            String spouseID = getPerson(personID).getSpouseID();
            spouseMapEvents.addAll(getPersonEvents(spouseID));
        }
        return spouseMapEvents;
    }

    public ArrayList<Event> getUserEvents() {
        return new ArrayList<>(getPersonEvents(userPersonID));
    }

    public String getEventDetails(Event event){
        return event.getEventType().toUpperCase() + ": " +
                event.getCity() + ", " +
                event.getCountry() + " (" + event.getYear() + ")";
    }

    public String getPersonOfEvent(Event event){
        return getPerson(event.getPersonID()).getFirstName() + " " +
                getPerson(event.getPersonID()).getLastName();
    }

    /**
     * Get filtered map events based on settings found in DataCache
     * @return filtered events
     */
    public ArrayList<Event> getFilteredEvents(String personID){

        DataCache dataCache = DataCache.getInstance();

        // Add spouse events
        ArrayList<Event> allMapEvents = new ArrayList<>(getSpouseEvents(personID));

        // Add mom and dad's side of events
        if(dataCache.isShowDadSideEvents()){
            allMapEvents.addAll(dataCache.getDadSideEvents());
        }
        if(dataCache.isShowMomSideEvents()){
            allMapEvents.addAll(dataCache.getMomSideEvents());
        }

        ArrayList<Event> filteredEvents = new ArrayList<>();

        // Add user events here to avoid father/mother side complications
        allMapEvents.addAll(dataCache.getUserEvents());

        // Filter events based on gender
        if(!dataCache.isShowFemaleEvents()){
            for(Event mapEvent : allMapEvents){
                if(!isMaleEvent(mapEvent)){
                    filteredEvents.add(mapEvent);
                }
            }
        }

        if(!dataCache.isShowMaleEvents()){
            for(Event mapEvent : allMapEvents){
                if(isMaleEvent(mapEvent)){
                    filteredEvents.add(mapEvent);
                }
            }
        }

        // Remove filtered events
        allMapEvents.removeAll(filteredEvents);

        return allMapEvents;
    }

    public ArrayList<Person> getFilteredPeople() {
        Set<Person> uniquePeople = new HashSet<>();

        for(Event event : getFilteredEvents(userPersonID)){
            uniquePeople.add(getPerson(event.getPersonID()));
        }

        return new ArrayList<>(uniquePeople);
    }

    private boolean isMaleEvent(Event event){
        Person associatedPerson = getPerson(event.getPersonID());
        return associatedPerson.getGender().equalsIgnoreCase("m");
    }

    public ArrayList<Event> getMomSideEvents() {
        momSideEvents = new ArrayList<>();
        setSideEvents_Helper(getPerson(userPersonID).getMotherID(), momSideEvents, 1, false);
        return momSideEvents;
    }

    public ArrayList<Event> getDadSideEvents() {
        dadSideEvents = new ArrayList<>();
        setSideEvents_Helper(getPerson(userPersonID).getFatherID(), dadSideEvents, 1, true);
        return dadSideEvents;
    }

    public void setSideEvents_Helper(String personID, ArrayList<Event> mapEvents, int gen, boolean isFatherSide) {

        // Add this person's events
        mapEvents.addAll(getPersonEvents(personID));

        // Get parent ID's
        String motherID = getPerson(personID).getMotherID();
        String fatherID = getPerson(personID).getFatherID();

        // If mother exists, add her and her parents' events
        if(motherID != null){
            setSideEvents_Helper(motherID, mapEvents, gen + 1, isFatherSide);
        }

        // If father exists, add his and his parent's events
        if(fatherID != null){
            setSideEvents_Helper(fatherID, mapEvents, gen + 1, isFatherSide);
        }
    }

    /**
     * Get events associated with a specific person
     * @param personID String of person ID
     * @return List of Events
     */
    public ArrayList<Event> getPersonEvents (String personID){
        ArrayList<Event> personEvents = new ArrayList<>();
        for(Event event : allEvents){
            if(event.getPersonID().equals(personID)){
                personEvents.add(event);
            }
        }
        return personEvents;
    }

    public void setMomSideEvents(ArrayList<Event> momSideEvents) {
        this.momSideEvents = momSideEvents;
    }

    public void setDadSideEvents(ArrayList<Event> dadSideEvents) {
        this.dadSideEvents = dadSideEvents;
    }

    public boolean isShowMaleEvents() {
        return showMaleEvents;
    }

    public void setShowMaleEvents(boolean showMaleEvents) {
        this.showMaleEvents = showMaleEvents;
    }

    public boolean isShowFemaleEvents() {
        return showFemaleEvents;
    }

    public void setShowFemaleEvents(boolean showFemaleEvents) {
        this.showFemaleEvents = showFemaleEvents;
    }

    public boolean isShowMomSideEvents() {
        return showMomSideEvents;
    }

    public void setShowMomSideEvents(boolean showMomSideEvents) {
        this.showMomSideEvents = showMomSideEvents;
    }

    public boolean isShowDadSideEvents() {
        return showDadSideEvents;
    }

    public void setShowDadSideEvents(boolean showDadSideEvents) {
        this.showDadSideEvents = showDadSideEvents;
    }

    public boolean isShowSpouseLines() {
        return showSpouseLines;
    }

    public void setShowSpouseLines(boolean showSpouseLines) {
        this.showSpouseLines = showSpouseLines;
    }

    public boolean isShowFamilyTreeLines() {
        return showFamilyTreeLines;
    }

    public void setShowFamilyTreeLines(boolean showFamilyTreeLines) {
        this.showFamilyTreeLines = showFamilyTreeLines;
    }

    public boolean isShowLifeStoryLines() {
        return showLifeStoryLines;
    }

    public void setShowLifeStoryLines(boolean showLifeStoryLines) {
        this.showLifeStoryLines = showLifeStoryLines;
    }

    public String getUserAuthToken() {
        return userAuthToken;
    }

    public void setUserAuthToken(String userAuthToken) {
        this.userAuthToken = userAuthToken;
    }

    public String getUserPersonID() {
        return userPersonID;
    }

    public void setUserPersonID(String userPersonID) {
        this.userPersonID = userPersonID;
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }

    public ArrayList<Event> getAllEvents() {
        return allEvents;
    }

    public void setAllEvents(ArrayList<Event> allEvents) {
        this.allEvents = allEvents;
    }

    public ArrayList<String> getLastNames() {
        return lastNames;
    }

    public void setLastNames(ArrayList<String> lastNames) {
        this.lastNames = lastNames;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
}
