package com.example.dogapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DogDetailActivity extends AppCompatActivity {
    private String dogName;
    private String currentUserID;
    private String userType;
    private DiaryAdapter diaryAdapter;
    private ArrayList<DiaryEntry> diaries;
    private TextView tvEmptyDiaries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_detail);

        TextView tvDetailDogName = findViewById(R.id.tvDetailDogName);
        Button btnGoWrite = findViewById(R.id.btnGoWrite);
        Button btnGoMessage = findViewById(R.id.btnGoMessage);
        RecyclerView recyclerViewDiaries = findViewById(R.id.recyclerViewDiaries);
        tvEmptyDiaries = findViewById(R.id.tvEmptyDiaries);

        dogName = getIntent().getStringExtra("dogName");
        if (dogName != null) {
            tvDetailDogName.setText(dogName);
        } else {
            dogName = "강아지";
        }

        diaries = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(diaries);
        recyclerViewDiaries.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDiaries.setAdapter(diaryAdapter);
        updateEmptyState();

        // 로그인할 때 스마트폰 저장소(SharedPreferences)에 저장해둔 사용자 권한을 불러오기
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserID = prefs.getString("userID", "");
        userType = prefs.getString("userType", ""); // "foster"(임시보호자) 또는 "adopter"(입양희망자)

        // 권한에 따라 보여줄 버튼을 결정
        if (userType.equals("foster") || userType.isEmpty()) {
            btnGoWrite.setVisibility(View.VISIBLE);
        } else if (userType.equals("adopter")) {
            btnGoMessage.setVisibility(View.VISIBLE);
        }

        btnGoWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DogDetailActivity.this, WriteActivity.class);
                intent.putExtra("dogName", dogName);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (diaryAdapter == null) {
            return;
        }
        loadDiariesFromServer();
    }

    private void loadDiariesFromServer() {
        String url;
        try {
            url = ServerConfig.endpoint("GetDiaryList.jsp") + "?dogName=" + URLEncoder.encode(dogName, "UTF-8");
            if ("foster".equals(userType) && !currentUserID.isEmpty()) {
                url += "&fosterId=" + URLEncoder.encode(currentUserID, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "강아지 이름을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    diaries.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject item = response.getJSONObject(i);
                            diaries.add(new DiaryEntry(
                                    item.optString("dogName", dogName),
                                    item.optString("dateText"),
                                    item.optInt("foodAmount"),
                                    item.optInt("poopCount"),
                                    item.optString("content"),
                                    item.optString("photoData")));
                        } catch (JSONException e) {
                            Toast.makeText(this, "일지 정보를 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    diaryAdapter.notifyDataSetChanged();
                    updateEmptyState();
                },
                error -> {
                    diaries.clear();
                    diaryAdapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(this, "서버에서 일지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }

    private void updateEmptyState() {
        tvEmptyDiaries.setVisibility(diaries.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
