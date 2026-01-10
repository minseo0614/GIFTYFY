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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GiftsMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VT_HEADER = 0;
    private static final int VT_ITEM = 1;

    private final boolean fromFriend;
    private final String friendName;

    private final Set<String> receivedSet = new HashSet<>();

    private final List<Product> rec = new ArrayList<>();
    private final List<Product> rest = new ArrayList<>();

    public GiftsMixAdapter(boolean fromFriend, String friendName, List<String> receivedTitles) {
        this.fromFriend = fromFriend;
        this.friendName = (friendName == null) ? "" : friendName;

        if (receivedTitles != null) receivedSet.addAll(receivedTitles);
    }

    private boolean hasHeader() {
        return fromFriend && !friendName.isEmpty();
    }

    public void setData(List<Product> recommended, List<Product> all) {
        rec.clear();
        rest.clear();

        if (recommended != null) rec.addAll(recommended);

        Set<Integer> recIds = new HashSet<>();
        for (Product p : rec) recIds.add(p.id);

        if (all != null) {
            for (Product p : all) {
                if (!recIds.contains(p.id)) rest.add(p);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader() && position == 0) return VT_HEADER;
        return VT_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());

        if (viewType == VT_HEADER) {
            View v = inf.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_product, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VT_HEADER) {
            HeaderVH h = (HeaderVH) holder;
            h.title.setText(friendName + " 추천 선물");
            return;
        }

        int headerOffset = hasHeader() ? 1 : 0;
        int idx = position - headerOffset;

        final Product p;
        final boolean isRec = idx < rec.size();

        if (isRec) {
            p = rec.get(idx);
        } else {
            p = rest.get(idx - rec.size());
        }

        ItemVH h = (ItemVH) holder;

        h.tvTitle.setText(p.title);
        h.tvPrice.setText(p.price + "원");

        Glide.with(h.itemView.getContext())
                .load(p.thumbnail)
                .into(h.imgThumb);

        h.btnWish.setImageResource(
                p.wish ? R.drawable.ic_heart_filled
                        : R.drawable.ic_heart_outline
        );

        h.btnWish.setOnClickListener(v -> {
            p.wish = !p.wish;
            notifyItemChanged(h.getAdapterPosition());
        });

        if (receivedSet.contains(p.title)) {
            h.itemView.setAlpha(0.45f);
            h.itemView.setBackgroundColor(0xFFF2F2F2);
        } else {
            h.itemView.setAlpha(1f);
            h.itemView.setBackgroundColor(0x00000000);
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), DetailActivity.class);
            i.putExtra("productId", p.id);
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        int header = hasHeader() ? 1 : 0;
        return header + rec.size() + rest.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView title;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            title.setTextSize(16);
            title.setPadding(24, 16, 24, 16);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvPrice;
        ImageButton btnWish;

        ItemVH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle  = itemView.findViewById(R.id.tv_title);
            tvPrice  = itemView.findViewById(R.id.tv_price);
            btnWish  = itemView.findViewById(R.id.btn_wish);
        }
    }
}