package com.example.android.releviumfinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Initialize xml intractable variables
    private Button loginBtn;
    private Button registerBtn;
    private Button googleBtn;
    private Button twitterBtn;
    private Button facebooknBtn;
    private ProgressDialog loadingBar;

    private EditText userNameEdt;
    private EditText passwordEdt;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);
            finish();
        }
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            setContentView(R.layout.activity_login);
        } else {

            setContentView(R.layout.activity_login_m);
        }

        FirebaseApp.initializeApp(this);
        getSupportActionBar().hide();

        //get the resource id from xml file
        loginBtn = (Button) findViewById(R.id.login);
        registerBtn = (Button) findViewById(R.id.register);

        userNameEdt = (EditText) findViewById(R.id.username);
        passwordEdt = (EditText) findViewById(R.id.password);

        loadingBar = new ProgressDialog(LoginActivity.this);

        //Set onClick listener for each button
        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    private void loginUser() {

        final String userName = userNameEdt.getText().toString().trim();
        final String password = passwordEdt.getText().toString().trim();
        loadingBar.setTitle("Logging-in");
        loadingBar.setMessage("Logging you in, this may take a moment.");
        loadingBar.setCanceledOnTouchOutside(false);
        Log.v("TEST", !(userName.isEmpty() && password.isEmpty())+" And check");
        Log.v("TEST", !(userName.isEmpty() || password.isEmpty())+" Or check");
        if (!(userName.isEmpty() && password.isEmpty()) && !(userName.isEmpty() || password.isEmpty())) {
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(userName, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                loadingBar.dismiss();
                                // Start the new activity
                                startActivity(mainActivity);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Incorrect user name or password.",
                                        Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                            // ...
                        }
                    });
        }
        else if(userName.isEmpty() && password.isEmpty()){
            Toast.makeText(this, "You must enter a username and a password", Toast.LENGTH_SHORT).show();
        }
        else if(userName.isEmpty()){
            Toast.makeText(this, "You must enter a username", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "You must enter a password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                loginUser();
                break;
            case R.id.register:
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity);
                break;

        }
    }
}

