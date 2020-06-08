package com.example.messengerclonejava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Invite extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        //toolbar
        ImageView imageView_back = (ImageView)findViewById(R.id.invite_image_back);
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //list
        ListView listView = (ListView) findViewById(R.id.invite_listView_searchResult);

        final List<UserInfo> search_results = new ArrayList<UserInfo>();

        final InvitesAdapter adapter = new InvitesAdapter(this,search_results);

        listView.setAdapter(adapter);

        EditText searchEditText = (EditText)findViewById(R.id.invite_editText_search);


        //search for user listeners
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String search_string = s.toString();
                if(search_string.length() < 4){
                    return;
                }
                search_results.clear();
                adapter.notifyDataSetChanged();
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String url = BuildConfig.API_URL + "/search/" + search_string;


                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{

                                Log.i("invites",response);
                                JSONObject jsonObj = new JSONObject(response);
                                JSONArray c = jsonObj.getJSONArray("users");
                                for (int i = 0 ; i < c.length(); i++) {
                                    JSONObject obj = c.getJSONObject(i);
                                    UserInfo userInfo = UserInfo.JsonToUserInfo(obj);
                                    if(!search_results.contains(userInfo)) {
                                        search_results.add(userInfo);
                                    }

                                }
                                adapter.notifyDataSetChanged();

                            }
                            catch (JSONException e){
                                Log.e("invites", "unexpected JSON exception", e);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                            Toast.makeText(getApplicationContext(),"Something went wrong: " + Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();


                            //todo: on 401
                            logout();

                        }
                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return Utility.header_jwt(getApplication());
                        }
                    };

                    requestQueue.add(stringRequest);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void logout(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }


    class InvitesAdapter extends ArrayAdapter<UserInfo> {
        Context context;
        List<UserInfo> userInfo;

        InvitesAdapter(Context c, List<UserInfo> userInfo_){
            super(c,R.layout.activity_invite_item, userInfo_);
            context = c;
            userInfo = userInfo_;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = layoutInflater.inflate(R.layout.activity_invite_item,parent,false);


            ImageView imageView = itemView.findViewById(R.id.invite_image);
            TextView usernameView = itemView.findViewById(R.id.invite_text_username);
            TextView aboutView = itemView.findViewById(R.id.invite_text_about);

            //todo: set image resource
            usernameView.setText(userInfo.get(position).username);
            aboutView.setText(userInfo.get(position).about);

            //setting button listener
            final Button button = itemView.findViewById(R.id.invite_button_invite);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setText("Sending...");
                    button.setEnabled(false);

                    int receiverId = userInfo.get(position).id;
                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    String url = BuildConfig.API_URL + "/invite/" + Integer.toString(receiverId);


                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            button.setText("Sent");
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(),"Something went wrong: " + Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();
                        }
                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return Utility.header_jwt(getApplication());
                        }
                    };

                    requestQueue.add(stringRequest);

                }
            });



            return itemView;
        }
    }


}
