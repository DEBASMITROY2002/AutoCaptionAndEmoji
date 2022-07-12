package com.example.memereact.models;

public class User {
    public String uid;
    public String displayName;
    public String imageURL;

    public User(String uid, String displayName, String imageURL){
        this.displayName = displayName;
        this.uid = uid;
        this.imageURL = imageURL;
    }

    public User() {
        this.displayName = "";
        this.uid = "";
        this.imageURL = "";
    }
}
