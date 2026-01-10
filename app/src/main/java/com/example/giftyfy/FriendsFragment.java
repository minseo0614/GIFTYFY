package com.example.giftyfy;

import android.content.Context;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FriendsFragment extends Fragment {

    public interface OnFriendGiftClickListener {
        void onFriendGiftClick(String friendName,
                               String relation,
                               ArrayList<String> interests,
                               ArrayList<String> receivedTitles);
    }

    private OnFriendGiftClickListener listener;

    private FriendAdapter upcomingAdapter;
    private FriendAdapter allAdapter;

    private final List<String> availableTags = Arrays.asList(
            "디저트러버", "애주가", "상품권애호가", "카페돌이", "고기진심러",
            "빵지순례자", "편의점단골", "배달앱VIP", "집순이", "집돌이",
            "향기컬렉터", "캠핑매니아", "프로직장인", "운동매니아", "영양제신봉자",
            "피부관리진심러", "다이어터", "귀여운게최고", "댕냥이집사", "독서가",
            "게임덕후", "보드게이머", "러닝크루", "요리꿈나무", "패션피플"
    );

    // 테스트용 받은 선물 풀
    private final List<String> dummyReceivedPool = Arrays.asList(
            "Perfume", "Skincare Set", "Chocolate Box", "Coffee Machine",
            "Headphones", "Backpack", "Lipstick", "Smart Watch"
    );

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFriendGiftClickListener) {
            listener = (OnFriendGiftClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Friend> allFriends = new ArrayList<>();
        List<Friend> upcomingFriends = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.KOREA);
        Random random = new Random();

        for (int i = 1; i <= 30; i++) {
            String birthday;
            if (i <= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, i);
                birthday = sdf.format(cal.getTime());
            } else {
                birthday = String.format(Locale.KOREA, "%02d-%02d",
                        random.nextInt(12) + 1, random.nextInt(28) + 1);
            }

            List<String> shuffledTags = new ArrayList<>(availableTags);
            Collections.shuffle(shuffledTags);

            int tagCount = 2 + random.nextInt(2); // 2~3개
            List<String> selectedTags = new ArrayList<>(shuffledTags.subList(0, tagCount));

            Friend friend = new Friend(
                    "친구 " + i,
                    birthday,
                    "미설정",
                    selectedTags
            );

            allFriends.add(friend);
            if (isUpcomingBirthday(birthday)) upcomingFriends.add(friend);
        }

        Runnable syncAction = () -> {
            if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
            if (allAdapter != null) allAdapter.notifyDataSetChanged();
        };

        FriendAdapter.OnGiftClickListener onGiftClick = friend -> {
            if (listener == null) return;

            ArrayList<String> interests = new ArrayList<>(friend.getInterests());

            ArrayList<String> receivedTitles = new ArrayList<>();
            List<String> pool = new ArrayList<>(dummyReceivedPool);
            Collections.shuffle(pool);
            int n = 2 + random.nextInt(2);
            receivedTitles.addAll(pool.subList(0, n));

            listener.onFriendGiftClick(
                    friend.getName(),
                    friend.getRelation(),
                    interests,
                    receivedTitles
            );
        };

        LinearLayout layoutUpcoming = view.findViewById(R.id.layoutUpcomingSection);
        if (!upcomingFriends.isEmpty()) {
            layoutUpcoming.setVisibility(View.VISIBLE);
            RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcomingBirthdays);
            rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
            upcomingAdapter = new FriendAdapter(upcomingFriends, syncAction, onGiftClick);
            rvUpcoming.setAdapter(upcomingAdapter);
        } else {
            layoutUpcoming.setVisibility(View.GONE);
        }

        RecyclerView rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        allAdapter = new FriendAdapter(allFriends, syncAction, onGiftClick);
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

            if (birthCal.before(now)) birthCal.add(Calendar.YEAR, 1);

            long diff = birthCal.getTimeInMillis() - now.getTimeInMillis();
            long diffDays = diff / (24L * 60 * 60 * 1000);

            return diffDays >= 0 && diffDays <= 7;
        } catch (ParseException e) {
            return false;
        }
    }
}