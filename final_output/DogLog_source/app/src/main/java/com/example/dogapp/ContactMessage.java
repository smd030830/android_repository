package com.example.dogapp;

public class ContactMessage {
    private final int id;
    private final String senderId;
    private final String receiverId;
    private final String dogName;
    private final String title;
    private final String content;
    private final String replyContent;
    private final String createdAt;
    private final String repliedAt;

    public ContactMessage(
            int id,
            String senderId,
            String receiverId,
            String dogName,
            String title,
            String content,
            String replyContent,
            String createdAt,
            String repliedAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.dogName = dogName;
        this.title = title;
        this.content = content;
        this.replyContent = replyContent;
        this.createdAt = createdAt;
        this.repliedAt = repliedAt;
    }

    public int getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getDogName() {
        return dogName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getRepliedAt() {
        return repliedAt;
    }
}
