package com.example.familymapclient;

import java.util.ArrayList;

import model.Event;

public class DataCache {

    private ArrayList<String> maleNames;
    private ArrayList<String> femaleNames;
    private ArrayList<Event> events;
    private ArrayList<String> lastNames;
    private String serverHost = "localhost"; // make these dynamic
    private int serverPort = 8081;

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

    public ArrayList<String> getMaleNames() {
        return maleNames;
    }

    public void setMaleNames(ArrayList<String> maleNames) {
        this.maleNames = maleNames;
    }

    public ArrayList<String> getFemaleNames() {
        return femaleNames;
    }

    public void setFemaleNames(ArrayList<String> femaleNames) {
        this.femaleNames = femaleNames;
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
