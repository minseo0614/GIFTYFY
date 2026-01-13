package com.example.giftyfy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.giftyfy.friend.FriendsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements FriendsFragment.OnFriendGiftClickListener {

    private BottomNavigationView bottomNav;

    // ✅ 친구에서 넘어온 경우에만 추천 프래그먼트 띄우기 위한 값
    private String pendingFriendUid = null;
    private String pendingFriendName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // 첫 화면
        if (savedInstanceState == null) {
            replaceFragment(new FriendsFragment());
            bottomNav.setSelectedItemId(R.id.nav_friends);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_friends) {
                // 친구 탭으로 가면 pending 유지할 필요 없음
                pendingFriendUid = null;
                pendingFriendName = null;
                replaceFragment(new FriendsFragment());
                return true;
            }

            if (id == R.id.nav_gifts) {
                // ✅ 핵심: 친구에서 넘어온 경우는 "추천(top6)" 프래그먼트
                if (pendingFriendUid != null) {
                    replaceFragment(GiftsRecommendFragment.newInstance(pendingFriendUid, pendingFriendName));
                } else {
                    replaceFragment(new GiftsFragment()); // 탭2(선물) 기본은 전체상품
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

    // ✅ FriendsFragment에서 "선물하러가기" 눌렀을 때 호출됨
    @Override
    public void onFriendGiftClick(String friendUid, String friendName) {
        pendingFriendUid = friendUid;
        pendingFriendName = friendName;

        // 선물 탭 선택 -> 위 리스너에서 pendingFriendUid 보고 추천 프래그먼트로 분기됨
        bottomNav.setSelectedItemId(R.id.nav_gifts);
    }
}