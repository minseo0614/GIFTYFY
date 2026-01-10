package com.example.giftyfy.friend;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.R;

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

    private FriendAdapter upcomingAdapter;
    private FriendAdapter allAdapter;

    // 사용자님이 요청하신 태그 리스트
    private final List<String> availableTags = Arrays.asList(
            "디저트러버", "애주가", "상품권애호가", "카페돌이", "고기진심러",
            "빵지순례자", "편의점단골", "배달앱VIP", "집순이", "집돌이",
            "향기컬렉터", "캠핑매니아", "프로직장인", "운동매니아", "영양제신봉자",
            "피부관리진심러", "다이어터", "귀여운게최고", "댕냥이집사", "독서가",
            "게임덕후", "보드게이머", "러닝크루", "요리꿈나무", "패션피플"
    );

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
        Random random = new Random();

        for (int i = 1; i <= 30; i++) {
            String birthday;
            if (i <= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, i);
                birthday = sdf.format(cal.getTime());
            } else {
                // 생일도 랜덤하게 생성 (월: 1~12, 일: 1~28)
                birthday = String.format(Locale.KOREA, "%02d-%02d", 
                        random.nextInt(12) + 1, random.nextInt(28) + 1);
            }

            // [태그 랜덤 선택 로직]
            // 전체 리스트를 복사해서 무작위로 섞음
            List<String> shuffledTags = new ArrayList<>(availableTags);
            Collections.shuffle(shuffledTags);
            
            // 2개 또는 3개 선택
            int tagCount = 2 + random.nextInt(2); 
            List<String> selectedTags = new ArrayList<>(shuffledTags.subList(0, tagCount));

            Friend friend = new Friend(
                    "친구 " + i,
                    birthday,
                    "미설정",
                    selectedTags
            );

            allFriends.add(friend);
            if (isUpcomingBirthday(birthday)) {
                upcomingFriends.add(friend);
            }
        }

        // 2. 동기화 명령 정의
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
        } else {
            layoutUpcoming.setVisibility(View.GONE);
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
