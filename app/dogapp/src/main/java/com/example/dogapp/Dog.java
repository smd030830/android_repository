package com.example.dogapp;

public class Dog {
    private String name;
    private String info;
    private String days;

    public Dog(String name, String info, String days) {
        this.name = name;
        this.info = info;
        this.days = days;
    }

    public String getName() { return name; }
    public String getInfo() { return info; }
    public String getDays() { return days; }
}