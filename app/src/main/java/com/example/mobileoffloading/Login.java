package com.example.mobileoffloading;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import com.example.mobileoffloading.Utils.Server;
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
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Activity where the users inputs his/her username and join the Mobile Offloading server
 */
public class Login extends AppCompatActivity {
    public static final String BATTERY = "battery";
    public static final String USERNAME = "username";
    public double lat, lon;
    public String username = null;
    private float battery;
    private EditText textUsername;
    private FusedLocationProviderClient fusedLocation;
    private Socket socket;
    private boolean processingRequest = false;      // Wait for server results before sending next request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        textUsername = findViewById(R.id.textUsername);
        fusedLocation = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        Server server = (Server) getApplication();
        socket = server.getSocket();
        socket.on("login results", onLoginResults);
        socket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("login results", onLoginResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocation();          //In case the location has changed
    }

    /**
     * OnClick listener to join the server
     * @param view - for the button
     */
    public void joinLobby(View view) {
        username = textUsername.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(Login.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!processingRequest) {
            battery = (float) getBattery(getApplicationContext());
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
            processingRequest = true;
        }
    }

    /**
     * Get battery percentage from device, will be useful when the user joins the lobby
     * @param context - Context
     * @return - int, device's battery percentage
     */
    private int getBattery(Context context) {
        BatteryManager batteryMan = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        return batteryMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
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

    /**
     * Checks current location permissions, and asks for them if disabled
     */
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

    private final LocationCallback locCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult results) {
            Location loc = results.getLastLocation();
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }
    };

    /**
     * Tells if the user was able to login and join the lobby
     */
    private final Emitter.Listener onLoginResults = args -> runOnUiThread(() -> {
        try {
            JSONObject json = (JSONObject) args[0];
            boolean results = json.getBoolean("results");
            if(results) {
                Intent intent = new Intent(getApplicationContext(), Lobby.class);
                intent.putExtra(BATTERY, battery);
                intent.putExtra(USERNAME, username);
                startActivity(intent);
            }else{
                Toast.makeText(Login.this, "Sorry, username '" + username + "' is not available",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        processingRequest = false;
    });
}