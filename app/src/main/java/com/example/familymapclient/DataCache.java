package com.example.familymapclient;

import java.util.ArrayList;

import model.Event;
import model.Person;
import model.User;

public class DataCache {

    private ArrayList<Event> events;
    private ArrayList<Person> people;
    private ArrayList<String> lastNames;
    private User user;
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

    public Person getPerson(String personID){
        for(Person person : people){
            if(person.getPersonID().equals(personID)){
                return person;
            }
        }

        return null;
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<Person> people) {
        this.people = people;
    }

    public ArrayList<Event> getEvents() {
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
