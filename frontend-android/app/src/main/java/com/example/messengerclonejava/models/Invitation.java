package com.example.messengerclonejava.models;

public class Invitation {
    public String invitation_id;
    public UserInfo sender;

    public Invitation(String invitation_id, UserInfo sender) {
        this.invitation_id = invitation_id;
        this.sender = sender;
    }
}
