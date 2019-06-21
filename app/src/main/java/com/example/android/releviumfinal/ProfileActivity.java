package com.example.android.releviumfinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName, userLastName;
    private CircleImageView userProfileImage;
    private Spinner bloodSpinner;
    private Switch statusSwitch, diseasSwitch;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitializeFields();


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });


        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this);
            }
        });
    }


    private void InitializeFields() {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userLastName = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        bloodSpinner = findViewById(R.id.blood_spinner);
        statusSwitch = findViewById(R.id.online_switch);
        diseasSwitch = findViewById(R.id.disease_switch);
        loadingBar = new ProgressDialog(ProfileActivity.this);
        setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();


                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            Task<Uri> urlTask = task.getResult().getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            Uri downloadUri = urlTask.getResult();
                            final String downloadUrl = String.valueOf(downloadUri);


                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }


    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setLastName = userLastName.getText().toString();
        String setBloodType = bloodSpinner.getSelectedItem().toString();
        boolean setStatues = statusSwitch.isChecked();
        boolean setDisease = diseasSwitch.isChecked();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setLastName)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            if(!setUserName.equals("")){
                profileMap.put("mFirstName", setUserName);
            }
            else{
                Toast.makeText(ProfileActivity.this, "You Can't Have an empty first name.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!setLastName.equals("")){
                profileMap.put("mLastName", setLastName);
            }
            else{
                Toast.makeText(ProfileActivity.this, "You Can't Have an empty last name.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (setStatues) {
                profileMap.put("AnonymityPreference", true);
            } else {
                profileMap.put("AnonymityPreference", false);
            }
            if (setDisease) {
                profileMap.put("ContagiousDisease", true);
            } else {
                profileMap.put("ContagiousDisease", false);
            }
            profileMap.put("BloodType", setBloodType);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(ProfileActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(ProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("mFirstName") && (dataSnapshot.hasChild("image")))) {
                            String retrieveUserName = dataSnapshot.child("mFirstName").getValue().toString();
                            String retrievesLastName = dataSnapshot.child("mLastName").getValue().toString();
                            Boolean setStatues = dataSnapshot.child("AnonymityPreference").getValue(Boolean.class);
                            Boolean setDisease = dataSnapshot.child("ContagiousDisease").getValue(Boolean.class);
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userLastName.setText(retrievesLastName);

                            if(dataSnapshot.child("BloodType").getValue() != null){
                                String setBloodType = dataSnapshot.child("BloodType").getValue().toString();
                                bloodSpinner.setSelection(((ArrayAdapter) bloodSpinner.getAdapter()).getPosition(setBloodType));
                            }
                            if(setStatues != null && setDisease != null){
                                diseasSwitch.setChecked(setDisease);
                                statusSwitch.setChecked(setStatues);
                            }

                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("mFirstName"))) {
                            String retrieveUserName = dataSnapshot.child("mFirstName").getValue().toString();
                            String retrievesLastName = dataSnapshot.child("mLastName").getValue().toString();
                            Boolean setStatues = dataSnapshot.child("AnonymityPreference").getValue(Boolean.class);
                            Boolean setDisease = dataSnapshot.child("ContagiousDisease").getValue(Boolean.class);

                            userName.setText(retrieveUserName);
                            userLastName.setText(retrievesLastName);

                            if(dataSnapshot.child("BloodType").getValue() != null){
                                String setBloodType = dataSnapshot.child("BloodType").getValue().toString();
                                bloodSpinner.setSelection(((ArrayAdapter) bloodSpinner.getAdapter()).getPosition(setBloodType));
                            }

                            if(setStatues != null && setDisease != null){
                                diseasSwitch.setChecked(setDisease);
                                statusSwitch.setChecked(setStatues);
                            }
                        } else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(ProfileActivity.this, "Please update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
