package com.example.messengerclonejava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView textViewSwitchToLogin = findViewById(R.id.register_textView_login);
        textViewSwitchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToLogin();
            }
        });
    }
    public void registerClicked(View view){
        EditText usernameEditText = findViewById(R.id.register_editText_username);
        EditText passwordEditText = findViewById(R.id.register_editText_password);
        EditText aboutEditText = findViewById(R.id.register_editText_about);

        String username = usernameEditText.getText().toString();
        String password =  passwordEditText.getText().toString();
        String about =  aboutEditText.getText().toString();


        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Username and password is required", Toast.LENGTH_SHORT).show();
        }
        else{
            register(username,password,about);
        }


    }
    private void register(String username, String password, String about){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = BuildConfig.API_URL + "/register";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("about", about);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getApplicationContext(), "registration successful", Toast.LENGTH_LONG).show();
                    switchToLogin();;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();
                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return requestBody.getBytes();
                }
            };

            requestQueue.add(stringRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void switchToLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
