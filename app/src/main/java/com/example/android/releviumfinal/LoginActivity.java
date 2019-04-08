package com.example.android.releviumfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    //Initialize xml intractable variables
    private Button loginBtn;
    private Button registerBtn;
    private Button googleBtn;
    private Button twitterBtn;
    private Button facebooknBtn;

    private EditText userNameEdt;
    private EditText passwordEdt;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        //get the resource id from xml file
        loginBtn = (Button) findViewById(R.id.login);
        registerBtn = (Button) findViewById(R.id.register);
        googleBtn = (Button) findViewById(R.id.google);
        twitterBtn = (Button) findViewById(R.id.twitter);
        facebooknBtn = (Button) findViewById(R.id.facebook);

        userNameEdt = (EditText) findViewById(R.id.username);
        passwordEdt = (EditText) findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        //Set onClick listener for each button
        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    private void loginUser(){

        final String userName = userNameEdt.getText().toString().trim();
        String password = passwordEdt.getText().toString().trim();
        if( !(userName.isEmpty() && password.isEmpty()) )
            mAuth.signInWithEmailAndPassword(userName, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                // Start the new activity
                                startActivity(mainActivity);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Incorrect user name or password.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                loginUser();
                break;
            case R.id.register:
                Intent mainActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(mainActivity);
                break;

        }
    }
}

