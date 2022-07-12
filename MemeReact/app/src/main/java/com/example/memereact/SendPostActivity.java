package com.example.memereact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.memereact.daos.PostDao;
import com.example.memereact.ml.Emojifier;
import com.example.memereact.ml.ModelNlp;
import com.example.memereact.ml.PreprocessedModelCnn;
import com.example.memereact.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.internal.JsonParser;


public class SendPostActivity extends AppCompatActivity {
    Button postbutton;
    Button chhosebtn;
    Button captionbtn;
    Button emjbtn;
    EditText editText;
    Bitmap bitmap;
    static ProgressBar progressBar;
    int w = 299;
    int h = 299;

    // view for image view
    private ImageView imageView;
    // Uri indicates, where the image will be picked from
    private Uri filePath;
    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_post);
        captionbtn = findViewById(R.id.cap_button);
        emjbtn = findViewById(R.id.emj_button);
        chhosebtn = findViewById(R.id.choosebutton);
        postbutton = findViewById(R.id.saveButton);
        editText = findViewById(R.id.editTxt);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar2);

        postbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filePath!=null){
                    String txt = editText.getText().toString();
                    if(txt.length()>0){
                        PostDao postDao = new PostDao();
                        String postID = postDao.addPost(txt);
                        uploadImage(postID);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"Please write something!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Choose image",Toast.LENGTH_SHORT).show();
                }
            }
        });

        chhosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
                editText.setText("");
            }
        });

        captionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filePath!=null) {
                    Toast.makeText(getApplicationContext(),"Please wait for some seconds",Toast.LENGTH_SHORT).show();
                    Bitmap img_ = Bitmap.createScaledBitmap(bitmap, w, h, true);
                    Context context = getApplicationContext();
                    TensorBuffer cNNresult_tb = getCNNresult(img_, context);
                    float[] sequence = new float[33];
                    Arrays.fill(sequence,0);
                    ArrayList<Float>ans=new ArrayList<>();

                    int start = 0;
                    StringTokenizer st = new StringTokenizer(editText.getText().toString()," ");
                    while (st.hasMoreTokens()) {
                        String s = st.nextToken();
                        try {
                            sequence[start] = Float.parseFloat(SingletonJSONS.getInstance(getApplicationContext()).w_to_i_jObj_small.get(s).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        start++;
                    }

                    for(int i = start;i<33;i++) {
                        int cur_best_ind = getSingleNLPResult(sequence, cNNresult_tb, context);
                        ans.add((float) cur_best_ind);
                        for(int j=0;j<ans.size();j++)
                            sequence[33-j-1]=ans.get(ans.size()-j-1);
                        if(cur_best_ind==13)
                            break;
                    }

                    String ans_str = "";
                    for(float x:ans) {
                        try {
                            ans_str+=SingletonJSONS.getInstance(getApplicationContext()).i_to_w_jObj_small.get(Integer.toString((int)x))+" ";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    editText.setText(editText.getText().toString()+" "+ans_str);
                    Toast.makeText(getApplicationContext(), "Auto Caption Completed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emjbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = editText.getText().toString();
                if(txt.length()>0){
                    int [] arr = getArrayFromSent(txt);
                    ArrayList<Integer> e_inds = getEMOJIResult(arr);
                    Toast.makeText(getApplicationContext(),getEmojiCodeFromInd(e_inds.get(0)),Toast.LENGTH_SHORT).show();
                    editText.setText(txt+getEmojiCodeFromInd(e_inds.get(0)));
                }else{
                    Toast.makeText(getApplicationContext(),"Please write something!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getEmoji(int uni)
    {
        return new String(Character.toChars(uni));
    }

    private String getEmojiFromInd(int e_ind) {
        if(e_ind==0)
            return "Heart";
        else if(e_ind==1)
            return "Pro";
        else if(e_ind==2)
            return "Happy";
        else if(e_ind==3)
            return "Sad";
        else
            return "Foody";
    }

    private String getEmojiCodeFromInd(int e_ind) {
        if(e_ind==0)
            return getEmoji(0x1f970);
        else if(e_ind==1)
            return getEmoji(0x1f60E);
        else if(e_ind==2)
            return getEmoji(0x1f600);
        else if(e_ind==3)
            return getEmoji(0x1f61E);
        else
            return getEmoji(0x1f924);
    }

    private ArrayList<Integer> getEMOJIResult(int[] inpArr) {
        Context context = getApplicationContext();
        try {
            Emojifier model = Emojifier.newInstance(context);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10  * 4);
            byteBuffer.order(ByteOrder.nativeOrder()); // new line added

            for (int i = 0;i<10;i++) {
                System.out.println("...../"+inpArr[i]);
                byteBuffer.putFloat((float)inpArr[i]);
            }

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Emojifier.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] probs = outputFeature0.getFloatArray();

            model.close();
            return getSortIndexes(probs);
        } catch (IOException e) {
            // TODO Handle the exception
            return null;
        }
    }

    private int [] getArrayFromSent(String txt) {
        int [] arr = new int[10];
        Arrays.fill(arr,0);
        StringTokenizer st = new StringTokenizer(txt," ");
        int count = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            try {
                String val =  SingletonJSONS.getInstance(getApplicationContext()).w_to_i_jObj.get(s).toString();
                arr[count] = Integer.parseInt(val);
                count++;
                if (count == 10)
                    break;
            }catch (Exception e){
                System.out.println("..,,,,,,,,,,,,,NOT FOOOOUND"+e);
            }
        }
        return arr;
    }

    private int getSingleNLPResult(float []prevSeq, TensorBuffer cnnResult_tb, Context context)
    {
        try {
            ModelNlp model = ModelNlp.newInstance(context);
            ByteBuffer byteBuffer0 = ByteBuffer.allocateDirect(33 * 1 * 4);
            byteBuffer0.order(ByteOrder.nativeOrder()); // new line added
            for (int i = 0;i<33;i++)
                byteBuffer0.putFloat(prevSeq[i]);
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 33}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer0);
            TensorBuffer inputFeature1 = cnnResult_tb;
            ModelNlp.Outputs outputs = model.process(inputFeature0, inputFeature1);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] floatArray = outputFeature0.getFloatArray();
            int ind = getBest(floatArray);
            model.close();
            return ind;
        } catch (IOException e) {
            // TODO Handle the exception
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    private TensorBuffer getCNNresult(Bitmap img_, Context context)
    {
        try {
            PreprocessedModelCnn model = PreprocessedModelCnn.newInstance(context);
            TensorImage tensorImage = new TensorImage(DataType.UINT8);
            tensorImage.load(img_);
            ByteBuffer byteBuffer = tensorImage.getBuffer();
            byteBuffer.order(ByteOrder.nativeOrder()); // new line added
            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 299, 299, 3}, DataType.UINT8);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            PreprocessedModelCnn.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            // Releases model resources if no longer used.
            model.close();
            return outputFeature0;
        } catch (IOException e) {
            // TODO Handle the exception
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);
                captionbtn.setVisibility(View.VISIBLE);
                emjbtn.setVisibility(View.VISIBLE);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void uploadImage(String postid)
    {
        if (filePath != null) {
            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + postid);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String url = uri.toString();
                                            PostDao postDao = new PostDao();
                                            Task<DocumentSnapshot> snapshotTask = postDao.getPostByID(postid);
                                            snapshotTask.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    Post cur_post = documentSnapshot.toObject(Post.class);
                                                    cur_post.imgAddr=url;
                                                    cur_post.sentiments = getEMOJIResult(getArrayFromSent(editText.getText().toString()));

                                                    System.out.println(editText.getText().toString()+cur_post.sentiments);
                                                    postDao.posts.document(postid).set(cur_post);
                                                    Toast.makeText(SendPostActivity.this,"Post created successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            Toast
                                    .makeText(SendPostActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }


    }

    private int getBest(@NonNull float[] probs)
    {
        int indx = 0;
        double prob = 0.0;
        for(int i=0;i<probs.length;i++){
            if(prob<probs[i]){
                indx = i;
                prob = probs[i];
            }
        }
        return indx;
    }

    private ArrayList<Integer> getSortIndexes(@NonNull float[] probs) {
        int n = probs.length;
        HashMap<Float, Integer> inds = new HashMap<>();
        for(int i = 0 ; i<n;i++)
            inds.put(probs[i],i);
        Arrays.sort(probs);
        ArrayList<Integer> inds_arr = new ArrayList<Integer>();
        for(int i = 0; i<n; i++)
            inds_arr.add(inds.get(probs[i]));
        Collections.reverse(inds_arr);
        return inds_arr;
    }
}