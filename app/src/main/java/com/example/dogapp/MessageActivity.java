package com.example.dogapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private static final String UTF8_FORM_CONTENT_TYPE =
            "application/x-www-form-urlencoded; charset=UTF-8";

    private String senderId;
    private String recipientId;
    private String dogName;
    private EditText editContactTitle;
    private EditText editContactMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        TextView tvFosterName = findViewById(R.id.tvFosterName);
        editContactTitle = findViewById(R.id.editContactTitle);
        editContactMessage = findViewById(R.id.editContactMessage);
        Button btnSendMessage = findViewById(R.id.btnSendMessage);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        senderId = prefs.getString("userID", "");
        recipientId = getIntent().getStringExtra("recipientId");
        dogName = getIntent().getStringExtra("dogName");
        String diaryDate = getIntent().getStringExtra("diaryDate");

        if (TextUtils.isEmpty(recipientId)) {
            recipientId = getIntent().getStringExtra("fosterId");
        }
        if (TextUtils.isEmpty(dogName)) {
            dogName = "강아지";
        }

        tvFosterName.setText(TextUtils.isEmpty(recipientId) ? "알 수 없음" : recipientId);
        editContactTitle.setText(dogName + " 문의");
        if (!TextUtils.isEmpty(diaryDate)) {
            editContactMessage.setHint(diaryDate + " 일지를 보고 문의할 내용을 적어주세요.");
        }

        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String title = editContactTitle.getText().toString().trim();
        String content = editContactMessage.getText().toString().trim();

        if (TextUtils.isEmpty(senderId)) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(recipientId)) {
            Toast.makeText(this, "받는 사람 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "문의 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ServerConfig.endpoint("SaveMessage.jsp"),
                response -> {
                    if ("success".equals(response.trim())) {
                        Toast.makeText(this, "문의 메시지를 보냈습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "문의 메시지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "서버 연결을 확인하세요.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("senderId", senderId);
                params.put("receiverId", recipientId);
                params.put("dogName", dogName);
                params.put("title", title);
                params.put("content", content);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return UTF8_FORM_CONTENT_TYPE;
            }
        };
        request.setShouldCache(false);
        Volley.newRequestQueue(this).add(request);
    }
}
