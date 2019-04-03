package com.example.android.releviumfinal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MapFragment())
                .commit();
    }
}
