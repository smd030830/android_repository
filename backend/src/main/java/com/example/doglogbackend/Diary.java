package com.example.doglogbackend;

public record Diary(
        int id,
        String fosterId,
        String dogName,
        String dateText,
        int foodAmount,
        int poopCount,
        String content,
        String photoData) {
}
