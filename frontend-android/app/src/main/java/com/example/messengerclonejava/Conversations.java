package com.example.messengerclonejava;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.messengerclonejava.models.ConversationInfo;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Conversations extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversations,container,false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button button = getActivity().findViewById(R.id.button_logout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        ListView listView = (ListView) getView().findViewById(R.id.conversationsList);

        final List<ConversationInfo> conversations = new ArrayList<ConversationInfo>();
        final ConversationInfoAdapter adapter = new ConversationInfoAdapter(getContext(),conversations);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),Chat.class);
                intent.putExtra("conversation_info",conversations.get(position));

                startActivity(intent);
            }
        });
        final MyApplication application = ((MyApplication)getActivity().getApplication());
        application.setConversationInfoAdapter(adapter);
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            String url = BuildConfig.API_URL + "/conversations";


            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        UserInfo loggedUser = application.getLoggedUser();
                        JSONObject jsonObj = new JSONObject(response);
                        JSONArray convs = jsonObj.getJSONArray("conversations");
                        for (int i = 0 ; i < convs.length(); i++) {
                            ConversationInfo conversation_info = ConversationInfo.JsonToConversationInfo(convs.getJSONObject(i),loggedUser);
                            if(!conversations.contains(conversation_info)) {
                                conversations.add(conversation_info);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    catch (JSONException e){
                        Log.e("conversations", "unexpected JSON exception", e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    Toast.makeText(getContext(),"Something went wrong: " + Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();

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



    }
    private void logout(){
        MyApplication application = ((MyApplication)getActivity().getApplication());
        application.getNotificationListener().socket.disconnect();
        Intent intent = new Intent(getContext(), Login.class);
        startActivity(intent);
    }
    class ConversationInfoAdapter extends ArrayAdapter<ConversationInfo> {
        Context context;
        List<ConversationInfo> conversation_info;
        UserInfo loggedUser;

        ConversationInfoAdapter(Context c, List<ConversationInfo> conversation_info_){
            super(c,R.layout.conversations_item, conversation_info_);
            context = c;
            conversation_info = conversation_info_;
            loggedUser = ((MyApplication)getActivity().getApplication()).getLoggedUser();
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = layoutInflater.inflate(R.layout.conversations_item,parent,false);

            TextView conversation_nameView = itemView.findViewById(R.id.conversations_item_text_view_conversation_name);
            TextView sender_nameView = itemView.findViewById(R.id.conversations_item_text_view_sender);
            TextView last_messageView = itemView.findViewById(R.id.conversations_item_text_view_message);
            TextView last_message_sent_onView = itemView.findViewById(R.id.conversations_item_text_view_sent_on);

            //todo: set image resource

            //setting conversation name, currently only working for conversations with 2 members,
            //basically set conversation name to user's that is different than logged user.
            conversation_nameView.setText(conversation_info.get(position).conversation_name);



            if(conversation_info.get(position).message_count >0){
                if(conversation_info.get(position).last_message_sender.id == loggedUser.id){
                    sender_nameView.setText("you" + ":");
                }
                else{
                    sender_nameView.setText(conversation_info.get(position).last_message_sender.username + ":");
                }
                last_messageView.setText(conversation_info.get(position).last_message);
                String send_on = new SimpleDateFormat("HH:mm").format(
                        conversation_info.get(position).last_message_sent_on
                );
                last_message_sent_onView.setText(send_on);
            }
            else{
                sender_nameView.setVisibility(View.GONE);
                last_messageView.setVisibility(View.GONE);
                String created_on = new SimpleDateFormat("HH:mm").format(
                        conversation_info.get(position).created_on
                );
                last_message_sent_onView.setText(created_on);
            }

            return itemView;
        }
    }
}
