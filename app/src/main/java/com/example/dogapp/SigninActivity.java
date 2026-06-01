package com.example.dogapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
    private Spinner spinnerUserType;
    private Button btnRegisterComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editRegisterId = findViewById(R.id.editRegisterId);
        editRegisterPw = findViewById(R.id.editRegisterPw);
        editRegisterEmail = findViewById(R.id.editRegisterEmail);
        rgGender = findViewById(R.id.rgGender);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        btnRegisterComplete = findViewById(R.id.btnRegisterComplete);

        // 스피너에 들어갈 목록 설정 (임시보호자, 입양희망자)
        String[] userTypes = {"임시보호자", "입양희망자"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

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

                // 스피너에서 선택된 값을 영문 기준 데이터로 변경
                String selectedType = spinnerUserType.getSelectedItem().toString();
                String userType = "adopter";
                if (selectedType.equals("임시보호자")) {
                    userType = "foster";
                }

                if (userID.isEmpty() || userPassword.isEmpty()) {
                    Toast.makeText(SigninActivity.this, "필수 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendRegisterRequest(userID, userPassword, userEmail, userGender, userType);
            }
        });
    }

    private void sendRegisterRequest(final String userID, final String userPassword, final String userEmail, final String userGender, final String userType) {
        // 에뮬레이터 IP 주소 오타 수정 (10.0.2.2)
        String url = "http://10.0.2.2:336/ServerProject/UserRegister.jsp";

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
                params.put("userType", userType); // 회원 구분 추가
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}