package com.example.giftyfy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GiftsMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PRODUCT = 1;

    private final List<Row> rows = new ArrayList<>();
    private final List<String> receivedGiftIds = new ArrayList<>();
    private final List<String> wishlistIds = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,###");

    private String targetFriendUid = null;

    public void setTargetFriendUid(String uid) {
        this.targetFriendUid = uid;
    }

    public void setReceivedGiftIds(List<String> ids) {
        this.receivedGiftIds.clear();
        if (ids != null) this.receivedGiftIds.addAll(ids);
        notifyDataSetChanged();
    }

    public void setWishlistIds(List<String> ids) {
        this.wishlistIds.clear();
        if (ids != null) this.wishlistIds.addAll(ids);
        notifyDataSetChanged();
    }

    public void setModeDefault(List<Product> all) {
        rows.clear();
        rows.add(Row.header("전체 선물"));
        if (all != null) {
            for (Product p : all) rows.add(Row.product(p));
        }
        notifyDataSetChanged();
    }

    public void setModeFriend(List<Product> recommended, List<Product> all) {
        rows.clear();
        if (recommended != null) {
            for (Product p : recommended) rows.add(Row.product(p));
        }
        rows.add(Row.header("전체 선물"));
        if (all != null) {
            for (Product p : all) rows.add(Row.product(p));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? TYPE_HEADER : TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header_simple, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_product, parent, false);
            return new ProductVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tv.setText(row.headerText);
            return;
        }

        ProductVH h = (ProductVH) holder;
        Product p = row.product;

        h.tvTitle.setText(p.getTitle() == null ? "" : p.getTitle());
        h.tvPrice.setText(df.format(p.getPrice()) + "원");

        if (h.tvCategory != null) {
            h.tvCategory.setText(p.getCategory() == null ? "" : p.getCategory());
        }

        String thumb = p.getThumbnail();
        if (thumb != null && !thumb.isEmpty()) {
            Glide.with(h.itemView.getContext()).load(thumb).into(h.imgThumb);
        } else {
            h.imgThumb.setImageDrawable(null);
        }

        //받은 선물
        boolean isReceived = receivedGiftIds.contains(p.getId());
        if (isReceived) {
            h.itemView.setAlpha(0.4f);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            h.imgThumb.setColorFilter(new ColorMatrixColorFilter(matrix));
            h.itemView.setClickable(false);
            if (h.btnWish != null) h.btnWish.setVisibility(View.GONE);
        } else {
            h.itemView.setAlpha(1.0f);
            h.imgThumb.setColorFilter(null);
            h.itemView.setClickable(true);
            if (h.btnWish != null) h.btnWish.setVisibility(View.VISIBLE);
        }

        //위시리스트
        boolean isWished = wishlistIds.contains(p.getId());
        if (h.btnWish != null && !isReceived) {
            h.btnWish.setImageResource(isWished ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            
            if (isWished) {
                h.btnWish.setColorFilter(Color.parseColor("#D080B6"));
            } else {
                h.btnWish.setColorFilter(Color.parseColor("#555555"));
            }

            h.btnWish.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
                
                boolean nextState = !isWished;
                FirebaseManager.getInstance().toggleWishlist(p.getId(), nextState);
                
                if (nextState) {
                    if (!wishlistIds.contains(p.getId())) wishlistIds.add(p.getId());
                } else {
                    wishlistIds.remove(p.getId());
                }
                notifyItemChanged(position);
                Toast.makeText(v.getContext(), nextState ? "위시리스트 추가" : "위시리스트 제거", Toast.LENGTH_SHORT).show();
            });
        }

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), DetailActivity.class);
            i.putExtra("productId", p.getId());
            if (targetFriendUid != null) {
                i.putExtra("targetFriendUid", targetFriendUid);
            }
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_header);
        }
    }

    static class ProductVH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvPrice, tvCategory;
        ImageButton btnWish;

        ProductVH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnWish = itemView.findViewById(R.id.btn_wish);
        }
    }

    static class Row {
        boolean isHeader;
        String headerText;
        Product product;
        static Row header(String text) {
            Row r = new Row(); r.isHeader = true; r.headerText = text; return r;
        }
        static Row product(Product p) {
            Row r = new Row(); r.isHeader = false; r.product = p; return r;
        }
    }
}
