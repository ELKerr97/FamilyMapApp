package com.example.familymapclient;

import model.Event;

public class MapEvent {

    private boolean isFatherSide;
    private boolean isMaleEvent;
    private int generation;
    private Event event;

    public MapEvent(boolean isFatherSide, boolean isMaleEvent, int generation, Event event) {
        this.isFatherSide = isFatherSide;
        this.isMaleEvent = isMaleEvent;
        this.generation = generation;
        this.event = event;
    }

    public MapEvent(Event event, boolean isMaleEvent){
        this.event = event;
        this.isMaleEvent = isMaleEvent;
        this.generation = 0;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isFatherSide() {
        return isFatherSide;
    }

    public void setFatherSide(boolean fatherSide) {
        isFatherSide = fatherSide;
    }

    public boolean isMaleEvent() {
        return isMaleEvent;
    }

    public void setMaleEvent(boolean maleEvent) {
        isMaleEvent = maleEvent;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }
}
