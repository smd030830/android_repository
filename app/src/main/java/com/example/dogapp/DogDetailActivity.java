package com.example.dogapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DogDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);

        TextView tvDetailDogName = findViewById(R.id.tvDetailDogName);
        Button btnGoWrite = findViewById(R.id.btnGoWrite);
        Button btnGoMessage = findViewById(R.id.btnGoMessage);

        // 어댑터에서 보낸 강아지 이름을 받아서 화면에 띄dnrl
        String dogName = getIntent().getStringExtra("dogName");
        if (dogName != null) {
            tvDetailDogName.setText(dogName);
        }

        // 로그인할 때 스마트폰 저장소(SharedPreferences)에 저장해둔 사용자 권한을 불러오기
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = prefs.getString("userType", ""); // "foster"(임시보호자) 또는 "adopter"(입양희망자)

        // 권한에 따라 보여줄 버튼을 결정
        if (userType.equals("foster")) {
            btnGoWrite.setVisibility(View.VISIBLE);
        } else if (userType.equals("adopter")) {
            btnGoMessage.setVisibility(View.VISIBLE);
        }

        btnGoWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DogDetailActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });

        btnGoMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DogDetailActivity.this, MessageActivity.class);
                startActivity(intent);
            }
        });
    }
}