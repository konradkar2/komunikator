package com.example.messengerclonejava;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.messengerclonejava.models.ConversationInfo;
import com.example.messengerclonejava.models.Message;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class NotificationListener  {
    Socket socket;
    MyApplication application;
    public NotificationListener(MyApplication application){
            this.application = application;
            try {
            String uri = BuildConfig.API_URL;
            socket = IO.socket(uri);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {

                    //socket.disconnect();
                }

            }).on("newMessage", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    String conversation_id = args[0].toString();
                    Log.i("msg",conversation_id);
                    updateLists(conversation_id);
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                }

            }).on("connect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i("io","connected");
                }
            });

            socket.connect();
            socket.emit("message", "hi");
            socket.emit("token", this.application.getAccessToken());


        }
        catch(URISyntaxException e){
            e.printStackTrace();;

        }

    }
    private void updateLists(String conversation_id){
        final Conversations.ConversationInfoAdapter conversationAdapter = this.application.getConversationInfoAdapter();
        if( conversationAdapter != null){
            final List<ConversationInfo> conversations =  conversationAdapter.conversation_info;

            try {
                RequestQueue requestQueue = Volley.newRequestQueue( conversationAdapter.context);
                String url = BuildConfig.API_URL + "/conversations";


                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            UserInfo loggedUser = application.getLoggedUser();
                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray convs = jsonObj.getJSONArray("conversations");
                            conversations.clear();
                            for (int i = 0 ; i < convs.length(); i++) {
                                ConversationInfo conversation_info = ConversationInfo.JsonToConversationInfo(convs.getJSONObject(i),loggedUser);
                                if(!conversations.contains(conversation_info)) {
                                    conversations.add(conversation_info);
                                }
                            }
                            conversationAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Log.e("conversations", "unexpected JSON exception", e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                        //todo: on 401
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return Utility.header_jwt(application);
                    }
                };

                requestQueue.add(stringRequest);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            final Chat.ChatAdapter chatAdapter = application.getChatAdapter();
            if(chatAdapter != null){
                try {
                    final List<Message> messages = chatAdapter.messages;
                    RequestQueue requestQueue = Volley.newRequestQueue(chatAdapter.context);
                    String last_message_id = "0";
                    if(messages.size() > 0){
                        last_message_id = messages.get(messages.size()-1).message_id;
                    }
                    String url = BuildConfig.API_URL + "/message/" +conversation_id + "/" + last_message_id;

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
                                chatAdapter.notifyDataSetChanged();

                            }
                            catch (JSONException e){
                                Log.e("invites", "unexpected JSON exception", e);
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", Utility.getVolleyErrorMessage(error));
                            //todo: on 401
                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            return Utility.header_jwt(application);
                        }
                    };
                    requestQueue.add(stringRequest);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


}
