package com.example.dogapp;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 하단 바 컴포넌트 불러오기
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 앱이 처음 켜졌을 때 기본으로 보여줄 첫 화면(HomeActivity 프래그먼트) 설정
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new HomeActivity())
                    .commit();
        }

        // 하단 바의 메뉴를 클릭했을 때 화면을 바꾸는 이벤트 리스너 설정
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // 클릭한 메뉴의 ID에 따라 바꿀 프래그먼트 지정
                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {
                    selectedFragment = new HomeActivity();
                } else if (itemId == R.id.menu_message) {
                    // 메시지 화면용 프래그먼트가 있다면 연결
                    // selectedFragment = new MessageFragment();
                } else if (itemId == R.id.menu_profile) {
                    // 마이페이지 화면용 프래그먼트가 있다면 연결
                    // selectedFragment = new ProfileFragment();
                }

                // 지정된 프래그먼트가 있다면 화면 중심(main_container)을 해당 화면으로 교체
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });
    }
}