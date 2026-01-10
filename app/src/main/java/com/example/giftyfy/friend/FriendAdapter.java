package com.example.giftyfy.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<Friend> friendList;
    private Runnable onDataChanged; // 데이터 변경 시 실행할 작업

    public FriendAdapter(List<Friend> friendList, Runnable onDataChanged) {
        this.friendList = friendList;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.tvName.setText(friend.getName());
        holder.tvBirthday.setText(friend.getBirthday());
        holder.tvRelation.setText(friend.getRelation());

        StringBuilder interestsText = new StringBuilder();
        for (String interest : friend.getInterests()) {
            interestsText.append("#").append(interest).append(" ");
        }
        holder.tvInterests.setText(interestsText.toString().trim());

        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutExpandable.getVisibility() == View.GONE) {
                holder.layoutExpandable.setVisibility(View.VISIBLE);
            } else {
                holder.layoutExpandable.setVisibility(View.GONE);
            }
        });

        View.OnClickListener tagClickListener = v -> {
            String newRelation = ((TextView) v).getText().toString();
            friend.setRelation(newRelation); // 데이터(객체) 업데이트
            
            // 데이터가 바뀌었으니 알림
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        };

        holder.tagFamily.setOnClickListener(tagClickListener);
        holder.tagFriend.setOnClickListener(tagClickListener);
        holder.tagLove.setOnClickListener(tagClickListener);
        holder.tagWork.setOnClickListener(tagClickListener);
        holder.tagAwkward.setOnClickListener(tagClickListener);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBirthday, tvRelation, tvInterests;
        TextView tagFamily, tagFriend, tagLove, tagWork, tagAwkward;
        View layoutExpandable;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBirthday = itemView.findViewById(R.id.tvBirthday);
            tvRelation = itemView.findViewById(R.id.tvRelation);
            tvInterests = itemView.findViewById(R.id.tvInterests);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
            
            tagFamily = itemView.findViewById(R.id.tagFamily);
            tagFriend = itemView.findViewById(R.id.tagFriend);
            tagLove = itemView.findViewById(R.id.tagLove);
            tagWork = itemView.findViewById(R.id.tagWork);
            tagAwkward = itemView.findViewById(R.id.tagAwkward);
        }
    }
}
