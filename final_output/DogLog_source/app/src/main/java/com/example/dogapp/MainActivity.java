package com.example.dogapp;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new HomeActivity())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {
                    selectedFragment = new HomeActivity();
                } else if (itemId == R.id.menu_message) {
                    selectedFragment = new MyPageFragment();
                } else if (itemId == R.id.menu_profile) {
                    selectedFragment = new MyPageFragment();
                }

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

    public void openHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new HomeActivity())
                .commit();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.menu_home);
        }
    }

    public void openProfile() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new MyPageFragment())
                .commit();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.menu_profile);
        }
    }
}
