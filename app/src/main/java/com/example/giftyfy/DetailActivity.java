package com.example.giftyfy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private ImageView imgMain;
    private TextView tvTitle, tvPrice, tvCategory, tvDesc;
    private Button btnGift;
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgMain = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvCategory = findViewById(R.id.tv_category);
        tvDesc = findViewById(R.id.tv_desc);
        btnGift = findViewById(R.id.btn_gift);

        String pid = getIntent().getStringExtra("productId");
        if (pid == null) {
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
                });
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
                    // 1. HTML 엔티티 제거
                    String decodedUrl = Html.fromHtml(rawUrl).toString();
                    
                    // 2. 만약 네이버 게이트웨이 주소라면 모바일 파라미터 강제 추가 시도
                    // (일부 링크는 m=1 또는 m=true 파라미터를 통해 모바일 강제 리다이렉트가 가능함)
                    if (decodedUrl.contains("gate.nhn") && !decodedUrl.contains("m=")) {
                        decodedUrl += "&m=true";
                    }

                    Log.d(TAG, "최종 시도 URL: " + decodedUrl);

                    try {
                        // 기본 브라우저 대신 인텐트 플래그를 설정하여 새 창으로 열기
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(decodedUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        
                        // 특정 브라우저(Chrome) 패키지를 명시적으로 지정해볼 수 있음 (에뮬레이터에 크롬이 있는 경우)
                        intent.setPackage("com.android.chrome"); 
                        
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            // 크롬이 없으면 다시 일반 호출
                            intent.setPackage(null);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "최종 연결 실패", e);
                        Toast.makeText(DetailActivity.this, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "링크가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
