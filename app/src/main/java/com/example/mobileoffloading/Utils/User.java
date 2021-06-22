package com.example.mobileoffloading.Utils;

/**
 * @author Luis Claramunt
 *         Daniel Evans
 *         Ting Xia
 *         Jianlun Li
 * Class used to handle users' data
 */
public class User {
    private String username;
    private float battery;
    private float latitude, longitude;
    private boolean admin;

    public User(String username, float battery, float latitude, float longitude, boolean admin){
        this.username=username;
        this.battery=battery;
        this.latitude=latitude;
        this.longitude=longitude;
        this.admin=admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
