package com.example.familymapclient;

import android.location.GnssAntennaInfo;
import android.os.Bundle;

import androidx.core.app.RemoteInput;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class LoginFragment extends Fragment {

    private Listener listener;

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

        loginButton.setOnClickListener(v -> {
            if(listener != null){
                listener.notifyDone();
            }
        });

        return view;
    }
}