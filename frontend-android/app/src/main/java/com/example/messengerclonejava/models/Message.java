package com.example.messengerclonejava.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    public String message_id;
    public Date sent_on;
    public String content;
    public UserInfo sender;

    public Message(String message_id, Date sent_on, String content, UserInfo sender) {
        this.message_id = message_id;
        this.sent_on = sent_on;
        this.content = content;
        this.sender = sender;
    }
    public static Message JsonToMessageInfo(JSONObject obj){
        try{
            String message_id = obj.getString("message_id");
            String sent_on_string = obj.getString("sent_on");
            String content = obj.getString("content");
            if(obj.isNull("content")){
                content = null;
            }
            UserInfo sender = UserInfo.JsonToUserInfo(obj.getJSONObject("sender"));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date sent_on_date = dateFormat.parse(sent_on_string);


            return new Message(message_id,sent_on_date,content,sender);
        }
        catch (JSONException e){
            Log.e("JsonToMessageInfo", "unexpected JSON exception", e);
        }
        catch(Exception e){

        }
        return null;
    }
}
