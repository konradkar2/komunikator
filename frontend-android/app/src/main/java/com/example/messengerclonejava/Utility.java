package com.example.messengerclonejava;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class Utility {
    public static Map<String, String> header_jwt(Application app) {
        Map<String, String> params = new HashMap<String, String>();
        String token = ((MyApplication)app).getAccessToken();
        params.put("Authorization", "Bearer " + token);
        return params;
    }
    public static Map<String, String> header_jwt(MyApplication myApp) {
        Map<String, String> params = new HashMap<String, String>();
        String token = myApp.getAccessToken();
        params.put("Authorization", "Bearer " + token);
        return params;
    }

    public static String getVolleyErrorMessage(VolleyError error) {
        try {

            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = "Something went wrong";
            if(data.has("message")){
                message = data.getString("message");
            }
            if(data.has("description")){
                message = data.getString("description");
            }

            return message;

        } catch (JSONException e) {
        } catch (UnsupportedEncodingException er) {
        }
        catch(NullPointerException err){

        }
        return "Unknown error";
    }


}
