package com.example.memereact;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;

public class SingletonJSONS extends AppCompatActivity {
    // Static variable reference of single_instance
    // of type Singleton
    private static SingletonJSONS single_instance = null;
    public static JSONObject w_to_i_jObj;
    public static JSONObject i_to_w_jObj;
    public static JSONObject w_to_i_jObj_small;
    public static JSONObject i_to_w_jObj_small;

    private SingletonJSONS(Context context)
    {
        try {
            InputStream w_to_i_json = context.getAssets().open("w_to_i.json");
//            InputStream i_to_w_json = context.getAssets().open("i_to_w.json");
            InputStream i_to_w_json_small = context.getAssets().open("i_to_w_small.json");
            InputStream w_to_i_json_small = context.getAssets().open("w_to_i_small.json");

            w_to_i_jObj = new JSONObject(Utils.loadJSONFromAsset(w_to_i_json));
//            i_to_w_jObj = new JSONObject(Utils.loadJSONFromAsset(i_to_w_json));
            i_to_w_jObj_small = new JSONObject(Utils.loadJSONFromAsset(i_to_w_json_small));
            w_to_i_jObj_small = new JSONObject(Utils.loadJSONFromAsset(w_to_i_json_small));
        }catch (Exception e){
            System.out.println("--------------------------------------------    JSON Error");
        }
    }

    public static SingletonJSONS getInstance(Context context)
    {
        if (single_instance == null)
            single_instance = new SingletonJSONS(context);
        return single_instance;
    }
}
