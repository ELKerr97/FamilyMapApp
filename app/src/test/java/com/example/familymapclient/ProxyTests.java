package com.example.familymapclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import model.Event;
import model.Person;
import request.LoginRequest;
import request.RegisterRequest;

public class ProxyTests {

    private final String HOST = "localhost";
    private final String PORT = "8081";
    private final String GOOD_USER = "Bob";
    private final String GOOD_PASS = "Carr";
    private final String BAD_USER = "asdf";
    private final String BAD_PASS = "asdf";
    private final DataCache dataCache = DataCache.getInstance();
    private final RegisterRequest registerRequest = new RegisterRequest(
            "Bob",
            "Carr",
            "bob.carr@email.com",
            "Bob",
            "Carr",
            "m"
    );
    private final RegisterRequest newRegisterRequest = new RegisterRequest(
            "Bill",
            "Cob",
            "bob.bill@email.com",
            "Bill",
            "Cob",
            "m"
    );
    private final LoginRequest goodLogin = new LoginRequest(GOOD_USER, GOOD_PASS);
    private final ServerProxy proxy = new ServerProxy();

    @Test
    public void registerTestPass() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        Assert.assertTrue(proxy.register(newRegisterRequest).isSuccess());
    }

    @Test
    public void registerTestFail(){
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        proxy.register(registerRequest);
        Assert.assertFalse(proxy.register(registerRequest).isSuccess());
    }

    @Test
    public void loginTestPass(){
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        proxy.register(registerRequest);
        Assert.assertTrue(proxy.login(goodLogin).isSuccess());
    }

    @Test
    public void loginTestFail() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        proxy.register(registerRequest);
        LoginRequest badLogin = new LoginRequest(GOOD_USER, BAD_PASS);
        Assert.assertFalse(proxy.login(badLogin).isSuccess());
    }


    @Test
    public void getCorrectPeopleTest() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        proxy.register(registerRequest);
        proxy.login(goodLogin);
        Assert.assertTrue(isCorrectPeople());
    }

    @Test
    public void getCorrectEventsTest() {
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        proxy.clear();
        proxy.register(registerRequest);
        proxy.login(goodLogin);
        Assert.assertTrue(isCorrectEvents());
    }

    private boolean isCorrectPeople () {
        ArrayList<Person> people = dataCache.getPeople();
        for(Person person : people){
            if(!person.getAssociatedUsername().equals(dataCache.getUsername())){
                return false;
            }
        }
        return true;
    }

    private boolean isCorrectEvents() {
        ArrayList<Event> events = dataCache.getAllEvents();
        for(Event event : events){
            if(!event.getAssociatedUsername().equals(dataCache.getUsername())){
                return false;
            }
        }
        return true;
    }

}
