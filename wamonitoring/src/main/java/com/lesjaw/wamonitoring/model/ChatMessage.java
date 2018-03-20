package com.lesjaw.wamonitoring.model;

public class ChatMessage {

    private String message;
    private String name;
    private String time;
    private String email;

    // Required default constructor for Firebase object mapping
    private ChatMessage() {
    }

    public ChatMessage(String message, String author, String time, String email) {
        this.message = message;
        this.name = author;
        this.time = time;
        this.email = email;


    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getEmail() {
        return email;
    }

}
