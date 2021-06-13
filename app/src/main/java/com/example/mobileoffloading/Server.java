package com.example.mobileoffloading;

import android.app.Application;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class Server extends Application {
    private Socket socket;
    {
        try {
            socket = IO.socket("http://192.168.1.11:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {return socket;}
}
