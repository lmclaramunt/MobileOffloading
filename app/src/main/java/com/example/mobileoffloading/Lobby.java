package com.example.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * @author Luis Claramunt
 * Lobby where the user waits for everyone to join
 * They can see the other users who have joined Mobile Offloading
 * When someone starts the task, the role of Admin will be assigned to one user
 */
public class Lobby extends AppCompatActivity {
    private ArrayList<User> userList;
    private UserRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        setTitle("Lobby");
        userList = new ArrayList<>();
        initRecyclerView();
    }

    /**
     * Initialize the RecyclerView that displays the User's
     */
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_view_users);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new UserRecyclerViewAdapter(userList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}