package com.example.giftyfy.friend;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendsFragment extends Fragment {

    public interface OnFriendGiftClickListener {
        void onFriendGiftClick(String friendName, String relation, ArrayList<String> interests, ArrayList<String> receivedTitles);
    }

    private OnFriendGiftClickListener giftClickListener;
    private FriendAdapter upcomingAdapter;
    private FriendAdapter allAdapter;
    private List<Friend> allFriendsList = new ArrayList<>();

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFriendGiftClickListener) {
            giftClickListener = (OnFriendGiftClickListener) context;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 서버에서 진짜 친구(유저) 목록 불러오기
        loadFriendsFromServer(view);
    }

    private void loadFriendsFromServer(View view) {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(friends -> {
            allFriendsList = friends;
            setupRecyclerViews(view);
        });
    }

    private void setupRecyclerViews(View view) {
        List<Friend> upcomingFriends = new ArrayList<>();
        for (Friend f : allFriendsList) {
            if (isUpcomingBirthday(f.getBirthday())) {
                upcomingFriends.add(f);
            }
        }

        // 동기화 리스너
        FriendAdapter.OnRelationChangeListener syncListener = () -> {
            if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
            if (allAdapter != null) allAdapter.notifyDataSetChanged();
        };

        // 선물하기 버튼 리스너
        FriendAdapter.OnGiftButtonClickListener onGiftClick = friend -> {
            if (giftClickListener != null) {
                giftClickListener.onFriendGiftClick(
                        friend.getName(),
                        friend.getRelation(),
                        new ArrayList<>(friend.getInterests()),
                        new ArrayList<>()
                );
            }
        };

        // 상단 생일 섹션
        LinearLayout layoutUpcoming = view.findViewById(R.id.layoutUpcomingSection);
        if (!upcomingFriends.isEmpty()) {
            layoutUpcoming.setVisibility(View.VISIBLE);
            RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcomingBirthdays);
            rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
            upcomingAdapter = new FriendAdapter(upcomingFriends, syncListener, onGiftClick);
            rvUpcoming.setAdapter(upcomingAdapter);
        } else {
            layoutUpcoming.setVisibility(View.GONE);
        }

        // 하단 전체 친구 리스트
        RecyclerView rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        allAdapter = new FriendAdapter(allFriendsList, syncListener, onGiftClick);
        rvFriends.setAdapter(allAdapter);
    }

    private boolean isUpcomingBirthday(String birthday) {
        if (birthday == null || birthday.isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.KOREA);
        try {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            // "YYYY-MM-DD" 형식이면 마지막 "MM-DD"만 추출
            String md = birthday.length() > 5 ? birthday.substring(birthday.length() - 5) : birthday;
            Date birthDate = sdf.parse(md);
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);
            birthCal.set(Calendar.YEAR, now.get(Calendar.YEAR));

            if (birthCal.before(now)) {
                birthCal.add(Calendar.YEAR, 1);
            }

            long diff = birthCal.getTimeInMillis() - now.getTimeInMillis();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            return diffDays >= 0 && diffDays <= 7;
        } catch (ParseException e) {
            return false;
        }
    }
}
