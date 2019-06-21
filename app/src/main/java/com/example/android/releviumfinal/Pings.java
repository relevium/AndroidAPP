package com.example.android.releviumfinal;


import java.util.UUID;

public class Pings {
    private String mDescription, mUserID;
    private int mImageId;

    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public String getmUserID() {
        return mUserID;
    }

    public Pings(){

    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public void setmImageId(int mImageId) {
        this.mImageId = mImageId;
    }


    public String getmDescription() {
        return mDescription;
    }

    public int getmImageId() {
        return mImageId;
    }

}
