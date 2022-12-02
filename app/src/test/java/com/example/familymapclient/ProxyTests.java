package com.example.familymapclient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import request.LoginRequest;

public class ProxyTests {

    private final String HOST = "10.0.2.2";
    private final String PORT = "localhost";
    private final String GOOD_USER = "a";
    private final String GOOD_PASS = "a";
    private final String BAD_USER = "asdf";
    private final String BAD_PASS = "asdf";
    private final DataCache dataCache = DataCache.getInstance();

    @Test
    public void loginTestPass(){
        dataCache.setServerHost(HOST);
        dataCache.setServerPort(PORT);
        ServerProxy proxy = new ServerProxy();
        LoginRequest goodLogin = new LoginRequest(GOOD_USER, GOOD_PASS);
        Assert.assertTrue(proxy.login(goodLogin).isSuccess());
    }
}
