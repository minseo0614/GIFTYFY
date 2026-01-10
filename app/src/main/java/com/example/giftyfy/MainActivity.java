package com.example.giftyfy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.giftyfy.friend.FriendsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements FriendsFragment.OnFriendGiftClickListener {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_friends) {
                fragment = new FriendsFragment();
            } else if (id == R.id.nav_gifts) {
                // ✅ 그냥 선물탭 눌러서 들어오는 경우
                fragment = GiftsRecommendFragment.newDefault();
            } else {
                fragment = new MyPageFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_friends);
        }
    }

    // ✅ 친구 탭에서 "선물하러 가기"로 넘어온 경우
    @Override
    public void onFriendGiftClick(String friendName,
                                  String relation,
                                  ArrayList<String> interests,
                                  ArrayList<String> receivedTitles) {

        bottomNav.getMenu().findItem(R.id.nav_gifts).setChecked(true);

        Fragment fragment = GiftsRecommendFragment.newInstance(
                friendName, relation, interests, receivedTitles
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}