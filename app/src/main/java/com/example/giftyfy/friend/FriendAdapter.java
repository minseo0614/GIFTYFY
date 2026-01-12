package com.example.giftyfy.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<Friend> friendList;
    private OnRelationChangeListener relationListener;
    private OnGiftButtonClickListener giftListener;

    public interface OnRelationChangeListener {
        void onRelationChanged();
    }

    public interface OnGiftButtonClickListener {
        void onGiftClick(Friend friend);
    }

    public FriendAdapter(List<Friend> friendList, OnRelationChangeListener relationListener, OnGiftButtonClickListener giftListener) {
        this.friendList = friendList;
        this.relationListener = relationListener;
        this.giftListener = giftListener;
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
        
        // 관계가 없으면 "미설정"으로 표시
        String relation = friend.getRelation();
        holder.tvRelation.setText((relation == null || relation.isEmpty()) ? "미설정" : relation);

        StringBuilder interestsText = new StringBuilder();
        if (friend.getInterests() != null) {
            for (String interest : friend.getInterests()) {
                interestsText.append("#").append(interest).append(" ");
            }
        }
        holder.tvInterests.setText(interestsText.toString().trim());

        holder.layoutExpandable.setVisibility(friend.isExpanded() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            boolean nextState = !friend.isExpanded();
            friend.setExpanded(nextState);
            holder.layoutExpandable.setVisibility(nextState ? View.VISIBLE : View.GONE);
        });

        holder.btnGoToGift.setOnClickListener(v -> {
            if (giftListener != null) {
                giftListener.onGiftClick(friend);
            }
        });

        View.OnClickListener tagClickListener = v -> {
            String newRelation = ((TextView) v).getText().toString();
            friend.setRelation(newRelation);
            
            // ✅ [수정] 친구 이름 대신 고유 ID(friend.getId())를 사용하여 저장합니다.
            if (friend.getId() != null) {
                FirebaseManager.getInstance().updateFriendRelation(friend.getId(), newRelation);
            }
            
            if (relationListener != null) {
                relationListener.onRelationChanged();
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
        return friendList == null ? 0 : friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBirthday, tvRelation, tvInterests;
        TextView tagFamily, tagFriend, tagLove, tagWork, tagAwkward;
        View layoutExpandable;
        View btnGoToGift;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBirthday = itemView.findViewById(R.id.tvBirthday);
            tvRelation = itemView.findViewById(R.id.tvRelation);
            tvInterests = itemView.findViewById(R.id.tvInterests);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
            btnGoToGift = itemView.findViewById(R.id.btnGoToGift);
            
            tagFamily = itemView.findViewById(R.id.tagFamily);
            tagFriend = itemView.findViewById(R.id.tagFriend);
            tagLove = itemView.findViewById(R.id.tagLove);
            tagWork = itemView.findViewById(R.id.tagWork);
            tagAwkward = itemView.findViewById(R.id.tagAwkward);
        }
    }
}
