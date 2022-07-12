package com.example.memereact.models;

import java.util.ArrayList;

public class Post {
    public String text;
    public String imgAddr;
    public User createdBy;
    public Long createdAt;
    public ArrayList<String>likedBy;
    public ArrayList<Integer>sentiments;

    public Post(String text,String imgAddr, User user, Long createdAt, ArrayList<String>likedBy, ArrayList<Integer>sentiments){
        this.text = text;
        this.createdBy = user;
        this.createdAt  = createdAt;
        this.likedBy = likedBy;
        this.imgAddr = imgAddr;
        this.sentiments = sentiments;
    }

    public Post(String text, User user, Long createdAt){
        this.text = text;
        this.createdBy = user;
        this.createdAt  = createdAt;
        this.likedBy = new ArrayList<String>();
        this.imgAddr = "https://icones.pro/wp-content/uploads/2021/06/icone-chargement-jaune.png";
        this.sentiments = new ArrayList<>(5);
    }

    public Post(){
        this.text = "";
        this.createdBy = new User();
        this.createdAt  = 0L;
        this.likedBy = new ArrayList();
        this.imgAddr = "https://icones.pro/wp-content/uploads/2021/06/icone-chargement-jaune.png";
        this.sentiments = new ArrayList<>(5);
    }
}
