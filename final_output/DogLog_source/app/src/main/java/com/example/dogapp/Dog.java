package com.example.dogapp;

public class Dog {
    private String name;
    private String breed;
    private int age;
    private String status;
    private String fosterId;
    private String photoData;
    private String info;
    private String days;

    public Dog(String name, String info, String days) {
        this(name, "", 0, days, "", "", info);
    }

    public Dog(String name, String breed, int age, String status, String fosterId, String photoData, String info) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.status = status;
        this.fosterId = fosterId;
        this.photoData = photoData;
        this.info = info;
        this.days = status;
    }

    public String getName() { return name; }
    public String getBreed() { return breed; }
    public int getAge() { return age; }
    public String getStatus() { return status; }
    public String getFosterId() { return fosterId; }
    public String getPhotoData() { return photoData; }
    public String getInfo() { return info; }
    public String getDays() { return days; }
}
