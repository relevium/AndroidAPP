package com.example.android.releviumfinal;

public class User {
    public String mFirstName, mLastName, mEmail, BloodType;
    public Boolean AnonymityPreference, ContagiousDisease;

    public User(){
        BloodType = "Not-Specified";
        AnonymityPreference = false;
        ContagiousDisease = false;
    }

    public User(String firstName, String lastName, String email) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mEmail = email;
        BloodType = "Not-Specified";
        ContagiousDisease = false;
        AnonymityPreference = false;
    }
}
