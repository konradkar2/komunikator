package com.example.messengerclonejava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.messengerclonejava.models.ConversationInfo;
import com.example.messengerclonejava.models.Invitation;
import com.example.messengerclonejava.models.Message;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Chat extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        final ConversationInfo conversation_info = (ConversationInfo) getIntent().getSerializableExtra("conversation_info");
        TextView conversation_nameView = findViewById(R.id.chat_text_view_conversation_name);
        conversation_nameView.setText(conversation_info.conversation_name);


        final ListView listView = findViewById(R.id.chat_List);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        final List<Message> messages = new ArrayList<Message>();

        final ChatAdapter adapter = new ChatAdapter(this,messages,conversation_info);
        listView.setAdapter(adapter);
        ((MyApplication)getApplication()).setChatAdapter(adapter);

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            int last_message_id = 0;
            String url = BuildConfig.API_URL + "/message/" + conversation_info.conversation_id + "/" +Integer.toString(last_message_id);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{

                        JSONObject jsonObj = new JSONObject(response);
                        JSONArray c = jsonObj.getJSONArray("new_messages");
                        for (int i = 0 ; i < c.length(); i++) {
                            JSONObject obj = c.getJSONObject(i);
                            Message message = Message.JsonToMessageInfo(obj);
                            if(!messages.contains(message) && message.content != null) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        listView.setSelection(adapter.getCount()-1);

                    }
                    catch (JSONException e){
                        Log.e("invites", "unexpected JSON exception", e);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", Utility.getVolleyErrorMessage(error));
                    Toast.makeText(getApplicationContext(), Utility.getVolleyErrorMessage(error), Toast.LENGTH_LONG).show();

                    //todo: on 401

                }
            }) {

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


        //Sending new message
        ImageView sendMessageButton = findViewById(R.id.chat_button_send_message);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String url = BuildConfig.API_URL + "/message/" + conversation_info.conversation_id;

                    TextView newMessageView = findViewById(R.id.chat_text_view_new_message_content);
                    final String content = newMessageView.getText().toString();
                    newMessageView.setText("");
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", Utility.getVolleyErrorMessage(error));
                            Toast.makeText(getApplicationContext(), "Something went wrong: " + Utility.getVolleyErrorMessage(error), Toast.LENGTH_LONG).show();

                            //todo: on 401

                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return Utility.header_jwt(getApplication());
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<>();
                            params.put("content",content);
                            return params;
                        }
                    };
                    requestQueue.add(stringRequest);
                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        ImageView goBackImage = findViewById(R.id.chat_image_back);
        goBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }


    class ChatAdapter extends ArrayAdapter<Message> {
        Context context;
        List<Message> messages;
        ConversationInfo conversation_info;
        ChatAdapter(Context c, List<Message> messages,ConversationInfo conversation_info){
            super(c,R.layout.chat_item, messages);
            context = c;
            this.messages = messages;
            this.conversation_info = conversation_info;
        }
        public void scrollDown(){
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = layoutInflater.inflate(R.layout.chat_item,parent,false);

            TextView messageView = itemView.findViewById(R.id.chat_item_text_view_message);
            messageView.setText(messages.get(position).content);

            if(conversation_info.conversation_name.equals(messages.get(position).sender.username)){
                messageView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corners_grey));
                messageView.setTextColor(Color.parseColor("#000000"));

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)messageView.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                messageView.setLayoutParams(params);

                int paddingSizeInDp = 10;
                float scale = getResources().getDisplayMetrics().density;
                int dpAsPixels = (int) (paddingSizeInDp*scale + 0.5f);
                messageView.setPadding(dpAsPixels,dpAsPixels,dpAsPixels,dpAsPixels);
            }
            if(position == (getCount()-1)){
                //parent.setSelected(true);
            }

            return itemView;
        }
    }
}
