package com.example.giftyfy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.giftyfy.friend.FriendsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // 탭 클릭 시 화면 전환
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_friends) {
                fragment = new FriendsFragment();
            } else if (id == R.id.nav_gifts) {
                fragment = new GiftsFragment();
            } else {
                fragment = new MyPageFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        });

        // 앱 시작 시: "선택"만 해주면 리스너가 알아서 replace 함
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_friends);
        }
    }
}