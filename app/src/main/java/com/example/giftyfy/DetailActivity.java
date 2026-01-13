package com.example.giftyfy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
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
    private String targetFriendUid; // ✅ 추가

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
        targetFriendUid = getIntent().getStringExtra("targetFriendUid"); // ✅ 추가

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
                    btnLike.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    isLiked = false;
                    btnLike.setImageResource(R.drawable.ic_heart_outline);
                }
            }
        });
    }

    private void toggleLike() {
        isLiked = !isLiked;
        btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        FirebaseManager.getInstance().toggleWishlist(productId, isLiked);
        String msg = isLiked ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateUI(Product p) {
        tvTitle.setText(p.getTitle());
        tvPrice.setText(df.format(p.getPrice()) + "원");
        tvCategory.setText(p.getCategory());
        tvDesc.setText(p.getDescription() != null ? p.getDescription() : "상세 설명이 없습니다.");

        Glide.with(this).load(p.getThumbnail()).into(imgMain);

        if (btnGift != null) {
            btnGift.setOnClickListener(v -> {
                // ✅ 1. DB 추가 로직 (친구가 지정된 경우)
                if (targetFriendUid != null && !targetFriendUid.isEmpty()) {
                    FirebaseManager.getInstance().addReceivedGiftToUser(targetFriendUid, productId);
                    Toast.makeText(this, "친구의 선물 목록에 추가되었습니다!", Toast.LENGTH_SHORT).show();
                }

                // ✅ 2. 쇼핑몰 이동 로직
                String rawUrl = p.getProductUrl();
                if (rawUrl != null && !rawUrl.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl));
                    startActivity(intent);
                } else {
                    Toast.makeText(DetailActivity.this, "상품 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}