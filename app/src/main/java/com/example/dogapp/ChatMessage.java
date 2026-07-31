package com.example.dogapp;

public class ChatMessage {
    private String id;
    private String senderId;
    private String text;
    private long sentAt;

    public ChatMessage() {
    }

    public ChatMessage(String id, String senderId, String text, long sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.sentAt = sentAt;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public long getSentAt() {
        return sentAt;
    }
}
