package com.example.memereact.daos;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.memereact.models.Post;
import com.example.memereact.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class PostDao {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public CollectionReference posts;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    public PostDao(){
        posts = db.collection("posts");
    }

    public String addPost(String cur_text){
        String uid = mFirebaseAuth.getUid();
        long currentTimeMillis = System.currentTimeMillis();

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User cur_user = documentSnapshot.toObject(User.class);
                Post post = new Post(cur_text, cur_user, currentTimeMillis);
                System.out.println("=================>"+cur_user.displayName);

                posts.document(Long.toString(currentTimeMillis)).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("------_______>> DocumentSnapshot successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("------_______ >>> DocumentSnapshot Unsuccessful!");
                    }
                });

            }
        });
        return Long.toString(currentTimeMillis);
    }

    public Task<DocumentSnapshot> getPostByID(String pid){
        return posts.document(pid).get();
    }

    public void updateLikes(String postId) {
        String user_id = FirebaseAuth.getInstance().getUid();
        Task<DocumentSnapshot> postByIDTask = getPostByID(postId);
        postByIDTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Post cur_post = documentSnapshot.toObject(Post.class);
                boolean isLiked = cur_post.likedBy.contains(user_id);
                System.out.println("isLiked>>>>>>>>>>>>"+isLiked);
                if(!isLiked)
                    cur_post.likedBy.add(user_id);
                else
                    cur_post.likedBy.remove(user_id);
                posts.document(postId).set(cur_post);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Failed>>>>>>>>>>>>"+e);
            }
        });
    }
}
