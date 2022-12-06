package com.example.familymapclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import model.Event;
import model.Person;
import request.LoginRequest;
import request.RegisterRequest;

public class ModelTests {

    private final DataCache dataCache = DataCache.getInstance();
    private final String HOST = "localhost";
    private final String PORT = "8081";
    private final LoginRequest goodLogin = new LoginRequest("sheila", "parker");
    private final ServerProxy proxy = new ServerProxy();

    @Before
    public void setUp() {

    }

    @Test
    public void getFamilyRelationshipsTest() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.login(goodLogin);

        Person testPerson = dataCache.getPerson(dataCache.getPerson(dataCache.getUserPersonID()).getMotherID());

        ArrayList<Person> children = dataCache.getPersonChildren(testPerson.getPersonID());
        Person father = dataCache.getPersonFather(testPerson.getPersonID());
        Person mother = dataCache.getPersonMother(testPerson.getPersonID());
        Assert.assertTrue(isCorrectChildren(children, testPerson));
        Assert.assertTrue(isCorrectFather(father, testPerson));
        Assert.assertTrue(isCorrectMother(mother, testPerson));
    }

    @Test
    public void testFilterSettings () {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        dataCache.setShowMomSideEvents(false);
        proxy.login(goodLogin);

        ArrayList<Event> filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());
        ArrayList<Event> momSideEvents = dataCache.getMomSideEvents();

        Assert.assertTrue(filterSideWorks(filteredEvents, momSideEvents));

        dataCache.setShowMomSideEvents(true);
        dataCache.setShowDadSideEvents(false);

        filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());
        ArrayList<Event> dadSideEvents = dataCache.getDadSideEvents();

        Assert.assertTrue(filterSideWorks(filteredEvents, dadSideEvents));

        dataCache.setShowDadSideEvents(true);
        dataCache.setShowMaleEvents(false);

        filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());
        Assert.assertTrue(filterGenderWorks(filteredEvents, "m"));

        dataCache.setShowMaleEvents(true);
        dataCache.setShowFemaleEvents(false);

        filteredEvents = dataCache.getFilteredEvents(dataCache.getUserPersonID());
        Assert.assertTrue(filterGenderWorks(filteredEvents, "f"));

        dataCache.setShowFemaleEvents(true);
    }

    @Test
    public void testLifeEventOrder() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        dataCache.setShowMomSideEvents(false);
        proxy.login(goodLogin);

        LinkedList<Event> lifeEvents = dataCache.getSortedUserLifeEvents(dataCache.getUserPersonID());

        Assert.assertTrue(correctLifeEventOrder(lifeEvents));
    }

    @Test
    public void testSearch() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        dataCache.setShowMomSideEvents(false);
        proxy.login(goodLogin);

        ArrayList<Person> filteredPeople = new ArrayList<>();
        ArrayList<Event>  filteredEvents = new ArrayList<>();

        String search = "jon";
        dataCache.refineSearch(filteredPeople, filteredEvents, search);
        Assert.assertTrue(checkGoodSearch(filteredPeople, filteredEvents, search));

        search = "garbage";
        dataCache.refineSearch(filteredPeople, filteredEvents, search);
        Assert.assertTrue(checkGoodSearch(filteredPeople, filteredEvents, search));

        search = "";
        dataCache.refineSearch(filteredPeople, filteredEvents, search);
        Assert.assertTrue(checkGoodSearch(filteredPeople, filteredEvents, search));

        search = "1";
        dataCache.refineSearch(filteredPeople, filteredEvents, search);
        Assert.assertTrue(checkGoodSearch(filteredPeople, filteredEvents, search));
    }

    private boolean checkGoodSearch(ArrayList<Person> filteredPeople, ArrayList<Event>  filteredEvents, String search){
        for(Person person : filteredPeople){
            if(!(person.getFirstName() + person.getLastName()).toLowerCase().contains(search.toLowerCase())){
                return false;
            }
        }
        for(Event event : filteredEvents){
            if(!dataCache.getEventDetails(event).toLowerCase().contains(search.toLowerCase())){
                return false;
            }
        }
        return true;
    }

    private boolean correctLifeEventOrder(LinkedList<Event> lifeEvents){
        for(int i = 1; i < lifeEvents.size() - 1; i ++){
            if(lifeEvents.get(i).getYear() < lifeEvents.get(i - 1).getYear()){
                return false;
            }
        }

        return lifeEvents.get(lifeEvents.size() - 1).getEventType().equalsIgnoreCase("death");
    }

    private boolean filterGenderWorks(ArrayList<Event> filteredEvents, String gender){
        for(Event filteredEvent : filteredEvents){
            Person personOfEvent = dataCache.getPerson(filteredEvent.getPersonID());
            if(personOfEvent.getGender().equalsIgnoreCase(gender)){
                return false;
            }
        }
        return true;
    }

    private boolean filterSideWorks(ArrayList<Event> filteredEvents, ArrayList<Event> omittedEvents){
        for(Event filteredEvent : filteredEvents){
            if(omittedEvents.contains(filteredEvent)){
                return false;
            }
        }
        return true;
    }

    private boolean isCorrectFather(Person father, Person person){
        if(person.getFatherID() != null){
            return person.getFatherID().equals(father.getPersonID());
        } else {
            return person.getFatherID() == null;
        }
    }

    private boolean isCorrectMother(Person mother, Person person){
        if(person.getMotherID() != null){
            return person.getMotherID().equals(mother.getPersonID());
        } else {
            return person.getMotherID() == null;
        }

    }

    private boolean isCorrectChildren(ArrayList<Person> children, Person observedPerson){

        ArrayList<String> childrenIDs = new ArrayList<>();
        for(Person child : children){
            childrenIDs.add(child.getPersonID());
        }

        for(Person person : dataCache.getPeople()){
            if(person.getFatherID() != null){
                if(person.getFatherID().equals(observedPerson.getPersonID())){
                    if(!childrenIDs.contains(person.getPersonID())){
                        return false;
                    }
                }
            }
            if(person.getMotherID() != null){
                if(person.getMotherID().equals(observedPerson.getPersonID())){
                    if(!childrenIDs.contains(person.getPersonID())){
                        return false;
                    }
                }
            }

        }
        return true;
    }

}
