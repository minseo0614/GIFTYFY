package com.example.giftyfy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GiftsMixAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PRODUCT = 1;

    private final List<Row> rows = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,###");

    // ✅ 기본 모드: 전체 상품만
    public void setModeDefault(List<Product> all) {
        rows.clear();
        rows.add(Row.header("전체 선물"));
        if (all != null) {
            for (Product p : all) rows.add(Row.product(p));
        }
        notifyDataSetChanged();
    }

    // ✅ 친구 모드: 추천 6개 + 전체
    public void setModeFriend(List<Product> recommended, List<Product> all) {
        rows.clear();

        rows.add(Row.header("추천 선물"));
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

        String title = (p.getTitle() == null) ? "" : p.getTitle();
        h.tvTitle.setText(title);

        int price = p.getPrice();
        h.tvPrice.setText(df.format(price) + "원");

        String thumb = p.getThumbnail();
        if (thumb != null && !thumb.isEmpty()) {
            Glide.with(h.itemView.getContext()).load(thumb).into(h.imgThumb);
        } else {
            h.imgThumb.setImageDrawable(null);
        }
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
        TextView tvTitle, tvPrice;

        ProductVH(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }

    static class Row {
        boolean isHeader;
        String headerText;
        Product product;

        static Row header(String text) {
            Row r = new Row();
            r.isHeader = true;
            r.headerText = text;
            return r;
        }

        static Row product(Product p) {
            Row r = new Row();
            r.isHeader = false;
            r.product = p;
            return r;
        }
    }
}