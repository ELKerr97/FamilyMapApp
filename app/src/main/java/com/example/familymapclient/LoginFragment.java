package com.example.familymapclient;

import android.content.Context;
import android.location.GnssAntennaInfo;
import android.os.Bundle;

import androidx.core.app.RemoteInput;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import request.LoginRequest;
import result.LoginResult;


public class LoginFragment extends Fragment {

    private Listener listener;
    private static final String LOGIN_RESULT_KEY = "LoginResultKey";

    public interface Listener {
        void notifyDone();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    public void registerListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    // Fragment inflates widget trees
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Login and Register buttons
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);

        // Edit Texts and Radios
        EditText serverHost = view.findViewById(R.id.server_host);
        EditText serverPort = view.findViewById(R.id.server_port);
        EditText username = view.findViewById(R.id.username);
        EditText password = view.findViewById(R.id.password);
        EditText firstName = view.findViewById(R.id.first_name);
        EditText lastName = view.findViewById(R.id.last_name);
        EditText email = view.findViewById(R.id.email);
        RadioButton male = view.findViewById(R.id.maleButton);
        RadioButton female = view.findViewById(R.id.femaleButton);

        // Determine if male or female
        String gender = "";
        if(male.isChecked()){
            gender = "m";
        } else if (female.isChecked()){
            gender = "f";
        }

        // Login button listener
        loginButton.setOnClickListener(v -> {
            try {
                LoginRequest loginRequest = new LoginRequest(username.getText().toString(),
                        password.getText().toString());
                Context context = this.getContext();
                // Create LoginTask
                Handler uiThreadMessageHandler = new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        String resultMessage = bundle.getString(LOGIN_RESULT_KEY);
                        Toast loginToast = Toast.makeText(context, resultMessage, Toast.LENGTH_LONG);
                        loginToast.show();
                    }
                };

                LoginTask loginTask = new LoginTask(uiThreadMessageHandler, loginRequest);
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(loginTask);
            } catch (Exception ex){
                ex.printStackTrace();
            }

//            if(listener != null){
//                listener.notifyDone();
//            }
        });

        // Register button listener
        registerButton.setOnClickListener(v -> {
            // Register User
            if(listener != null){
                listener.notifyDone();
            }
        });

        return view;
    }

    private static class LoginTask implements Runnable {

        private final Handler messageHandler;

        private final LoginRequest loginRequest;

        public LoginTask (Handler messageHandler, LoginRequest loginRequest){
            this.loginRequest = loginRequest;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy();
            //System.out.println("login with server proxy");
            sendMessage(proxy.login(loginRequest));
        }

        private void sendMessage(LoginResult result){
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putString(LOGIN_RESULT_KEY, result.getMessage());
            message.setData(messageBundle);
            messageHandler.sendMessage(message);
        }
    }
}