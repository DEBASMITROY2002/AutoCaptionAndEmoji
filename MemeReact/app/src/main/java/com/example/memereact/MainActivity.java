package com.example.memereact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.memereact.daos.PostDao;
import com.example.memereact.models.Post;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IPostAdapter{
    private FloatingActionButton addButton;
    private PostDao postDao;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addButton = findViewById(R.id.addButton);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent intent = new Intent(context, SendPostActivity.class);
                startActivity(intent);
            }
        });

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        postDao = new PostDao();
        CollectionReference posts = postDao.posts;

        Query query = posts.orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.rView);
        recyclerView.setLayoutManager(linearLayoutManager);
        postAdapter = new PostAdapter(options, this);
        System.out.println("-------- Adapter init "+postAdapter);
        recyclerView.setAdapter(postAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("-------- ADapter listens"+postAdapter);
        postAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        postAdapter.stopListening();
    }

    @Override
    public void onLikeCLik(String postId) {
        System.out.println("IDDDDDDD_>>>>>>>>>>>>"+postId);
        postDao.updateLikes(postId);
    }
}