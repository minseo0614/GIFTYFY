package com.example.giftyfy.friend;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giftyfy.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.VH> {

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    private final List<Friend> items;
    private final OnFriendClickListener clickListener;
    private final boolean isUpcomingStyle;

    public FriendAdapter(List<Friend> items, boolean isUpcomingStyle, OnFriendClickListener clickListener) {
        this.items = items;
        this.isUpcomingStyle = isUpcomingStyle;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isUpcomingStyle ? R.layout.item_friend_upcoming : R.layout.item_friend;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Friend f = items.get(position);
        if (f == null) return;

        h.tvName.setText(f.getName());

        String rawBirthday = f.getBirthday();
        if (rawBirthday != null && rawBirthday.contains("-")) {
            String[] parts = rawBirthday.split("-");
            if (parts.length >= 3) {
                h.tvBirthday.setText(Integer.parseInt(parts[1]) + "월 " + Integer.parseInt(parts[2]) + "일");
            } else if (parts.length == 2) {
                h.tvBirthday.setText(Integer.parseInt(parts[0]) + "월 " + Integer.parseInt(parts[1]) + "일");
            } else {
                h.tvBirthday.setText(rawBirthday);
            }
        } else {
            h.tvBirthday.setText(rawBirthday);
        }

        if (h.tvRelation != null) {
            String rel = f.getRelation();
            if (rel == null || rel.isEmpty() || rel.equals("미설정")) {
                h.tvRelation.setText("미설정");
                h.tvRelation.setBackgroundResource(R.drawable.bg_pill_soft); 
                h.tvRelation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
                h.tvRelation.setTextColor(Color.BLACK);      
            } else {
                h.tvRelation.setText(rel);
                h.tvRelation.setBackgroundResource(R.drawable.bg_pill_soft);
                h.tvRelation.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FBF7F9")));
                h.tvRelation.setTextColor(Color.BLACK);
            }
        }

        // ✅ [해결] 프로필 사진을 ic_person 기본 아이콘으로 통일
        if (h.ivProfile != null) {
            if (f.getProfileUrl() != null && !f.getProfileUrl().isEmpty()) {
                Glide.with(h.itemView.getContext())
                        .load(f.getProfileUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(h.ivProfile);
            } else {
                // ✅ 프로필이 없으면 ic_person 표시 + 부드러운 배경
                h.ivProfile.setImageResource(R.drawable.ic_person); 
                h.ivProfile.setBackgroundResource(R.drawable.bg_pill_soft);
                h.ivProfile.setPadding(10, 10, 10, 10);
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onFriendClick(f);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvBirthday, tvRelation;

        VH(@NonNull View v) {
            super(v);
            ivProfile = v.findViewById(R.id.ivProfile);
            tvName = v.findViewById(R.id.tvName);
            tvBirthday = v.findViewById(R.id.tvBirthday);
            tvRelation = v.findViewById(R.id.tvRelation);
        }
    }
}
