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
        void onFriendGiftClick(String friendUid, String friendName);
    }

    private OnFriendGiftClickListener giftClickListener;

    private FriendAdapter allAdapter;
    private FriendAdapter upcomingAdapter;
    
    private final List<Friend> allFriendsList = new ArrayList<>();
    private final List<Friend> upcomingFriendsList = new ArrayList<>();
    
    private LinearLayout layoutUpcomingSection;

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
    public void onDetach() {
        super.onDetach();
        giftClickListener = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutUpcomingSection = view.findViewById(R.id.layoutUpcomingSection);
        
        // 1) 생일 임박 리사이클러뷰
        RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcomingBirthdays);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 2) 전체 친구 리사이클러뷰
        RecyclerView rvAll = view.findViewById(R.id.rvFriends);
        rvAll.setLayoutManager(new LinearLayoutManager(getContext()));

        FriendAdapter.OnRelationChangeListener relationListener = () -> {
            if (allAdapter != null) allAdapter.notifyDataSetChanged();
            if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
        };

        FriendAdapter.OnGiftButtonClickListener giftListener = friend -> {
            if (giftClickListener != null && friend != null) {
                giftClickListener.onFriendGiftClick(friend.getId(), friend.getName());
            }
        };

        upcomingAdapter = new FriendAdapter(upcomingFriendsList, relationListener, giftListener);
        rvUpcoming.setAdapter(upcomingAdapter);

        allAdapter = new FriendAdapter(allFriendsList, relationListener, giftListener);
        rvAll.setAdapter(allAdapter);

        loadFriends();
    }

    private void loadFriends() {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(new FirebaseManager.OnFriendsLoadedListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                allFriendsList.clear();
                upcomingFriendsList.clear();
                
                if (friends != null) {
                    allFriendsList.addAll(friends);
                    
                    // 🎂 생일 일주일 이내 친구 필터링
                    for (Friend f : friends) {
                        if (isBirthdayUpcoming(f.getBirthday(), 7)) {
                            upcomingFriendsList.add(f);
                        }
                    }
                }
                
                // 섹션 표시 여부
                if (upcomingFriendsList.isEmpty()) {
                    layoutUpcomingSection.setVisibility(View.GONE);
                } else {
                    layoutUpcomingSection.setVisibility(View.VISIBLE);
                }

                if (allAdapter != null) allAdapter.notifyDataSetChanged();
                if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (e != null) e.printStackTrace();
            }
        });
    }

    /**
     * 생일이 오늘로부터 days 이내인지 확인 (년도 무시)
     */
    private boolean isBirthdayUpcoming(String birthdayStr, int days) {
        if (birthdayStr == null || birthdayStr.isEmpty()) return false;
        
        try {
            // "MM/dd" 또는 "yyyy-MM-dd" 등 다양한 형식 대응 필요할 수 있으나, 
            // 일단 숫자만 추출하거나 정해진 포맷이 있다고 가정
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            Date birthDate = sdf.parse(birthdayStr);
            if (birthDate == null) return false;

            Calendar today = Calendar.getInstance();
            Calendar bDay = Calendar.getInstance();
            bDay.setTime(birthDate);
            
            // 올해의 생일로 설정
            bDay.set(Calendar.YEAR, today.get(Calendar.YEAR));
            
            // 만약 올해 생일이 이미 지났다면 내년 생일로 계산 (옵션)
            // 여기서는 단순하게 "현재 시점으로부터 7일 이내"만 체크
            
            long diffMillis = bDay.getTimeInMillis() - today.getTimeInMillis();
            long diffDays = diffMillis / (24 * 60 * 60 * 1000);
            
            // 0일(오늘) ~ 7일 사이
            return diffDays >= 0 && diffDays <= days;
            
        } catch (ParseException e) {
            // 포맷이 다를 경우 처리 (예: "0521" 등)
            return false;
        }
    }
}