package com.example.giftyfy.friend;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.R;
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

    // ✅ 에러 해결을 위한 인터페이스 정의 (MainActivity에서 구현 중인 것)
    public interface OnFriendGiftClickListener {
        void onFriendGiftClick(String friendName, String relation, ArrayList<String> interests, ArrayList<String> receivedTitles);
    }

    private OnFriendGiftClickListener giftClickListener;
    private FriendAdapter upcomingAdapter;
    private FriendAdapter allAdapter;

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // MainActivity가 이 인터페이스를 구현하고 있는지 확인하고 연결합니다.
        if (context instanceof OnFriendGiftClickListener) {
            giftClickListener = (OnFriendGiftClickListener) context;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 데이터 생성 및 분류 로직 (기존과 동일)
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
                    new ArrayList<>(Arrays.asList("취미" + i, "관심사" + (i + 1)))
            );

            allFriends.add(friend);
            if (isUpcomingBirthday(birthday)) {
                upcomingFriends.add(friend);
            }
        }

        // 동기화 리스너
        FriendAdapter.OnRelationChangeListener syncListener = () -> {
            if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
            if (allAdapter != null) allAdapter.notifyDataSetChanged();
        };

        // 🎁 선물하기 클릭 리스너 (어댑터에 전달할 용도)
        FriendAdapter.OnGiftButtonClickListener onGiftClick = friend -> {
            if (giftClickListener != null) {
                // MainActivity의 onFriendGiftClick 호출
                giftClickListener.onFriendGiftClick(
                        friend.getName(),
                        friend.getRelation(),
                        new ArrayList<>(friend.getInterests()),
                        new ArrayList<>() // 아직 받은 선물 목록은 비어있음
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
        }

        // 하단 전체 친구 리스트
        RecyclerView rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        allAdapter = new FriendAdapter(allFriends, syncListener, onGiftClick);
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
