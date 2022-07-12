package com.example.memereact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memereact.daos.UserDao;
import com.example.memereact.models.Post;
import com.example.memereact.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class PostAdapter extends FirestoreRecyclerAdapter<Post,PostViewHolder>{

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    IPostAdapter listener;
    public PostAdapter(@NonNull FirestoreRecyclerOptions<Post> options, IPostAdapter listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
        holder.postText.setText(model.text);
        holder.userText.setText(model.createdBy.displayName);
        holder.createdAt.setText(Utils.getTimeAgo(model.createdAt));
//        holder.postLayout.setBackgroundResource(R.drawable.hap2);
        Glide.with(holder.userImage.getContext()).load(model.createdBy.imageURL).into(holder.userImage);
        Glide.with(holder.userImage.getContext()).load(model.imgAddr).into(holder.postImage);

        String currentUserId = FirebaseAuth.getInstance().getUid();
        Boolean isLiked = model.likedBy.contains(currentUserId);
        holder.likeCount.setText(Integer.toString(model.likedBy.size()));

        if(isLiked)
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.getContext(), R.drawable.thumb_up));
        else
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.getContext(), R.drawable.thumb_up_void));
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        PostViewHolder postViewHolder = new PostViewHolder(view);

        postViewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> list_of_likingUserID = getSnapshots().get(postViewHolder.getAdapterPosition()).likedBy;
                String signed_in_user_id = FirebaseAuth.getInstance().getUid();

                if(list_of_likingUserID.contains(signed_in_user_id))
                    Toast.makeText(parent.getContext(), "Like Removed", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(parent.getContext(), "Liked", Toast.LENGTH_SHORT).show();

                for(String i:list_of_likingUserID){
                    UserDao userDao = new UserDao();
                    Task<DocumentSnapshot> task = userDao.getUserByID(i);

                    task.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User cur_user = documentSnapshot.toObject(User.class);
                        }
                    });
                }
                listener.onLikeCLik(getSnapshots().getSnapshot(postViewHolder.getAdapterPosition()).getId());
            }
        });

        return postViewHolder;
    }
}

class PostViewHolder extends RecyclerView.ViewHolder {
    public TextView postText ;
    public TextView userText ;
    public TextView createdAt ;
    public TextView likeCount ;
    public ImageView userImage ;
    public ImageView likeButton ;
    public ImageView postImage ;
    public ConstraintLayout postLayout ;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
         postText = itemView.findViewById(R.id.postTitle);
         userText  = itemView.findViewById(R.id.userName);
         createdAt = itemView.findViewById(R.id.createdAt);
         likeCount = itemView.findViewById(R.id.likeCount);
         userImage = itemView.findViewById(R.id.userImage);
         likeButton = itemView.findViewById(R.id.likeButton);
         postImage = itemView.findViewById(R.id.postImage);
         postLayout = itemView.findViewById(R.id.post_CLayout);
    }
}

interface IPostAdapter
{
    void onLikeCLik(String postId);
}
