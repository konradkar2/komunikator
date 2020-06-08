package com.example.messengerclonejava.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserInfo implements Serializable {
    public String username;
    public int id;
    public String about;
    public String image_url;

    public UserInfo(String username, int id, String about, String image_url) {
        this.username = username;
        this.id = id;
        this.about = about;
        this.image_url = image_url;
    }

    public static UserInfo JsonToUserInfo(JSONObject obj){

        try{
            String username = obj.getString("username");
            int user_id = obj.getInt("id");
            String about = obj.getString("about");
            String image_url = obj.getString("image_url");

            return new UserInfo(username,user_id,about,image_url);
        }
        catch (JSONException e){
            Log.e("JsonToUserInfo", "unexpected JSON exception", e);
        }
        return null;
    }
}
