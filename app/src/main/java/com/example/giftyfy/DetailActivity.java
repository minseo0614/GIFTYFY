package com.example.giftyfy;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private boolean liked = false;   // 하트 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // ✅ Toolbar 설정 (뒤로가기 화살표 + 제목 숨김)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");   // 제목 숨김
        }

        // ✅ 전달받은 상품 id
        int id = getIntent().getIntExtra("productId", -1);
        if (id == -1) {
            finish();
            return;
        }

        // ✅ View 연결
        ImageView img = findViewById(R.id.img);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvPrice = findViewById(R.id.tv_price);
        TextView tvCategory = findViewById(R.id.tv_category);
        TextView tvDesc = findViewById(R.id.tv_desc);

        ImageView btnLike = findViewById(R.id.btn_like);
        findViewById(R.id.btn_gift);   // 버튼만 존재 (동작 없음)

        // ❤️ 하트 초기 상태
        btnLike.setImageResource(R.drawable.ic_heart_outline);

        btnLike.setOnClickListener(v -> {
            liked = !liked;
            btnLike.setImageResource(
                    liked ? R.drawable.ic_heart_filled
                            : R.drawable.ic_heart_outline
            );
        });

        // ✅ API 호출
        ProductApi api = ApiClient.get().create(ProductApi.class);
        api.getProductDetail(id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call,
                                   @NonNull Response<Product> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DetailActivity.this,
                            "상세 실패: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Product p = response.body();

                tvTitle.setText(p.title);
                tvPrice.setText(p.price + "원");
                tvCategory.setText(p.category);
                tvDesc.setText(p.description);

                Glide.with(DetailActivity.this)
                        .load(p.thumbnail)
                        .into(img);
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                Toast.makeText(DetailActivity.this,
                        "상세 API 실패: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    // ✅ 뒤로가기 화살표 클릭 처리
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}