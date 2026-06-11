package com.example.dogapp;

public class DiaryEntry {
    private final String dogName;
    private final String dateText;
    private final int foodAmount;
    private final int poopCount;
    private final String content;
    private final String photoData;

    public DiaryEntry(String dogName, String dateText, int foodAmount, int poopCount, String content, String photoData) {
        this.dogName = dogName;
        this.dateText = dateText;
        this.foodAmount = foodAmount;
        this.poopCount = poopCount;
        this.content = content;
        this.photoData = photoData;
    }

    public String getDogName() {
        return dogName;
    }

    public String getDateText() {
        return dateText;
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public int getPoopCount() {
        return poopCount;
    }

    public String getContent() {
        return content;
    }

    public String getPhotoData() {
        return photoData;
    }
}
