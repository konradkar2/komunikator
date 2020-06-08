package com.example.messengerclonejava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView textViewSwitchToRegister = findViewById(R.id.login_textView_register);
        textViewSwitchToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRegister();
            }
        });
    }

    public void loginClicked(View view){
        EditText usernameEditText = findViewById(R.id.login_editText_username);
        EditText passwordEditText = findViewById(R.id.login_editText_password);

        String username = usernameEditText.getText().toString();
        String password =  passwordEditText.getText().toString();


        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Username and password is required", Toast.LENGTH_SHORT).show();
        }
        else{
            login(username,password);
        }


    }
    private void login(final String username, final String password){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = BuildConfig.API_URL + "/auth";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject json = new JSONObject(response);
                        String token = json.get("access_token").toString();
                        UserInfo loggedUser = UserInfo.JsonToUserInfo(json.getJSONObject("user"));
                        MyApplication application =  ((MyApplication)getApplication());
                        application.setAccessToken(token);
                        application.setLoggedUser(loggedUser);
                        NotificationListener notificationListener = new NotificationListener(application);
                        application.setNotificationListener(notificationListener);

                    }
                    catch (JSONException e){
                        Log.e("login", "unexpected JSON exception", e);
                    }

                    switchToFriends();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("password", password);
                    return params;
                }
            };

            requestQueue.add(stringRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void switchToFriends(){
        Intent intent = new Intent(this, Navigation.class);
        startActivity(intent);
    }
    private void switchToRegister(){
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
