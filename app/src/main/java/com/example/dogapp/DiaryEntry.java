package com.example.dogapp;

public class DiaryEntry {
    private final int id;
    private final String fosterId;
    private final String dogName;
    private final String dateText;
    private final int foodAmount;
    private final int poopCount;
    private final String content;
    private final String photoData;

    public DiaryEntry(String dogName, String dateText, int foodAmount, int poopCount, String content, String photoData) {
        this(0, "", dogName, dateText, foodAmount, poopCount, content, photoData);
    }

    public DiaryEntry(String fosterId, String dogName, String dateText, int foodAmount, int poopCount, String content, String photoData) {
        this(0, fosterId, dogName, dateText, foodAmount, poopCount, content, photoData);
    }

    public DiaryEntry(int id, String fosterId, String dogName, String dateText, int foodAmount, int poopCount, String content, String photoData) {
        this.id = id;
        this.fosterId = fosterId;
        this.dogName = dogName;
        this.dateText = dateText;
        this.foodAmount = foodAmount;
        this.poopCount = poopCount;
        this.content = content;
        this.photoData = photoData;
    }

    public int getId() {
        return id;
    }

    public String getFosterId() {
        return fosterId;
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
