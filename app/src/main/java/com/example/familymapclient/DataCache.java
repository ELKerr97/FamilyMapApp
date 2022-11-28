package com.example.familymapclient;

import java.util.ArrayList;

import model.Event;
import model.Person;
import model.User;

public class DataCache {

    private ArrayList<Event> events;
    private ArrayList<Person> people;
    private ArrayList<String> lastNames;
    private String userPersonID;
    private String userAuthToken;
    private String serverHost;
    private int serverPort;

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

    private DataCache() {}

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

    /**
     * Get events associated with a specific person
     * @param personID String of person ID
     * @return List of Events
     */
    public ArrayList<Event> getPersonEvents (String personID){
        ArrayList<Event> personEvents = new ArrayList<>();
        for(Event event : events){
            if(event.getAssociatedUsername().equals(personID)){
                personEvents.add(event);
            }
        }

        return personEvents;
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
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
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

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
