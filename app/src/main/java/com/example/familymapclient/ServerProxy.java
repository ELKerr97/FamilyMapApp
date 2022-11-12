package com.example.familymapclient;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import service.LoginService;

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
