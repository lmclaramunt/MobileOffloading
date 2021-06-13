package com.example.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Luis Claramunt
 * Lobby where the user waits for everyone to join
 * They can see the other users who have joined Mobile Offloading
 * When someone starts the task, the role of Admin will be assigned to one user
 */
public class Lobby extends AppCompatActivity {
    private ArrayList<User> userList;
    private UserRecyclerViewAdapter adapter;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setTitle("Lobby");
        userList = new ArrayList<>();
        initRecyclerView();
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("userAdded", onUserAdded);
        socket.on("lobbyUsers", onLobbyUsers);
        socket.emit("joinLobby", "");
    }

    /**
     * Initialize the RecyclerView that displays the User's
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.users_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new UserRecyclerViewAdapter(userList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private Emitter.Listener onUserAdded = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String username;
        int battery;
        float lat, lon;
        try {
            username = data.getString("username");
            battery = data.getInt("battery");
            lat = data.getLong("latitude");
            lon = data.getLong("longitude");
        } catch (JSONException e) {
            return;
        }
        User newUser = new User(username, battery, lat, lon);
        adapter.addUser(newUser);
    });

    private Emitter.Listener onLobbyUsers = args -> runOnUiThread(() -> {
        JSONArray array = (JSONArray) args[0];
        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject data = (JSONObject) array.get(i);
                String username;
                int battery;
                float lat, lon;
                username = data.getString("username");
                battery = data.getInt("battery");
                lat = data.getLong("latitude");
                lon = data.getLong("longitude");
                User newUser = new User(username, battery, lat, lon);
                adapter.addUser(newUser);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    });
}