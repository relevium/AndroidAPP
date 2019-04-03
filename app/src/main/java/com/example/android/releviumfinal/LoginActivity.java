package com.example.android.releviumfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    //Initialize xml intractable variables
    private Button loginBtn;
    private Button registerBtn;
    private Button googleBtn;
    private Button twitterBtn;
    private Button facebooknBtn;

    private EditText userNameEdt;
    private EditText passwordEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //get the resource id from xml file
        loginBtn = (Button) findViewById(R.id.login);
        registerBtn = (Button) findViewById(R.id.register);
        googleBtn = (Button) findViewById(R.id.google);
        twitterBtn = (Button) findViewById(R.id.twitter);
        facebooknBtn = (Button) findViewById(R.id.facebook);

        userNameEdt = (EditText) findViewById(R.id.username);
        passwordEdt = (EditText) findViewById(R.id.password);

        //Set onClick listener for each button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Login-in button is clicked on.
            @Override
            public void onClick(View view) {
                if(userNameEdt.getText().toString().equals("admin") && passwordEdt.getText().toString().equals("admin")){
                    // Create a new intent to open the {@link NumbersActivity}
                    Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                    // Start the new activity
                    startActivity(mainActivity);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Wrong user name or password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Login-in button is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link RegisterActivity} and Start the new activity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}

