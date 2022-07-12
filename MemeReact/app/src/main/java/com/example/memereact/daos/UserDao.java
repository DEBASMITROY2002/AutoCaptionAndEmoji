package com.example.memereact.daos;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.memereact.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDao {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference users;
    String TAG = "---------------->";
    public UserDao(){
        users = db.collection("users");
    }

    public void addUser(User user){
        users.document(user.uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "DocumentSnapshot successfully written!");
            }
        });
    }

    public Task<DocumentSnapshot> getUserByID(String uid){
        return users.document(uid).get();
    }
}

