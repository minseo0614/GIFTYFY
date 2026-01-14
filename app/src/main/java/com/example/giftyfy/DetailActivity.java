package com.example.giftyfy;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.giftyfy.friend.Friend;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private ImageView imgMain;
    private TextView tvTitle, tvPrice, tvCategory, tvDesc;
    private Button btnGift;
    private ImageButton btnLike;
    private MaterialToolbar toolbar;
    private final DecimalFormat df = new DecimalFormat("#,###");

    private boolean isLiked = false;
    private String productId;
    private String targetFriendUid; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgMain = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvCategory = findViewById(R.id.tv_category);
        tvDesc = findViewById(R.id.tv_desc);
        btnGift = findViewById(R.id.btn_gift);
        btnLike = findViewById(R.id.btn_like);

        productId = getIntent().getStringExtra("productId");
        targetFriendUid = getIntent().getStringExtra("targetFriendUid"); 

        if (productId == null || productId.isEmpty()) {
            finish();
            return;
        }

        fetchProductDetailDirectly(productId);
        checkWishlistStatus();

        btnLike.setOnClickListener(v -> toggleLike());
    }

    private void fetchProductDetailDirectly(String pid) {
        FirebaseFirestore.getInstance()
                .collection("products")
                .document(pid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product p = documentSnapshot.toObject(Product.class);
                        if (p != null) {
                            p.setId(documentSnapshot.getId());
                            updateUI(p);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error", e));
    }

    private void checkWishlistStatus() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseManager.getInstance().getUserByUid(uid, data -> {
            if (data != null) {
                List<String> wishlist = (List<String>) data.get("wishlist");
                if (wishlist != null && wishlist.contains(productId)) {
                    isLiked = true;
                    updateLikeIcon(true);
                } else {
                    isLiked = false;
                    updateLikeIcon(false);
                }
            }
        });
    }

    private void toggleLike() {
        isLiked = !isLiked;
        updateLikeIcon(isLiked);
        FirebaseManager.getInstance().toggleWishlist(productId, isLiked);
        String msg = isLiked ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateLikeIcon(boolean liked) {
        if (btnLike == null) return;
        btnLike.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        if (liked) {
            btnLike.setColorFilter(Color.parseColor("#D080B6"));
        } else {
            btnLike.setColorFilter(Color.parseColor("#555555"));
        }
    }

    private void updateUI(Product p) {
        tvTitle.setText(p.getTitle());
        tvPrice.setText(df.format(p.getPrice()) + "원");
        tvCategory.setText(p.getCategory());
        tvDesc.setText(p.getDescription() != null ? p.getDescription() : "상세 설명이 없습니다.");

        Glide.with(this).load(p.getThumbnail()).into(imgMain);

        if (btnGift != null) {
            btnGift.setOnClickListener(v -> {
                if (targetFriendUid == null || targetFriendUid.isEmpty()) {
                    showFriendSelectPopup(p);
                } else {
                    processGiftAction(targetFriendUid, p.getProductUrl());
                }
            });
        }
    }

    // 친구 선택 팝업창
    private void showFriendSelectPopup(Product product) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_friend_select, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 닫기 버튼
        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        // 리사이클러뷰 설정
        RecyclerView rv = dialogView.findViewById(R.id.rvFriends);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseManager.getInstance().fetchAllUsersAsFriends(new FirebaseManager.OnFriendsLoadedListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                if (friends == null || friends.isEmpty()) {
                    Toast.makeText(DetailActivity.this, "등록된 친구가 없습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                FriendSelectAdapter adapter = new FriendSelectAdapter(friends, selectedFriend -> {
                    processGiftAction(selectedFriend.getId(), product.getProductUrl());
                    Toast.makeText(DetailActivity.this, selectedFriend.getName() + "님에게 선물을 보냈습니다!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
                rv.setAdapter(adapter);
            }
        });

        dialog.show();
    }

    private void processGiftAction(String friendUid, String productUrl) {
        FirebaseManager.getInstance().addReceivedGiftToUser(friendUid, productId);
        if (productUrl != null && !productUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(productUrl));
            startActivity(intent);
        } else {
            Toast.makeText(DetailActivity.this, "상품 링크가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private static class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.VH> {
        private final List<Friend> list;
        private final OnFriendSelectedListener listener;

        interface OnFriendSelectedListener { void onSelected(Friend friend); }

        FriendSelectAdapter(List<Friend> list, OnFriendSelectedListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Friend f = list.get(position);
            holder.tv.setText(f.getName());
            if (f.getProfileUrl() != null && !f.getProfileUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(f.getProfileUrl()).circleCrop().into(holder.iv);
            } else {
                holder.iv.setImageResource(R.drawable.ic_person);
            }
            holder.itemView.setOnClickListener(v -> listener.onSelected(f));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv; ImageView iv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tvName); iv = v.findViewById(R.id.ivProfile); }
        }
    }
}
