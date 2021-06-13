package com.example.mobileoffloading;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * @author Luis Claramunt
 * Lobby where the users input their username and join the Mobile Offloading server
 */
public class Login extends AppCompatActivity {
    public double lat, lon;
    public String username = null;
    public float battery;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context ctx, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            battery = level * 100 / (float) scale;
        }
    };
    private EditText textUsername;
    private FusedLocationProviderClient fusedLocation;
    private Socket socket;
    private boolean serverConn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        textUsername = findViewById(R.id.textUsername);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        fusedLocation = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        getLocation();
        Server server = (Server) getApplication();
        socket = server.getSocket();
//        socket.on(Socket.EVENT_CONNECT, connEmitter);
        socket.on(Socket.EVENT_CONNECT, connEmitter);
        socket.connect();
    }

    /**
     * OnClick listener to join the server
     * @param view - for the button
     */
    public void joinLobby(View view) {
        username = textUsername.getText().toString().trim();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("battery", battery);
            jsonObject.put("latitude", lat);
            jsonObject.put("longitude", lon);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("login", jsonObject);
        startActivity(new Intent(getApplicationContext(), Lobby.class));
    }

    /**
     * Get user's location (latitude and longitude)
     */
    private void getLocation() {
        checkLocationPerm();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationEnabled()) {
                fusedLocation.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null) {
                        updateLocation();
                    } else {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Please turn on your location", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }
    }

    private void checkLocationPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        }
    }

    private boolean locationEnabled(){
        LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return  locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setFastestInterval(0);
        request.setInterval(5);
        fusedLocation = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocation.requestLocationUpdates(request, locCallback, Looper.myLooper());
    }

    private LocationCallback locCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult results) {
            Location loc = results.getLastLocation();
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }
    };

    private final Emitter.Listener connEmitter = args -> runOnUiThread(new Runnable() {
        @Override
        public void run() {
            if(!serverConn) {
                username = textUsername.getText().toString().trim();
                if (!username.isEmpty()) {
                    socket.emit("joinLobby", username);
                } else {
                    serverConn = true;
                }
            }
        }
    });




}