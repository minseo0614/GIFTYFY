package com.example.giftyfy;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.friend.Friend;
import com.example.giftyfy.friend.FriendAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendsFragment extends Fragment {

    private FriendAdapter upcomingAdapter;
    private FriendAdapter allAdapter;

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 데이터 생성
        List<Friend> allFriends = new ArrayList<>();
        List<Friend> upcomingFriends = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.KOREA);

        for (int i = 1; i <= 30; i++) {
            String birthday;
            if (i <= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, i);
                birthday = sdf.format(cal.getTime());
            } else {
                birthday = "08-" + (i % 28 + 1);
            }

            Friend friend = new Friend(
                    "친구 " + i,
                    birthday,
                    "미설정",
                    Arrays.asList("취미" + i, "관심사" + (i + 1))
            );

            allFriends.add(friend);
            if (isUpcomingBirthday(birthday)) {
                upcomingFriends.add(friend);
            }
        }

        // 2. 동기화 명령 정의 (이게 실행되면 두 리스트가 모두 새로고침됨)
        Runnable syncAction = () -> {
            if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
            if (allAdapter != null) allAdapter.notifyDataSetChanged();
        };

        // 3. 상단 리스트 설정
        LinearLayout layoutUpcoming = view.findViewById(R.id.layoutUpcomingSection);
        if (!upcomingFriends.isEmpty()) {
            layoutUpcoming.setVisibility(View.VISIBLE);
            RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcomingBirthdays);
            rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
            upcomingAdapter = new FriendAdapter(upcomingFriends, syncAction);
            rvUpcoming.setAdapter(upcomingAdapter);
        }

        // 4. 하단 리스트 설정
        RecyclerView rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        allAdapter = new FriendAdapter(allFriends, syncAction);
        rvFriends.setAdapter(allAdapter);
    }

    private boolean isUpcomingBirthday(String birthday) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.KOREA);
        try {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            Date birthDate = sdf.parse(birthday);
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
