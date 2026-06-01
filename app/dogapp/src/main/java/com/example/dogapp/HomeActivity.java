package com.example.dogapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private void loadDogDataFromServer() {
        // 본인 서버 IP와 프로젝트명에 맞게 URL을 수정해야 합니다. (에뮬레이터 기본 IP는 10.0.2.2)
        String url = "http://10.0.2.2:8080/ServerProject/GetDogList.jsp";

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