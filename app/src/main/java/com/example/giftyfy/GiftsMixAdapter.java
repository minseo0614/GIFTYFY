package com.example.giftyfy;

import android.content.Intent;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GiftsMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final boolean fromFriend;
    private final String friendName;
    private final Set<String> receivedSet;

    private final List<Product> recommended = new ArrayList<>();
    private final List<Product> allRemaining = new ArrayList<>();
    
    // ✅ 가격 포맷 설정을 위한 DecimalFormat 추가
    private final DecimalFormat df = new DecimalFormat("#,###");

    public GiftsMixAdapter(boolean fromFriend, String friendName, List<String> received) {
        this.fromFriend = fromFriend;
        this.friendName = friendName;
        this.receivedSet = new HashSet<>(received);
    }

    public void setData(List<Product> rec, List<Product> all) {
        recommended.clear();
        recommended.addAll(rec);

        Set<String> recIds = new HashSet<>();
        for (Product p : rec) recIds.add(p.getId());

        allRemaining.clear();
        for (Product p : all) {
            if (!recIds.contains(p.getId())) allRemaining.add(p);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (fromFriend && position == 0) return 0; // Header
        int actualPos = fromFriend ? position - 1 : position;
        if (actualPos < recommended.size()) return 1; // Rec
        return 2; // Normal
    }

    @Override
    public int getItemCount() {
        int count = recommended.size() + allRemaining.size();
        if (fromFriend) count++;
        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gifts_recommend_header, parent, false);
            return new HeaderVH(v);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tv.setText(friendName + "님을 위한 선물 추천");
            return;
        }

        ProductVH h = (ProductVH) holder;
        int actualPos = fromFriend ? position - 1 : position;
        Product p = (actualPos < recommended.size()) ? recommended.get(actualPos) : allRemaining.get(actualPos - recommended.size());

        h.tvTitle.setText(p.getTitle());
        
        // ✅ .0 제거 및 콤마 추가 적용
        h.tvPrice.setText(df.format(p.getPrice()) + "원");

        Glide.with(h.itemView.getContext())
                .load(p.getThumbnail())
                .into(h.imgThumb);

        h.btnWish.setImageResource(
                p.isWish() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
        );

        h.btnWish.setOnClickListener(v -> {
            p.setWish(!p.isWish());
            notifyItemChanged(position);
        });

        if (receivedSet.contains(p.getTitle())) {
            h.tvTitle.setTextColor(Color.GRAY);
            h.tvTitle.setText("[이미 받은] " + p.getTitle());
        } else {
            h.tvTitle.setTextColor(Color.BLACK);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), DetailActivity.class);
            i.putExtra("productId", p.getId());
            v.getContext().startActivity(i);
        });
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(View v) { 
            super(v); 
            tv = v.findViewById(R.id.tv_bar); 
        }
    }

    static class ProductVH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvPrice;
        ImageButton btnWish;
        ProductVH(View v) {
            super(v);
            imgThumb = v.findViewById(R.id.img_thumb);
            tvTitle = v.findViewById(R.id.tv_title);
            tvPrice = v.findViewById(R.id.tv_price);
            btnWish = v.findViewById(R.id.btn_wish);
        }
    }
}
