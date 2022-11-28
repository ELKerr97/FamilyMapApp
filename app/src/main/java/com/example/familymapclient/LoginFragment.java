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

import model.Person;
import model.User;
import request.EventRequest;
import request.LoginRequest;
import request.PersonRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;


public class LoginFragment extends Fragment {

    private Listener listener;
    private static final String LOGIN_RESULT_KEY = "LoginResultKey";
    private static final String LOGIN_RESULT_FIRSTNAME_KEY = "LoginResultFirstNameKey";
    private static final String LOGIN_RESULT_LASTNAME_KEY = "LoginResultLastNameKey";
    private static final String REGISTER_RESULT_KEY = "RegisterResultKey";
    private final DataCache dataCache;

    public interface Listener {
        void notifyDone();
    }

    public LoginFragment() {
        this.dataCache = DataCache.getInstance();
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

        Context context = this.getContext();

        // Login button listener
        loginButton.setOnClickListener(v -> {
            try {
                // Check for invalid input
                if(username.getText().toString().equals("") ||
                        password.getText().toString().equals("")){

                    Toast invalidToast = Toast.makeText(context, "Error: Please enter a valid" +
                            " username and password.", Toast.LENGTH_LONG);
                    invalidToast.show();

                } else if (serverPort.getText().toString().equals("") ||
                        serverHost.getText().toString().equals("")) {

                    Toast invalidToast = Toast.makeText(context, "Error: Please enter a valid" +
                            " server host or server port.", Toast.LENGTH_LONG);
                    invalidToast.show();

                } else {
                    dataCache.setServerPort(Integer.parseInt(serverPort.getText().toString()));
                    dataCache.setServerHost(serverHost.getText().toString());

                    LoginRequest loginRequest = new LoginRequest(username.getText().toString(),
                            password.getText().toString());

                    // Create LoginTask
                    Handler uiThreadMessageHandler = new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            Toast loginToast;
                            Bundle bundle = message.getData();
                            String resultMessage = bundle.getString(LOGIN_RESULT_KEY);
                            // If there are no errors, move on to map fragment.
                            if(listener != null && !resultMessage.contains("Error")){
                                String firstName = bundle.getString(LOGIN_RESULT_FIRSTNAME_KEY);
                                String lastName = bundle.getString(LOGIN_RESULT_LASTNAME_KEY);
                                loginToast = Toast.makeText(
                                        context,
                                        "Successfully logged in " + firstName + " " + lastName,
                                        Toast.LENGTH_LONG);
                                loginToast.show();
                                listener.notifyDone();
                            } else {
                                loginToast = Toast.makeText(context, resultMessage, Toast.LENGTH_LONG);
                                loginToast.show();
                            }
                        }
                    };

                    // Create new LoginTask to run in the background
                    LoginTask loginTask = new LoginTask(uiThreadMessageHandler, loginRequest);
                    // Execute background task
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(loginTask);
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }


        });

        // Register button listener
        registerButton.setOnClickListener(v -> {
            try {
                // Determine if male or female
                String gender = "";
                if(male.isChecked()){
                    gender = "m";
                } else if (female.isChecked()){
                    gender = "f";
                }

                // Check for form errors
                if(serverPort.getText().toString().equals("") ||
                        serverHost.getText().toString().equals("")){
                    Toast invalidToast = Toast.makeText(context, "Error: Please enter a valid" +
                            " server host or server port.", Toast.LENGTH_LONG);
                    invalidToast.show();
                } else if (username.getText().toString().equals("") ||
                        password.getText().toString().equals("")){

                    Toast invalidToast = Toast.makeText(context, "Error: Please enter a valid" +
                            " username and password.", Toast.LENGTH_LONG);
                    invalidToast.show();

                } else if (firstName.getText().toString().equals("") ||
                            lastName.getText().toString().equals("") ||
                            email.getText().toString().equals("") ||
                            gender.equals("")){

                    Toast invalidToast = Toast.makeText(context, "Error: Please enter a valid" +
                            " username and password.", Toast.LENGTH_LONG);
                    invalidToast.show();

                } else {
                    dataCache.setServerPort(Integer.parseInt(serverPort.getText().toString()));
                    dataCache.setServerHost(serverHost.getText().toString());

                    RegisterRequest registerRequest = new RegisterRequest(
                            username.getText().toString(),
                            password.getText().toString(),
                            email.getText().toString(),
                            firstName.getText().toString(),
                            lastName.getText().toString(),
                            gender
                            );

                    Handler uiThreadMessageHandler = new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            Bundle bundle = message.getData();
                            String resultMessage = bundle.getString(REGISTER_RESULT_KEY);
                            Toast registerToast = Toast.makeText(context, resultMessage, Toast.LENGTH_LONG);
                            registerToast.show();
                            // If there are no errors, move on to map fragment.
//                            if(listener != null && !resultMessage.contains("Error")){
//                                listener.notifyDone();
//                            }
                        }
                    };

                    // Create new LoginTask to run in the background
                    RegisterTask registerTask = new RegisterTask(uiThreadMessageHandler, registerRequest);
                    // Execute background task
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(registerTask);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
            LoginResult loginResult = proxy.login(loginRequest);
            sendMessage(loginResult);
        }

        private void sendMessage(LoginResult result){
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            DataCache dataCache = DataCache.getInstance();
            if(result.isSuccess()){
                Person userPerson = dataCache.getPerson(result.getPersonID());
                messageBundle.putString(LOGIN_RESULT_FIRSTNAME_KEY, userPerson.getFirstName());
                messageBundle.putString(LOGIN_RESULT_LASTNAME_KEY, userPerson.getLastName());
                dataCache.setUserPersonID(userPerson.getPersonID());
            }
            messageBundle.putString(LOGIN_RESULT_KEY, result.getMessage());
            message.setData(messageBundle);
            messageHandler.sendMessage(message);
        }
    }

    private static class RegisterTask implements Runnable {

        private final Handler messageHandler;
        private final RegisterRequest registerRequest;

        public RegisterTask (Handler messageHandler, RegisterRequest registerRequest){
            this.messageHandler = messageHandler;
            this.registerRequest = registerRequest;
        }

        @Override
        public void run() {
            ServerProxy proxy = new ServerProxy();
            sendMessage(proxy.register(registerRequest));
        }

        private void sendMessage(RegisterResult result){
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putString(REGISTER_RESULT_KEY, result.getMessage());
            message.setData(messageBundle);
            messageHandler.sendMessage(message);
        }

    }
}