package com.example.giftyfy;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgMain;
    private TextView tvTitle, tvPrice, tvCategory, tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // ✅ ID 수정: tv_detail_title -> tv_title 등 XML에 정의된 이름으로 변경
        imgMain = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvCategory = findViewById(R.id.tv_category);
        tvDesc = findViewById(R.id.tv_desc);

        String pid = getIntent().getStringExtra("productId");
        if (pid == null) {
            finish();
            return;
        }

        fetchProductDetail(pid);
    }

    private void fetchProductDetail(String pid) {
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                Product target = null;
                for (Product p : products) {
                    if (p.getId().equals(pid)) {
                        target = p;
                        break;
                    }
                }

                if (target != null) {
                    updateUI(target);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DetailActivity.this, "상세 정보 로딩 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Product p) {
        tvTitle.setText(p.getTitle());
        tvPrice.setText(p.getPrice() + "원");
        tvCategory.setText(p.getCategory());
        tvDesc.setText(p.getDescription());

        Glide.with(this)
                .load(p.getThumbnail())
                .into(imgMain);
    }
}
