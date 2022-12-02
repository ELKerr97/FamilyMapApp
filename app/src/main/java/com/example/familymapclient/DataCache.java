package com.example.familymapclient;

import java.util.ArrayList;

import model.Event;
import model.Person;

public class DataCache {

    private ArrayList<Event> allEvents;
    private ArrayList<MapEvent> momSideEvents;
    private ArrayList<MapEvent> dadSideEvents;
    private ArrayList<Person> people;
    private ArrayList<String> lastNames;
    private ArrayList<MapEvent> mapEvents;
    private String userPersonID;
    private String userAuthToken;
    private String serverHost;
    private String serverPort;

    private boolean showMaleEvents;
    private boolean showFemaleEvents;
    private boolean showMomSideEvents;
    private boolean showDadSideEvents;

    private boolean showSpouseLines;
    private boolean showFamilyTreeLines;
    private boolean showLifeStoryLines;

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

    public ArrayList<Event> getSpouseEvents(String personID){
        ArrayList<Event> spouseMapEvents = new ArrayList<>();
        if(!getPerson(personID).getSpouseID().equals("")){
            String spouseID = getPerson(personID).getSpouseID();
            spouseMapEvents.addAll(getPersonEvents(spouseID));
        }
        return spouseMapEvents;
    }

    public ArrayList<MapEvent> getUserEvents() {
        ArrayList<MapEvent> userMapEvents = new ArrayList<>();
        for(Event event : getPersonEvents(userPersonID)){
            MapEvent mapEvent;
            boolean isMale = getPerson(userPersonID).getGender().equalsIgnoreCase("m");
            mapEvent = new MapEvent(event, isMale);
            userMapEvents.add(mapEvent);
        }
        return userMapEvents;
    }

    public ArrayList<MapEvent> getMomSideEvents() {
        momSideEvents = new ArrayList<>();
        setSideEvents_Helper(getPerson(userPersonID).getMotherID(), momSideEvents, 1, false);
        return momSideEvents;
    }

    public ArrayList<MapEvent> getDadSideEvents() {
        dadSideEvents = new ArrayList<>();
        setSideEvents_Helper(getPerson(userPersonID).getFatherID(), dadSideEvents, 1, true);
        return dadSideEvents;
    }

    public void setSideEvents_Helper(String personID, ArrayList<MapEvent> mapEvents, int gen, boolean isFatherSide) {

        // Add this person's events
        for(Event event : getPersonEvents(personID)){
            MapEvent mapEvent;
            boolean isMale = getPerson(event.getPersonID()).getGender().equalsIgnoreCase("m");
            mapEvent = new MapEvent(isFatherSide, isMale, gen, event);
            mapEvents.add(mapEvent);
        }

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

    public void setMomSideEvents(ArrayList<MapEvent> momSideEvents) {
        this.momSideEvents = momSideEvents;
    }

    public void setDadSideEvents(ArrayList<MapEvent> dadSideEvents) {
        this.dadSideEvents = dadSideEvents;
    }

    public ArrayList<MapEvent> getMapEvents() {
        return mapEvents;
    }

    public void setMapEvents(ArrayList<MapEvent> mapEvents) {
        this.mapEvents = mapEvents;
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
