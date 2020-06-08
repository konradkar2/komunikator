package com.example.messengerclonejava;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.messengerclonejava.models.Invitation;
import com.example.messengerclonejava.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReceivedInvitations extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.received_invitations,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //ListView listView = (ListView) getView().findViewById(R.id.listViewSearchResult);
        ImageView imageAdd = getView().findViewById(R.id.fragment_received_image_add);
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToInvite();
            }
        });


        TextView usernameView = getActivity().findViewById(R.id.received_invitations_no_invites);
        usernameView.setVisibility(View.GONE);

        //setting adapter for custom list view
        ListView listView = (ListView) getActivity().findViewById(R.id.received_invitations_listView);

        final List<Invitation> received_invitations = new ArrayList<Invitation>();

        final ReceivedInvitesAdapter adapter = new ReceivedInvitesAdapter(getContext(),received_invitations);

        listView.setAdapter(adapter);

        //getting invitations

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            String url = BuildConfig.API_URL + "/invitations";


            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{

                        JSONObject jsonObj = new JSONObject(response);
                        jsonObj = jsonObj.getJSONObject("invitations");
                        JSONArray c = jsonObj.getJSONArray("received");
                        for (int i = 0 ; i < c.length(); i++) {
                            JSONObject obj = c.getJSONObject(i);

                            String invitation_id = obj.getString("invitation_id");
                            UserInfo userInfo = UserInfo.JsonToUserInfo(obj.getJSONObject("sender"));

                            Invitation invitation = new Invitation(invitation_id,userInfo);


                            if(!received_invitations.contains(invitation)) {
                                received_invitations.add(invitation);
                            }

                        }
                        TextView usernameView = getActivity().findViewById(R.id.received_invitations_no_invites);
                        if(received_invitations.isEmpty()){
                            usernameView.setVisibility(View.VISIBLE);
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
                    Toast.makeText(getContext(),"Something went wrong: " + Utility.getVolleyErrorMessage(error), Toast.LENGTH_SHORT).show();

                    //todo: on 401

                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return Utility.header_jwt(getActivity().getApplication());
                }
            };

            requestQueue.add(stringRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }




    }
    class ReceivedInvitesAdapter extends ArrayAdapter<Invitation> {
        Context context;
        List<Invitation> invitations;

        ReceivedInvitesAdapter(Context c, List<Invitation> invitations_){
            super(c,R.layout.activity_invite_item, invitations_);
            context = c;
            invitations = invitations_;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = layoutInflater.inflate(R.layout.received_invitations_item,parent,false);


            ImageView imageView = itemView.findViewById(R.id.received_invitations_image);
            TextView usernameView = itemView.findViewById(R.id.received_invitations_text_username);
            TextView aboutView = itemView.findViewById(R.id.received_invitations_text_about);

            //todo: set image resource
            usernameView.setText(invitations.get(position).sender.username);
            aboutView.setText(invitations.get(position).sender.about);

            //setting button listener
            final Button buttonAccept = itemView.findViewById(R.id.received_invitations_button_accept);
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    StringRequest request = manageInvitationRequest(position,true);
                    try{
                        requestQueue.add(request);
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }

                }
            });
            final Button buttonDecline = itemView.findViewById(R.id.received_invitations_button_decline);
            buttonDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    StringRequest request = manageInvitationRequest(position,false);
                    try{
                        requestQueue.add(request);
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }

                }
            });


            return itemView;
        }
        private StringRequest manageInvitationRequest( final int position, final boolean accept){

            String url = BuildConfig.API_URL + "/invitation/manage";
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("invitation_id", invitations.get(position).invitation_id);
                jsonBody.put("accept", accept);
            }
            catch (JSONException jsonEx){
                Log.e("received_invites", "unexpected JSON exception", jsonEx);
            }
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String text  = (accept ? "Accepted" : "Declined");

                    Toast.makeText(getContext(),text, Toast.LENGTH_SHORT).show();
                    invitations.remove(position);

                    if(invitations.isEmpty()){
                        TextView usernameView = getActivity().findViewById(R.id.received_invitations_no_invites);
                        usernameView.setVisibility(View.VISIBLE);
                    }

                    notifyDataSetChanged();
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
                    return Utility.header_jwt(getActivity().getApplication());
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return requestBody.getBytes();
            }
            };

            return stringRequest;
        }
    }
    private void switchToInvite(){
        Intent intent = new Intent(getActivity(), Invite.class);
        startActivity(intent);
    }




}

