package com.example.doglogbackend;

public record ContactMessage(
        int id,
        String senderId,
        String receiverId,
        String dogName,
        String title,
        String content,
        String replyContent,
        String createdAt,
        String repliedAt) {
}
