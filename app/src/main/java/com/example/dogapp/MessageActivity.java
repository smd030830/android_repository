package com.example.dogapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이전에 만들어둔 메시지 레이아웃 화면을 연결
        setContentView(R.layout.activity_message);
    }
}