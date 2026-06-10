package com.example.dogapp;

public final class ServerConfig {
    public static final String BASE_URL = "http://10.0.2.2:8080/ServerProject/";

    private ServerConfig() {
    }

    public static String endpoint(String fileName) {
        return BASE_URL + fileName;
    }
}
