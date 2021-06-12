package com.example.mobileoffloading;

/**
 * @author Luis Claramunt
 * Class used to handle users' data
 */
public class User {
    private String username;
    private int battery;
    private float latitude, longitude;

    public User(String username, int battery, float latitude, float longitude){
        this.username=username;
        this.battery=battery;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
