package com.example.messengerclonejava;

import android.app.Application;

import com.example.messengerclonejava.models.ConversationInfo;
import com.example.messengerclonejava.models.Message;
import com.example.messengerclonejava.models.UserInfo;

import java.util.List;

public class MyApplication extends Application {
    private String AccessToken;
    private UserInfo LoggedUser;
    private NotificationListener notificationListener;
    private Conversations.ConversationInfoAdapter conversationInfoAdapter;
    private Chat.ChatAdapter chatAdapter;

    public Conversations.ConversationInfoAdapter getConversationInfoAdapter() {
        return conversationInfoAdapter;
    }

    public void setConversationInfoAdapter(Conversations.ConversationInfoAdapter conversationInfoAdapter) {
        this.conversationInfoAdapter = conversationInfoAdapter;
    }

    public Chat.ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(Chat.ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }



    public void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    public void setLoggedUser(UserInfo loggedUser) {
        LoggedUser = loggedUser;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public UserInfo getLoggedUser() {
        return LoggedUser;
    }

    public NotificationListener getNotificationListener() {
        return notificationListener;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }
}
