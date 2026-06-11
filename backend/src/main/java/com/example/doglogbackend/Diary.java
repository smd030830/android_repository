package com.example.doglogbackend;

public record Diary(
        String fosterId,
        String dogName,
        String dateText,
        int foodAmount,
        int poopCount,
        String content,
        String photoData) {
}
