package com.example.giftyfy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.giftyfy.friend.FriendsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    private String pendingFriendUid = null;
    private String pendingFriendName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            replaceFragment(new FriendsFragment());
            bottomNav.setSelectedItemId(R.id.nav_friends);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_friends) {
                pendingFriendUid = null;
                pendingFriendName = null;
                replaceFragment(new FriendsFragment());
                return true;
            }

            if (id == R.id.nav_gifts) {
                if (pendingFriendUid != null) {
                    replaceFragment(GiftsRecommendFragment.newInstance(pendingFriendUid, pendingFriendName));
                } else {
                    replaceFragment(new GiftsFragment());
                }
                return true;
            }

            if (id == R.id.nav_mypage) {
                pendingFriendUid = null;
                pendingFriendName = null;
                replaceFragment(new MyPageFragment());
                return true;
            }

            return false;
        });
    }

    private void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void onFriendGiftClick(String friendUid, String friendName) {
        pendingFriendUid = friendUid;
        pendingFriendName = friendName;
        bottomNav.setSelectedItemId(R.id.nav_gifts);
    }

    // 선물 추천 탭 -> 친구 탭
    public void backToFriends() {
        pendingFriendUid = null;
        pendingFriendName = null;
        bottomNav.setSelectedItemId(R.id.nav_friends);
    }
}
