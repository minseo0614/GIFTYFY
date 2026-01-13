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
import java.util.Map;

public class GiftsFragment extends Fragment {

    private static final String ARG_FROM_FRIEND = "fromFriend";
    private static final String ARG_FRIEND_UID = "friendUid";
    private static final String ARG_FRIEND_NAME = "friendName";

    private boolean fromFriend = false;
    private String friendUid = "";
    private String friendName = "";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private TextView tvGiftHeader;

    // ✅ inner class final 문제 방지
    private final List<String> friendInterests = new ArrayList<>();

    public GiftsFragment() {
        super(R.layout.fragment_gifts);
    }

    public static GiftsFragment newDefault() {
        GiftsFragment f = new GiftsFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_FROM_FRIEND, false);
        f.setArguments(b);
        return f;
    }

    public static GiftsFragment newFromFriend(String friendUid, String friendName) {
        GiftsFragment f = new GiftsFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_FROM_FRIEND, true);
        b.putString(ARG_FRIEND_UID, friendUid);
        b.putString(ARG_FRIEND_NAME, friendName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGiftHeader = view.findViewById(R.id.tvGiftHeader);

        recyclerView = view.findViewById(R.id.rv_products);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter();
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            fromFriend = getArguments().getBoolean(ARG_FROM_FRIEND, false);
            friendUid = getArguments().getString(ARG_FRIEND_UID, "");
            friendName = getArguments().getString(ARG_FRIEND_NAME, "");
        }

        if (!fromFriend) {
            tvGiftHeader.setVisibility(View.GONE);
            loadAllProducts();
        } else {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText(friendName + " 추천 선물");
            loadTop6ForFriend();
        }
    }

    private void loadAllProducts() {
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                Log.d("GiftsFragment", "all products=" + (products == null ? 0 : products.size()));
                adapter.setItems(products);
            }

            @Override
            public void onError(Exception e) {
                Log.e("GiftsFragment", "loadAllProducts error", e);
                Toast.makeText(getContext(), "상품 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTop6ForFriend() {
        // 1) 친구 interests 불러오기
        FirebaseManager.getInstance().getUserByUid(friendUid, new FirebaseManager.OnUserLoadedListener() {
            @Override
            public void onLoaded(Map<String, Object> userData) {
                friendInterests.clear();
                if (userData != null) {
                    Object raw = userData.get("interests");
                    if (raw instanceof List) {
                        //noinspection unchecked
                        friendInterests.addAll((List<String>) raw);
                    }
                }

                // 2) 내가 그 친구를 설정한 relation 불러오기
                FirebaseManager.getInstance().getMyFriendRelation(friendUid, new FirebaseManager.OnRelationLoadedListener() {
                    @Override
                    public void onLoaded(String relation) {

                        // 3) products 불러와서 알고리즘으로 top6 계산
                        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
                            @Override
                            public void onLoaded(List<Product> products) {
                                List<Product> top6 = Recommender.topN(products, relation, friendInterests, 6);
                                adapter.setItems(top6);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("GiftsFragment", "getAllProducts error", e);
                                Toast.makeText(getContext(), "상품 불러오기 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("GiftsFragment", "getMyFriendRelation error", e);
                        Toast.makeText(getContext(), "관계 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("GiftsFragment", "getUserByUid error", e);
                Toast.makeText(getContext(), "친구 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}