package com.example.android.releviumfinal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AgentActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AgentFragment())
                .commit();
    }
}
