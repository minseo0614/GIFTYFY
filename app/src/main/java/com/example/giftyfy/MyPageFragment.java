package com.example.giftyfy;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.friend.Friend;
import com.example.giftyfy.login.LoginActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPageFragment extends Fragment {

    private TextView tvMonthTitle;
    private TextView tvMyInterestsTitle;
    private RecyclerView rvCalendar;
    private RecyclerView rvReceivedGifts;
    private RecyclerView rvWishlist;
    private Calendar selectedDate;

    private ChipGroup cgMyInterests;
    private Button btnAddInterest;
    private Button btnLogout;

    private final String[] availableTags = {
            "디저트러버", "애주가", "상품권애호가", "카페돌이", "고기진심러",
            "빵지순례자", "편의점단골", "배달앱VIP", "집순이", "집돌이",
            "향기컬렉터", "캠핑매니아", "프로직장인", "운동매니아", "영양제신봉자",
            "피부관리진심러", "다이어터", "귀여운게최고", "댕냥이집사", "독서가",
            "게임덕후", "보드게이머", "요리꿈나무", "패션피플", "주얼리수집가", "문구가좋아"
    };

    private String myName = "";
    private String myBirthday = "";
    private List<String> myInterests = new ArrayList<>();
    
    private List<Friend> realFriends = new ArrayList<>();

    private ProductAdapter receivedAdapter;
    private ProductAdapter wishAdapter;

    public MyPageFragment() {
        super(R.layout.fragment_mypage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = Calendar.getInstance();

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvMyInterestsTitle = view.findViewById(R.id.tvMyInterestsTitle);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvReceivedGifts = view.findViewById(R.id.rvReceivedGifts);
        rvWishlist = view.findViewById(R.id.rvWishlist);
        cgMyInterests = view.findViewById(R.id.cgMyInterests);
        btnAddInterest = view.findViewById(R.id.btnAddInterest);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupRecyclerViews();
        loadMyProfileFromServer();
        loadRealFriendsFromServer();
        setupCalendarButtons(view);

        btnAddInterest.setOnClickListener(v -> showTagSelectDialog());
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        rvReceivedGifts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        receivedAdapter = new ProductAdapter();
        rvReceivedGifts.setAdapter(receivedAdapter);

        rvWishlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        wishAdapter = new ProductAdapter();
        rvWishlist.setAdapter(wishAdapter);
    }

    private void loadMyProfileFromServer() {
        FirebaseManager.getInstance().listenToMyProfile(data -> {
            if (data != null) {
                myName = (String) data.get("name");
                myBirthday = (String) data.get("birthday");
                
                if (myName != null && !myName.isEmpty()) {
                    tvMyInterestsTitle.setText(myName + "님의 취향 키워드");
                } else {
                    tvMyInterestsTitle.setText("나의 취향 키워드");
                }

                List<String> loadedInterests = (List<String>) data.get("interests");
                if (loadedInterests != null) {
                    myInterests = new ArrayList<>(loadedInterests);
                    updateInterestsUI();
                }

                // 위시리스트 ID 목록 가져오기
                List<String> wishIds = (List<String>) data.get("wishlist");
                if (wishIds == null) wishIds = new ArrayList<>();

                // ✅ 어댑터들에 위시리스트 상태 알려주기
                if (receivedAdapter != null) receivedAdapter.setWishlistIds(wishIds);
                if (wishAdapter != null) wishAdapter.setWishlistIds(wishIds);

                // ✅ 받은 선물 목록 로드
                List<String> receivedIds = (List<String>) data.get("receivedGifts");
                if (receivedIds != null && !receivedIds.isEmpty()) {
                    FirebaseManager.getInstance().getProductsByIds(receivedIds, products -> {
                        receivedAdapter.setItems(products);
                    });
                } else {
                    receivedAdapter.setItems(new ArrayList<>());
                }

                // ✅ 위시리스트 목록 로드
                if (!wishIds.isEmpty()) {
                    FirebaseManager.getInstance().getProductsByIds(wishIds, products -> {
                        wishAdapter.setItems(products);
                    });
                } else {
                    wishAdapter.setItems(new ArrayList<>());
                }
            }
        });
    }

    private void loadRealFriendsFromServer() {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(friends -> {
            if (friends != null) {
                this.realFriends = friends;
                updateCalendar();
            }
        });
    }

    private void updateInterestsUI() {
        cgMyInterests.removeAllViews();
        for (String tag : myInterests) {
            addTagChipToUI(tag);
        }
    }

    private void addTagChipToUI(String tag) {
        Chip chip = new Chip(getContext());
        chip.setText("#" + tag);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setChipStrokeColorResource(R.color.black);
        chip.setChipStrokeWidth(1f);
        chip.setOnCloseIconClickListener(v -> {
            myInterests.remove(tag);
            saveProfileToServer();
            cgMyInterests.removeView(chip);
        });
        cgMyInterests.addView(chip);
    }

    private void showTagSelectDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("나의 취향 키워드 선택")
                .setItems(availableTags, (dialog, which) -> {
                    String selectedTag = availableTags[which];
                    if (!myInterests.contains(selectedTag)) {
                        myInterests.add(selectedTag);
                        saveProfileToServer();
                        addTagChipToUI(selectedTag);
                    }
                })
                .show();
    }

    private void saveProfileToServer() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String userId = email.split("@")[0];
        FirebaseManager.getInstance().saveMyProfile(userId, myName, myBirthday, myInterests);
    }

    private void setupCalendarButtons(View view) {
        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월", Locale.KOREA);
        tvMonthTitle.setText(sdf.format(selectedDate.getTime()));
        List<String> daysInMonth = generateDaysInMonth(selectedDate);
        CalendarAdapter adapter = new CalendarAdapter(daysInMonth, realFriends, (Calendar) selectedDate.clone());
        rvCalendar.setAdapter(adapter);
    }

    private List<String> generateDaysInMonth(Calendar calendar) {
        List<String> dayList = new ArrayList<>();
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < firstDayOfWeek; i++) dayList.add("");
        for (int i = 1; i <= lastDay; i++) dayList.add(String.valueOf(i));
        return dayList;
    }
}