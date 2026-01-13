package com.example.giftyfy.friend;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.R;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    public interface OnFriendGiftClickListener {
        void onFriendGiftClick(String friendUid, String friendName);
    }

    private OnFriendGiftClickListener giftClickListener;

    private FriendAdapter adapter;
    private final List<Friend> allFriendsList = new ArrayList<>();

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
    public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        FriendAdapter.OnRelationChangeListener relationListener = () -> {
            if (adapter != null) adapter.notifyDataSetChanged();
        };

        FriendAdapter.OnGiftButtonClickListener giftListener = friend -> {
            if (giftClickListener != null && friend != null) {
                giftClickListener.onFriendGiftClick(friend.getId(), friend.getName());
            }
        };

        adapter = new FriendAdapter(allFriendsList, relationListener, giftListener);
        rvFriends.setAdapter(adapter);

        loadFriends();
    }

    private void loadFriends() {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(new FirebaseManager.OnFriendsLoadedListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                allFriendsList.clear();
                if (friends != null) allFriendsList.addAll(friends);
                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (e != null) e.printStackTrace();
            }
        });
    }
}