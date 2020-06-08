package com.example.messengerclonejava.models;

import android.app.Application;
import android.util.Log;

import com.example.messengerclonejava.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversationInfo implements Serializable {
    public String conversation_name;

    public String conversation_id;
    public Date created_on;
    public int member_count;
    public int message_count;
    public String last_message;
    public Date last_message_sent_on;
    public UserInfo last_message_sender;
    public List<UserInfo> members;



    public ConversationInfo(String conversation_name, String conversation_id, Date created_on, int member_count, int message_count, String last_message, Date last_message_sent_on, UserInfo last_message_sender, List<UserInfo> members) {
        this.conversation_name = conversation_name;

        this.conversation_id = conversation_id;
        this.created_on = created_on;
        this.member_count = member_count;
        this.message_count = message_count;
        this.last_message = last_message;
        this.last_message_sent_on = last_message_sent_on;
        this.last_message_sender = last_message_sender;
        this.members = members;
    }

    public static ConversationInfo JsonToConversationInfo(JSONObject infoJson, UserInfo loggedUser) {
        try {

            String id = infoJson.getString("id");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date created_on = dateFormat.parse(infoJson.getString("created_on"));
            int member_count = infoJson.getInt("member_count");
            int message_count = infoJson.getInt("message_count");
            String last_message = infoJson.getString("last_message");
            Date last_message_sent_on;
            UserInfo last_message_sender;
            if(message_count > 0){
                last_message_sent_on = dateFormat.parse(infoJson.getString("last_message_sent_on"));
                last_message_sender = UserInfo.JsonToUserInfo(infoJson.getJSONObject("last_message_sender"));
            }
            else{
                last_message_sent_on = null;
                last_message_sender = null;
            }
            JSONArray membersJson = infoJson.getJSONArray("members");

            List<UserInfo> member_list = new ArrayList<UserInfo>();
            for (int i = 0; i < membersJson.length(); i++) {
                UserInfo member = UserInfo.JsonToUserInfo(membersJson.getJSONObject(i));
                member_list.add(member);
            }
            //setting conversation name, currently only working for conversations with 2 members,
            //basically sets conversation name to user's that is different than logged user.
            UserInfo user = getFirstDifferentUser(member_list,loggedUser);
            String conversation_name = "Something went wrong getting conversation name";
            if(user != null){
                conversation_name = user.username;
            }

            return new ConversationInfo(conversation_name,id,created_on,member_count,message_count,last_message,last_message_sent_on,last_message_sender,member_list);

        }
        catch(JSONException ex){
                Log.e("ConversationInfo", "unexpected JSON exception", ex);
        }
        catch (Exception e){
        }
        return null;
    }
    private static UserInfo getFirstDifferentUser(List<UserInfo> users, UserInfo user){

        if(users.size()> 1 && users.get(0).id == user.id){
            return users.get(1);
        }
        return users.get(0);
    }

}

