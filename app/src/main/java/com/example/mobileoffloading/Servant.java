package com.example.mobileoffloading;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Servant extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servant);
        String id = getIntent().getStringExtra("id");
        TextView tx = findViewById(R.id.textView5);
        tx.setText("Hello Servant " + id);
    }
}