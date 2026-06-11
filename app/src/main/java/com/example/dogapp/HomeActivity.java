package com.example.dogapp;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends Fragment {

    private RecyclerView recyclerViewDogs;
    private DogAdapter adapter;
    private ArrayList<Dog> dogList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        // 상단 버튼 이벤트 처리
        TextView tvGoRegister = view.findViewById(R.id.tvGoRegister);
        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SigninActivity.class);
                startActivity(intent);
            }
        });

        TextView tvGoLogin = view.findViewById(R.id.tvGoLogin);
        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        Button btnWriteDiaryMain = view.findViewById(R.id.btnWriteDiaryMain);
        btnWriteDiaryMain.setOnClickListener(v -> openWritePage());

        // 리사이클러뷰 및 어댑터 초기 세팅
        recyclerViewDogs = view.findViewById(R.id.recyclerViewDogs);
        recyclerViewDogs.setLayoutManager(new LinearLayoutManager(getContext()));
        dogList = new ArrayList<>();
        adapter = new DogAdapter(dogList);
        recyclerViewDogs.setAdapter(adapter);

        // DB에서 데이터 불러오기 함수 실행
        loadDogDataFromServer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            loadDogDataFromServer();
        }
    }

    private void openWritePage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = prefs.getString("userID", "");
        String userType = prefs.getString("userType", "");

        if (userID.isEmpty()) {
            Toast.makeText(getActivity(), "로그인 후 일지를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        if (!userType.equals("foster")) {
            Toast.makeText(getActivity(), "임시보호자만 일지를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), WriteActivity.class);
        startActivity(intent);
    }

    private void loadDogDataFromServer() {
        String url = ServerConfig.endpoint("GetDogList.jsp");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            dogList.clear(); // 기존 리스트 초기화

                            // 서버에서 보낸 JSON 문자열을 배열로 변환
                            JSONArray jsonArray = new JSONArray(response.trim());

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                // JSON에서 각 항목 추출
                                String name = obj.getString("name");
                                String breed = obj.getString("breed");
                                int age = obj.getInt("age");
                                String status = obj.getString("status");

                                // 화면에 보여줄 형태로 문자열 조합
                                String info = breed + " · 약 " + age + "살";

                                // Dog 객체 생성 후 리스트에 추가
                                dogList.add(new Dog(name, info, status));
                            }

                            // 어댑터에 데이터가 변경되었음을 알림
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "데이터 분석 오류 발생", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(getActivity(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        requestQueue.add(stringRequest);
    }
}
