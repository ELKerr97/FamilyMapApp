package com.example.familymapclient;

import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import service.LoginService;

public class ServerProxy {

    public ServerProxy (){}

    public LoginResult login(LoginRequest loginRequest) {

        LoginService loginService = new LoginService();
        return loginService.login(loginRequest);
    }
}
