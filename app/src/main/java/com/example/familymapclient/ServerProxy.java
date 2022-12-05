package com.example.familymapclient;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import request.EventRequest;
import request.LoginRequest;
import request.PersonRequest;
import request.RegisterRequest;
import result.EventResult;
import result.LoginResult;
import result.PersonResult;
import result.RegisterResult;

public class ServerProxy {

    private DataCache dataCache;

    public ServerProxy (){
        this.dataCache = DataCache.getInstance();
    }

    public LoginResult login(LoginRequest loginRequest) {
        LoginResult loginResult;
        try {
            // Make request body
            URL url = new URL("http://" + dataCache.getServerHost() +
                ":" + dataCache.getServerPort() + "/user/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();
            String loginRequestBody = gson.toJson(loginRequest);
            OutputStream reqBody = connection.getOutputStream();
            writeString(loginRequestBody, reqBody);
            reqBody.close();

            int responseCode = connection.getResponseCode();

            if(responseCode == 200){
                InputStream resBody = connection.getInputStream();
                String responseData = readString(resBody);
                loginResult = gson.fromJson(responseData, LoginResult.class);
                EventRequest eventRequest = new EventRequest(loginResult.getAuthtoken(), null);
                PersonRequest personRequest = new PersonRequest(loginResult.getAuthtoken(), "");
                // set user Auth Token in data cache for later use
                dataCache.setUserAuthToken(loginResult.getAuthtoken());
                getEvents(eventRequest);
                getPeople(personRequest);
            } else {
                InputStream resBody = connection.getErrorStream();
                String responseData = readString(resBody);
                loginResult = gson.fromJson(responseData, LoginResult.class);
            }

            return loginResult;

        } catch (IOException e) {
            e.printStackTrace();
            return new LoginResult(null, null, null, false,
                    "Error: Invalid server host or server port.");
        }
    }

    public void getPeople (PersonRequest personRequest){
        PersonResult personResult;
        try {
            // Make request body
            URL url = new URL("http://" + dataCache.getServerHost() +
                    ":" + dataCache.getServerPort() + "/person");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", personRequest.getAuthtoken());
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();

            int responseCode = connection.getResponseCode();

            if(responseCode == 200){
                InputStream resBody = connection.getInputStream();
                String responseData = readString(resBody);
                personResult = gson.fromJson(responseData, PersonResult.class);
            } else {
                InputStream resBody = connection.getErrorStream();
                String responseData = readString(resBody);
                personResult = gson.fromJson(responseData, PersonResult.class);
            }

            if (personRequest.getPersonID().equalsIgnoreCase("")){
                dataCache.setPeople(personResult.getData());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void getEvents (EventRequest eventRequest){
        EventResult eventResult;
        try {
            // Make request body
            URL url = new URL("http://" + dataCache.getServerHost() +
                    ":" + dataCache.getServerPort() + "/event");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", eventRequest.getAuthtoken());
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();

            int responseCode = connection.getResponseCode();

            if(responseCode == 200){
                InputStream resBody = connection.getInputStream();
                String responseData = readString(resBody);
                eventResult = gson.fromJson(responseData, EventResult.class);
            } else {
                InputStream resBody = connection.getErrorStream();
                String responseData = readString(resBody);
                eventResult = gson.fromJson(responseData, EventResult.class);
            }

            dataCache.setAllEvents(eventResult.getData());
            dataCache.setEventColors();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        RegisterResult registerResult;
        try {
            // Make request body
            URL url = new URL("http://" + dataCache.getServerHost() +
                    ":" + dataCache.getServerPort() + "/user/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();
            String registerRequestBody = gson.toJson(registerRequest);
            OutputStream reqBody = connection.getOutputStream();
            writeString(registerRequestBody, reqBody);
            reqBody.close();

            int responseCode = connection.getResponseCode();

            if(responseCode == 200){
                InputStream resBody = connection.getInputStream();
                String responseData = readString(resBody);
                registerResult = gson.fromJson(responseData, RegisterResult.class);
            } else {
                InputStream resBody = connection.getErrorStream();
                String responseData = readString(resBody);
                registerResult = gson.fromJson(responseData, RegisterResult.class);
            }

            return registerResult;

        } catch (IOException e) {
            e.printStackTrace();
            return new RegisterResult(null, null, null, false,
                    "Error: Invalid server host or server port.");
        }
    }

    protected void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    protected String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
}
