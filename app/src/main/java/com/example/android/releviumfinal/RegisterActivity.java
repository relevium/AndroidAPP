package com.example.android.releviumfinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private Button registerBtn;
    private EditText firstnameEdt;
    private EditText lastnameEdt;
    private EditText emailEdt;
    private EditText passwordEdt;
    private EditText repasswordEdt;
    private String passwordRegx = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            setContentView(R.layout.activity_register);
        } else {

            setContentView(R.layout.activity_register_m);
        }

        FirebaseApp.initializeApp(this);

        registerBtn = (Button) findViewById(R.id.submit);

        firstnameEdt = (EditText) findViewById(R.id.firstName_register);
        lastnameEdt = (EditText) findViewById(R.id.lastName_register);
        emailEdt = (EditText) findViewById(R.id.email_register);
        passwordEdt = (EditText) findViewById(R.id.password_register);
        repasswordEdt = (EditText) findViewById(R.id.re_password_register);
        loadingBar = new ProgressDialog(RegisterActivity.this);

        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(this);
    }

    private void registerUser() {
        final String firstName = firstnameEdt.getText().toString().trim();
        final String lastName = lastnameEdt.getText().toString().trim();
        final String email = emailEdt.getText().toString().trim();
        final String password = passwordEdt.getText().toString().trim();
        final String repassword = repasswordEdt.getText().toString().trim();
        //final String phone = editTextPhone.getText().toString().trim();

        if (firstName.isEmpty()) {
            firstnameEdt.setError("first name required");
            firstnameEdt.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            lastnameEdt.setError("last name required");
            lastnameEdt.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEdt.setError("email required");
            emailEdt.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEdt.setError("invalid email address");
            emailEdt.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEdt.setError("fill password");
            passwordEdt.requestFocus();
            return;
        }

        if (!password.equals(repassword)) {
            repasswordEdt.setError("password mismatch");
            repasswordEdt.requestFocus();
            return;
        }
        if(!Pattern.compile(passwordRegx).matcher(password).matches()){
            Toast.makeText(this, "Password must be more than 8 letters with one upper letter, number and a symbol.", Toast.LENGTH_SHORT).show();
            return;
        }
        //progressBar.setVisibility(View.VISIBLE);
        loadingBar.setTitle("Registering");
        loadingBar.setMessage("Registering you in our servers, this may take a moment.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {



                        if (task.isSuccessful()) {

                            User user = new User(
                                    firstName,
                                    lastName,
                                    email
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_LONG).show();
                                        Intent loginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(loginActivity);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Please try again later", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                registerUser();
                break;
        }
    }
}
