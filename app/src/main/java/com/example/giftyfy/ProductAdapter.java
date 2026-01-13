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

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    private final List<Product> items = new ArrayList<>();

    // ✅ GiftsFragment에서 이걸로 리스트 넣기
    public void setItems(List<Product> products) {
        items.clear();
        if (products != null) items.addAll(products);
        notifyDataSetChanged();
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

        h.tvTitle.setText(p.getTitle() == null ? "" : p.getTitle());
        h.tvPrice.setText(p.getPrice() + "원");

        // ✅ Firestore 필드: thumbnail
        String thumb = p.getThumbnail();

        Glide.with(h.itemView.getContext())
                .load(thumb)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground) // 너 프로젝트에 있는 drawable로 바꿔도 됨
                .error(R.drawable.ic_launcher_foreground)
                .into(h.imgThumb);

        // ✅ 하트 버튼은 일단 기능 안 쓰면 숨겨도 됨(원하면 제거 가능)
        if (h.btnWish != null) h.btnWish.setVisibility(View.GONE);

        // (선택) 카드 클릭 -> 상세 화면
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

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvPrice;
        ImageButton btnWish;

        VH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle  = itemView.findViewById(R.id.tv_title);
            tvPrice  = itemView.findViewById(R.id.tv_price);
            btnWish  = itemView.findViewById(R.id.btn_wish); // xml에 있으니까 일단 찾아둠
        }
    }
}