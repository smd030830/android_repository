package com.example.dogapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이전에 만들어둔 일지 작성 레이아웃 화면을 연결
        setContentView(R.layout.activity_write);
    }
}