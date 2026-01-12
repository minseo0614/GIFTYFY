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

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    private final List<Product> items;
    // ✅ 천 단위 콤마와 소수점 제거를 위한 포맷 설정
    private final DecimalFormat df = new DecimalFormat("#,###");

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

        h.tvTitle.setText(p.getTitle());
        
        // ✅ 1000.0 대신 1,000원으로 표시되도록 수정
        h.tvPrice.setText(df.format(p.getPrice()) + "원");

        Glide.with(h.itemView.getContext())
                .load(p.getThumbnail())
                .into(h.imgThumb);

        h.btnWish.setImageResource(
                p.isWish() ? R.drawable.ic_heart_filled
                        : R.drawable.ic_heart_outline
        );

        h.btnWish.setOnClickListener(v -> {
            p.setWish(!p.isWish());
            notifyItemChanged(h.getAdapterPosition());
        });

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), DetailActivity.class);
            i.putExtra("productId", p.getId());
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
