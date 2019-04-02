package com.example.android.releviumfinal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {


    private Button registerBtn;
    private EditText firstnameEdt;
    private EditText lastnameEdt;
    private EditText usernameEdt;
    private EditText emailEdt;
    private EditText passwordEdt;
    private EditText repasswordEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        registerBtn = (Button) findViewById(R.id.submit);

        firstnameEdt = (EditText) findViewById(R.id.firstName_register);
        lastnameEdt = (EditText) findViewById(R.id.lastName_register);
        usernameEdt = (EditText) findViewById(R.id.username_register);
        emailEdt = (EditText) findViewById(R.id.email_register);
        passwordEdt = (EditText) findViewById(R.id.password_register);
        repasswordEdt = (EditText) findViewById(R.id.re_password_register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the submit button is clicked on.
            @Override
            public void onClick(View view) {

            }
        });
    }
}
