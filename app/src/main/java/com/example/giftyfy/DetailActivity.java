package com.example.giftyfy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private ImageView imgMain;
    private TextView tvTitle, tvPrice, tvCategory, tvDesc;
    private Button btnGift;
    private MaterialToolbar toolbar;
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // ✅ 툴바 설정 및 뒤로가기 버튼 활성화
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // 툴바의 뒤로가기 버튼 클릭 이벤트
        toolbar.setNavigationOnClickListener(v -> finish());

        imgMain = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvCategory = findViewById(R.id.tv_category);
        tvDesc = findViewById(R.id.tv_desc);
        btnGift = findViewById(R.id.btn_gift);

        String pid = getIntent().getStringExtra("productId");
        if (pid == null || pid.isEmpty()) {
            finish();
            return;
        }

        fetchProductDetailDirectly(pid);
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

    private void updateUI(Product p) {
        tvTitle.setText(p.getTitle());
        tvPrice.setText(df.format(p.getPrice()) + "원");
        tvCategory.setText(p.getCategory());
        tvDesc.setText(p.getDescription() != null ? p.getDescription() : "상세 설명이 없습니다.");

        Glide.with(this).load(p.getThumbnail()).into(imgMain);

        if (btnGift != null) {
            btnGift.setOnClickListener(v -> {
                String rawUrl = p.getProductUrl();
                if (rawUrl != null && !rawUrl.isEmpty()) {
                    // 실제 기기에서 테스트 완료된 로직 (필요 시 Html.fromHtml 추가 가능)
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl));
                    startActivity(intent);
                } else {
                    Toast.makeText(DetailActivity.this, "상품 링크가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
