package com.example.dogapp;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

public class HomeActivity extends Fragment {
    private static final String ALL_BREEDS = "전체 품종";
    private static final String ALL_AGES = "전체 나이";

    private DogAdapter adapter;
    private ArrayList<Dog> dogList;
    private ArrayList<Dog> allDogList;
    private ArrayAdapter<String> breedAdapter;
    private EditText editSearch;
    private Spinner spinnerBreed;
    private Spinner spinnerAge;
    private TextView tvGoLogin;
    private TextView tvGoRegister;
    private View viewAuthDivider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        tvGoRegister = view.findViewById(R.id.tvGoRegister);
        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SigninActivity.class);
                startActivity(intent);
            }
        });

        tvGoLogin = view.findViewById(R.id.tvGoLogin);
        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userID = prefs.getString("userID", "");
                if (userID.isEmpty()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openProfile();
                }
            }
        });
        viewAuthDivider = view.findViewById(R.id.viewAuthDivider);

        Button btnWriteDiaryMain = view.findViewById(R.id.btnWriteDiaryMain);
        btnWriteDiaryMain.setOnClickListener(v -> openWritePage());
        applyAuthHeader();

        allDogList = new ArrayList<>();
        dogList = new ArrayList<>();

        editSearch = view.findViewById(R.id.editSearch);
        spinnerBreed = view.findViewById(R.id.spinnerBreed);
        spinnerAge = view.findViewById(R.id.spinnerAge);
        setupSearchAndFilters();

        RecyclerView recyclerViewDogs = view.findViewById(R.id.recyclerViewDogs);
        recyclerViewDogs.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DogAdapter(dogList);
        recyclerViewDogs.setAdapter(adapter);

        loadDogDataFromServer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            applyAuthHeader();
            loadDogDataFromServer();
        }
    }

    private void applyAuthHeader() {
        if (getActivity() == null || tvGoLogin == null || tvGoRegister == null || viewAuthDivider == null) {
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = prefs.getString("userID", "");
        boolean loggedIn = !userID.isEmpty();
        tvGoLogin.setText(loggedIn ? "마이페이지" : "로그인");
        tvGoRegister.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        viewAuthDivider.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
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

        if (!"foster".equals(userType)) {
            Toast.makeText(getActivity(), "임시보호자만 일지를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), WriteActivity.class);
        startActivity(intent);
    }

    private void setupSearchAndFilters() {
        breedAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>());
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(breedAdapter);

        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{ALL_AGES, "0~1살", "2~4살", "5~8살", "9살 이상"});
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(ageAdapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyDogFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyDogFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyDogFilters();
            }
        };
        spinnerBreed.setOnItemSelectedListener(filterListener);
        spinnerAge.setOnItemSelectedListener(filterListener);
        refreshBreedFilterOptions();
    }

    private void loadDogDataFromServer() {
        String url = buildDogListUrl();
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getActivity(), "사용자 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        allDogList.clear();
                        JSONArray jsonArray = new JSONArray(response.trim());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String name = obj.getString("name");
                            String breed = obj.getString("breed");
                            int age = obj.getInt("age");
                            String status = obj.getString("status");
                            String fosterId = obj.optString("fosterId");
                            String photoData = obj.optString("photoData");
                            String info = breed + " · 약 " + age + "살";

                            allDogList.add(new Dog(name, breed, age, status, fosterId, photoData, info));
                        }

                        refreshBreedFilterOptions();
                        applyDogFilters();
                    } catch (JSONException e) {
                        Log.e("HomeActivity", "Failed to parse dog list", e);
                        Toast.makeText(getActivity(), "데이터 분석 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("HomeActivity", "Failed to load dog list", error);
                    Toast.makeText(getActivity(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireActivity()).add(stringRequest);
    }

    private String buildDogListUrl() {
        String url = ServerConfig.endpoint("GetDogList.jsp");
        if (getActivity() == null) {
            return url;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = prefs.getString("userID", "");
        String userType = prefs.getString("userType", "");
        if (!"foster".equals(userType) || TextUtils.isEmpty(userID)) {
            return url;
        }

        try {
            return url + "?fosterId=" + URLEncoder.encode(userID, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private void refreshBreedFilterOptions() {
        if (breedAdapter == null) {
            return;
        }

        String selectedBreed = spinnerBreed == null || spinnerBreed.getSelectedItem() == null
                ? ALL_BREEDS
                : spinnerBreed.getSelectedItem().toString();
        LinkedHashSet<String> breeds = new LinkedHashSet<>();
        breeds.add(ALL_BREEDS);
        if (allDogList != null) {
            for (Dog dog : allDogList) {
                if (!TextUtils.isEmpty(dog.getBreed())) {
                    breeds.add(dog.getBreed());
                }
            }
        }

        breedAdapter.clear();
        breedAdapter.addAll(breeds);
        breedAdapter.notifyDataSetChanged();

        int selectedIndex = 0;
        for (int i = 0; i < breedAdapter.getCount(); i++) {
            if (selectedBreed.equals(breedAdapter.getItem(i))) {
                selectedIndex = i;
                break;
            }
        }
        spinnerBreed.setSelection(selectedIndex);
    }

    private void applyDogFilters() {
        if (dogList == null || adapter == null || allDogList == null) {
            return;
        }

        String query = editSearch == null ? "" : editSearch.getText().toString().trim().toLowerCase(Locale.KOREA);
        String selectedBreed = spinnerBreed == null || spinnerBreed.getSelectedItem() == null
                ? ALL_BREEDS
                : spinnerBreed.getSelectedItem().toString();
        String selectedAge = spinnerAge == null || spinnerAge.getSelectedItem() == null
                ? ALL_AGES
                : spinnerAge.getSelectedItem().toString();

        dogList.clear();
        for (Dog dog : allDogList) {
            if (!matchesSearch(dog, query)) {
                continue;
            }
            if (!ALL_BREEDS.equals(selectedBreed) && !selectedBreed.equals(dog.getBreed())) {
                continue;
            }
            if (!matchesAge(dog.getAge(), selectedAge)) {
                continue;
            }
            dogList.add(dog);
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesSearch(Dog dog, String query) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }

        return containsIgnoreCase(dog.getName(), query)
                || containsIgnoreCase(dog.getBreed(), query)
                || containsIgnoreCase(dog.getStatus(), query)
                || containsIgnoreCase(dog.getFosterId(), query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.KOREA).contains(query);
    }

    private boolean matchesAge(int age, String selectedAge) {
        if (ALL_AGES.equals(selectedAge)) {
            return true;
        }
        if ("0~1살".equals(selectedAge)) {
            return age <= 1;
        }
        if ("2~4살".equals(selectedAge)) {
            return age >= 2 && age <= 4;
        }
        if ("5~8살".equals(selectedAge)) {
            return age >= 5 && age <= 8;
        }
        if ("9살 이상".equals(selectedAge)) {
            return age >= 9;
        }
        return true;
    }
}
