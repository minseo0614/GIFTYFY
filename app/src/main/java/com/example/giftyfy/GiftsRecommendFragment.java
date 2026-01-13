package com.example.giftyfy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GiftsRecommendFragment extends Fragment {

    private static final String ARG_FRIEND_UID = "friendUid";
    private static final String ARG_FRIEND_NAME = "friendName";

    private String friendUid;
    private String friendName;

    private ProductAdapter adapter;
    private TextView tvGiftHeader;

    public GiftsRecommendFragment() {
        super(R.layout.fragment_gifts); // 너가 올린 fragment_gifts.xml 그대로 사용
    }

    public static GiftsRecommendFragment newInstance(String friendUid, String friendName) {
        GiftsRecommendFragment f = new GiftsRecommendFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FRIEND_UID, friendUid);
        b.putString(ARG_FRIEND_NAME, friendName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            friendUid = args.getString(ARG_FRIEND_UID, null);
            friendName = args.getString(ARG_FRIEND_NAME, "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGiftHeader = view.findViewById(R.id.tvGiftHeader);

        RecyclerView rv = view.findViewById(R.id.rv_products);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter();
        rv.setAdapter(adapter);

        // ✅ "OO 추천 선물" 표시
        if (friendName != null && !friendName.isEmpty()) {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText(friendName + " 추천 선물");
        } else {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText("추천 선물");
        }

        loadTop6();
    }

    private void loadTop6() {
        if (friendUid == null || friendUid.isEmpty()) {
            Toast.makeText(getContext(), "친구 정보가 없어요", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().getFriendContext(friendUid, new FirebaseManager.OnFriendContextLoadedListener() {
            @Override
            public void onLoaded(String relation, ArrayList<String> interests) {
                final String rel = (relation == null || relation.isEmpty()) ? "미설정" : relation;
                final ArrayList<String> ints = (interests == null) ? new ArrayList<>() : interests;

                Log.d("Reco", "friendUid=" + friendUid + ", relation=" + rel + ", interests=" + ints.size());

                FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
                    @Override
                    public void onLoaded(List<Product> products) {
                        List<Product> top6 = Recommender.topN(products, rel, ints, 6);
                        adapter.setItems(top6); // ✅ 절대 products 전체를 넣지 말 것
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("Reco", "getAllProducts error", e);
                        Toast.makeText(getContext(), "상품 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Reco", "getFriendContext error", e);
                Toast.makeText(getContext(), "친구 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
