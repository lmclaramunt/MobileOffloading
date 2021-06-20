package com.example.mobileoffloading;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Lobby where the user waits for everyone to join
 * They can see the other users who have joined Mobile Offloading
 * When someone starts the task, the role of Admin will be assigned to one user
 */
public class Lobby extends AppCompatActivity {
    private ArrayList<User> userList;
    private UserRecyclerViewAdapter adapter;
    private Socket socket;
    private float battery;
    private String username;
    private Button btnStart;
    //Used to constantly monitor device's battery
    private BroadcastReceiver batteryBroadcast = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context ctx, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float newBattery = level * 100 / (float) scale;
            if(newBattery != battery){
                battery = newBattery;
                socket.emit("battery change", battery);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setTitle("Lobby");
        battery = getIntent().getFloatExtra(Login.BATTERY, 0);
        username = getIntent().getStringExtra(Login.USERNAME);
        this.registerReceiver(this.batteryBroadcast, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        userList = new ArrayList<>();
        btnStart = findViewById(R.id.btnStart);
        initRecyclerView();
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("userAdded", onUserAdded);
        socket.on("lobbyUsers", onLobbyUsers);
        socket.on("update user", onUpdateUser);
        socket.emit("joinLobby", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.off("userAdded", onUserAdded);
        socket.off("lobbyUsers", onLobbyUsers);
        socket.off("update user", onUpdateUser);
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

    /**
     * A user has joined the lobby
     */
    private final Emitter.Listener onUserAdded = args -> runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            User newUser = processUserJson(data);
            if(newUser.isAdmin()){
                adapter.removeAdmin();      // So the UI is updated
            }
            adapter.addUser(newUser);
            updateAdminInterface();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * Get all user that have joined the lobby
     */
    private final Emitter.Listener onLobbyUsers = args -> runOnUiThread(() -> {
        JSONArray array = (JSONArray) args[0];
        for(int i = 0; i < array.length(); i++) {
            try {
                JSONObject data = (JSONObject) array.get(i);
                User newUser = processUserJson(data);
                adapter.addUser(newUser);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateAdminInterface();
    });

    private final Emitter.Listener onUpdateUser = args -> runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            String username = data.getString("username");
            float battery = (float) data.getDouble("battery");
            boolean admin = data.getBoolean("admin");
            if(admin){
                adapter.removeAdmin();      // So the UI is updated
            }
            adapter.updateBattery(username, battery, admin);
            updateAdminInterface();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    /**
     * Process JSON files that contain User's data
     * @param json - file with user's info
     * @return - new User Object
     * @throws JSONException - Exception
     */
    private User processUserJson(JSONObject json) throws JSONException {
        String username = json.getString("username");
        float battery = json.getInt("battery");
        float lat = json.getLong("latitude");
        float lon = json.getLong("longitude");
        boolean admin = json.getBoolean("admin");
        return new User(username, battery, lat, lon, admin);
    }

    /**
     * The button to start Mobile offloading will only be available to the admin
     */
    private void updateAdminInterface(){
        String admin = adapter.getAdminUsername();
        if(username.equals(admin)){
            btnStart.setVisibility(View.VISIBLE);
        }else{
            btnStart.setVisibility(View.INVISIBLE);
        }
    }
}