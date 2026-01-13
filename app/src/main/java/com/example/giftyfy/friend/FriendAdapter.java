package com.example.giftyfy.friend;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giftyfy.DetailActivity;
import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.Product;
import com.example.giftyfy.R;
import com.example.giftyfy.Recommender;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<Friend> friendList;
    private final OnRelationChangeListener relationListener;
    private final OnGiftButtonClickListener giftListener;

    public interface OnRelationChangeListener {
        void onRelationChanged();
    }

    public interface OnGiftButtonClickListener {
        void onGiftClick(Friend friend);
    }

    public FriendAdapter(List<Friend> friendList,
                         OnRelationChangeListener relationListener,
                         OnGiftButtonClickListener giftListener) {
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

        String relation = friend.getRelation();
        holder.tvRelation.setText((relation == null || relation.isEmpty()) ? "미설정" : relation);

        StringBuilder interestsText = new StringBuilder();
        if (friend.getInterests() != null) {
            for (String interest : friend.getInterests()) {
                interestsText.append("#").append(interest).append(" ");
            }
        }
        holder.tvInterests.setText(interestsText.toString().trim());

        // 펼침/접힘 및 추천 데이터 로드
        if (friend.isExpanded()) {
            holder.layoutExpandable.setVisibility(View.VISIBLE);
            loadRecommendationsForFriend(holder, friend);
        } else {
            holder.layoutExpandable.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            boolean nextState = !friend.isExpanded();
            friend.setExpanded(nextState);
            notifyItemChanged(position);
        });

        holder.btnGoToGift.setOnClickListener(v -> {
            if (giftListener != null) {
                giftListener.onGiftClick(friend);
            }
        });

        View.OnClickListener tagClickListener = v -> {
            String newRelation = ((TextView) v).getText().toString();
            friend.setRelation(newRelation);
            holder.tvRelation.setText(newRelation);

            if (friend.getId() != null && !friend.getId().isEmpty()) {
                FirebaseManager.getInstance().updateFriendRelation(friend.getId(), newRelation);
                // 관계가 바뀌면 추천 목록도 갱신되어야 함
                notifyItemChanged(position);
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

    private void loadRecommendationsForFriend(FriendViewHolder holder, Friend friend) {
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                String relation = friend.getRelation() != null ? friend.getRelation() : "미설정";
                List<String> interests = friend.getInterests() != null ? friend.getInterests() : new ArrayList<>();
                
                List<Product> top3 = Recommender.topN(products, relation, interests, 3);
                
                holder.layoutGiftList.removeAllViews();
                for (Product p : top3) {
                    View itemView = LayoutInflater.from(holder.itemView.getContext())
                            .inflate(R.layout.item_gift_recommend, holder.layoutGiftList, false);
                    
                    ImageView iv = itemView.findViewById(R.id.ivGiftImage);
                    TextView tv = itemView.findViewById(R.id.tvGiftName);
                    
                    tv.setText(p.getTitle());
                    Glide.with(itemView.getContext()).load(p.getThumbnail()).into(iv);

                    // 추천 아이템 클릭 시 상세 화면 이동
                    itemView.setOnClickListener(v -> {
                        Intent i = new Intent(v.getContext(), DetailActivity.class);
                        i.putExtra("productId", p.getId());
                        i.putExtra("targetFriendUid", friend.getId()); // 친구 ID 전달
                        v.getContext().startActivity(i);
                    });

                    holder.layoutGiftList.addView(itemView);
                }
            }

            @Override
            public void onError(Exception e) {
                holder.layoutGiftList.removeAllViews();
            }
        });
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
        LinearLayout layoutGiftList; // ✅ 추가

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvBirthday = itemView.findViewById(R.id.tvBirthday);
            tvRelation = itemView.findViewById(R.id.tvRelation);
            tvInterests = itemView.findViewById(R.id.tvInterests);

            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
            btnGoToGift = itemView.findViewById(R.id.btnGoToGift);
            layoutGiftList = itemView.findViewById(R.id.layoutGiftList); // ✅ 추가

            tagFamily = itemView.findViewById(R.id.tagFamily);
            tagFriend = itemView.findViewById(R.id.tagFriend);
            tagLove = itemView.findViewById(R.id.tagLove);
            tagWork = itemView.findViewById(R.id.tagWork);
            tagAwkward = itemView.findViewById(R.id.tagAwkward);
        }
    }
}