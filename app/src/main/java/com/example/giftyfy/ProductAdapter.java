package com.example.giftyfy;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    private final List<Product> items;

    public ProductAdapter(List<Product> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);

        h.tvTitle.setText(p.title);
        h.tvPrice.setText(p.price + "원");

        Glide.with(h.itemView.getContext())
                .load(p.thumbnail)
                .into(h.imgThumb);

        // ❤️ 하트 상태 표시
        h.btnWish.setImageResource(
                p.wish ? R.drawable.ic_heart_filled
                        : R.drawable.ic_heart_outline
        );

        // ❤️ 하트 클릭 → 토글만
        h.btnWish.setOnClickListener(v -> {
            p.wish = !p.wish;   // 상태 반전
            notifyItemChanged(h.getAdapterPosition());
        });

        // 📄 카드 클릭 → 상세 화면 이동
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), DetailActivity.class);
            i.putExtra("productId", p.id);
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvPrice;
        ImageButton btnWish;

        VH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle  = itemView.findViewById(R.id.tv_title);
            tvPrice  = itemView.findViewById(R.id.tv_price);
            btnWish  = itemView.findViewById(R.id.btn_wish);
        }
    }
}