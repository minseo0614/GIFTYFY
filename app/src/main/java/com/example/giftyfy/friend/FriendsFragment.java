package com.example.giftyfy.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.MainActivity;
import com.example.giftyfy.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendsFragment extends Fragment {

    private RecyclerView rvFriends;
    private RecyclerView rvUpcoming;

    private final List<Friend> friendList = new ArrayList<>();
    private final List<Friend> upcomingFriendList = new ArrayList<>();
    private FriendAdapter allAdapter;
    private FriendAdapter upcomingAdapter;

    public FriendsFragment() {
        super(R.layout.fragment_friends);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFriends = view.findViewById(R.id.rvFriends);
        rvUpcoming = view.findViewById(R.id.rvUpcomingBirthdays);

        if (rvFriends != null) {
            rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (rvUpcoming != null) {
            rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        FriendAdapter.OnFriendClickListener clickListener = f -> {
            if (f != null) openFriendSheet(f.getId());
        };

        allAdapter = new FriendAdapter(friendList, false, clickListener);
        upcomingAdapter = new FriendAdapter(upcomingFriendList, true, clickListener);

        if (rvFriends != null) rvFriends.setAdapter(allAdapter);
        if (rvUpcoming != null) rvUpcoming.setAdapter(upcomingAdapter);

        loadMyFriends();
    }

    private void openFriendSheet(String uid) {
        FriendBottomSheetDialogFragment sheet = FriendBottomSheetDialogFragment.newInstance(uid);
        
        //1. 선물 탭 이동 리스너
        sheet.setOnGoGiftListener((friendUid, name) -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onFriendGiftClick(friendUid, name);
            }
        });

        //2. 관계 변경 시 목록 즉시 새로고침 리스너 추가
        sheet.setOnRelationChangedListener(() -> {
            Log.d("FriendsFragment", "Relation changed, reloading...");
            loadMyFriends();
        });

        sheet.show(getParentFragmentManager(), "friend_sheet");
    }

    private void loadMyFriends() {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(new FirebaseManager.OnFriendsLoadedListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                if (!isAdded()) return;

                friendList.clear();
                upcomingFriendList.clear();

                if (friends != null) {
                    for (Friend f : friends) {
                        friendList.add(f);
                        if (isBirthdayUpcoming(f.getBirthday(), 7)) {
                            upcomingFriendList.add(f);
                        }
                    }
                }

                if (allAdapter != null) allAdapter.notifyDataSetChanged();
                if (upcomingAdapter != null) upcomingAdapter.notifyDataSetChanged();
                
                View v = getView();
                if (v != null) {
                    View section = v.findViewById(R.id.layoutUpcomingSection);
                    if (section != null) section.setVisibility(upcomingFriendList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("FriendsFragment", "친구 로드 실패", e);
            }
        });
    }

    private boolean isBirthdayUpcoming(String bdayStr, int days) {
        if (bdayStr == null || bdayStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            Date date = sdf.parse(bdayStr);
            if (date == null) return false;

            Calendar today = Calendar.getInstance();
            Calendar bDay = Calendar.getInstance();
            bDay.setTime(date);
            bDay.set(Calendar.YEAR, today.get(Calendar.YEAR));

            if (bDay.before(today)) {
                bDay.add(Calendar.YEAR, 1);
            }

            long diff = bDay.getTimeInMillis() - today.getTimeInMillis();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            return diffDays >= 0 && diffDays <= days;
        } catch (ParseException e) {
            return false;
        }
    }
}
