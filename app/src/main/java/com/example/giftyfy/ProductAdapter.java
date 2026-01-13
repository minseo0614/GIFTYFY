package com.example.giftyfy;

import android.content.Intent;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    private final List<Product> items = new ArrayList<>();
    private final List<String> wishlistIds = new ArrayList<>(); // ✅ 내 위시리스트 ID 목록
    private final DecimalFormat df = new DecimalFormat("#,###");
    private String targetFriendUid = null;

    public void setTargetFriendUid(String uid) {
        this.targetFriendUid = uid;
    }

    public void setItems(List<Product> products) {
        items.clear();
        if (products != null) items.addAll(products);
        notifyDataSetChanged();
    }

    // ✅ 위시리스트 정보 갱신
    public void setWishlistIds(List<String> ids) {
        wishlistIds.clear();
        if (ids != null) wishlistIds.addAll(ids);
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
        h.tvPrice.setText(df.format(p.getPrice()) + "원");

        String thumb = p.getThumbnail();
        Glide.with(h.itemView.getContext())
                .load(thumb)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground) 
                .error(R.drawable.ic_launcher_foreground)
                .into(h.imgThumb);

        // ✅ 1. 위시리스트 상태 표시
        boolean isWished = wishlistIds.contains(p.getId());
        if (h.btnWish != null) {
            h.btnWish.setVisibility(View.VISIBLE);
            h.btnWish.setImageResource(isWished ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            
            // ✅ 2. 위시리스트 버튼 클릭 이벤트
            h.btnWish.setOnClickListener(v -> {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(v.getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                boolean nextState = !isWished;
                FirebaseManager.getInstance().toggleWishlist(p.getId(), nextState);
                
                // 로컬 상태 즉시 반영 (선택 사항: 서버 리스너가 있으면 자동으로 다시 불릴 것임)
                if (nextState) {
                    if (!wishlistIds.contains(p.getId())) wishlistIds.add(p.getId());
                } else {
                    wishlistIds.remove(p.getId());
                }
                notifyItemChanged(position);
                
                String msg = nextState ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.";
                Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
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
            btnWish  = itemView.findViewById(R.id.btn_wish);
        }
    }
}