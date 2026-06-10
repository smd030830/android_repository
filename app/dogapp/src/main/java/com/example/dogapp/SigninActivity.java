package com.example.dogapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    private EditText editRegisterId, editRegisterPw, editRegisterEmail;
    private RadioGroup rgGender;
    private Button btnRegisterComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editRegisterId = findViewById(R.id.editRegisterId);
        editRegisterPw = findViewById(R.id.editRegisterPw);
        editRegisterEmail = findViewById(R.id.editRegisterEmail);
        rgGender = findViewById(R.id.rgGender);
        btnRegisterComplete = findViewById(R.id.btnRegisterComplete);

        btnRegisterComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = editRegisterId.getText().toString();
                String userPassword = editRegisterPw.getText().toString();
                String userEmail = editRegisterEmail.getText().toString();

                int checkedId = rgGender.getCheckedRadioButtonId();
                String userGender = "";
                if (checkedId == R.id.rbMale) {
                    userGender = "male";
                } else if (checkedId == R.id.rbFemale) {
                    userGender = "female";
                }

                if (userID.isEmpty() || userPassword.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "필수 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendRegisterRequest(userID, userPassword, userEmail,  userGender);
            }
        });
    }

    private void sendRegisterRequest(final String userID, final String userPassword, final String userEmail, final String userGender) {
        // 본인의 서버 IP 주소와 JSP 파일 경로로 수정할게 나중에
        String url = "http://10.0.2.2:8080/ServerProject/UserRegister.jsp";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("success")) {
                            Toast.makeText(SigninActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SigninActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                //예외처리느낌
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SigninActivity.this, "네트워크 오류 발생: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("userPassword", userPassword);
                params.put("userEmail", userEmail);
                params.put("userGender", userGender);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
