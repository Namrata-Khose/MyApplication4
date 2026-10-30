package com.example.gupshup;

public class ChatModel {

    private String userId;
    private String userName;
    private String lastMessage;
    private String time;

    // Required empty constructor for Firebase
    public ChatModel() {
    }

    public ChatModel(String userId, String userName, String lastMessage, String time) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }
}
