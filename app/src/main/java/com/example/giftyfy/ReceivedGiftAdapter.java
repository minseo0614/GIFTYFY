package com.example.giftyfy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReceivedGiftAdapter extends RecyclerView.Adapter<ReceivedGiftAdapter.VH> {

    private final List<ReceivedGift> items = new ArrayList<>();

    public void setItems(List<ReceivedGift> gifts) {
        items.clear();
        if (gifts != null) items.addAll(gifts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_gift, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReceivedGift rg = items.get(position);

        h.tvSender.setText("From " + rg.getSenderName());
        
        // 상품 정보 로드
        FirebaseFirestore.getInstance().collection("products").document(rg.getProductId()).get()
                .addOnSuccessListener(pdoc -> {
                    if (pdoc.exists()) {
                        h.tvTitle.setText(pdoc.getString("title"));
                        String thumb = pdoc.getString("thumbnail");
                        Glide.with(h.itemView.getContext()).load(thumb).into(h.ivProduct);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvTitle, tvSender;

        VH(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSender = itemView.findViewById(R.id.tvSender);
        }
    }
}
