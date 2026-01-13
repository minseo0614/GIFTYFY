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

public class GiftsRecommendFragment extends Fragment {

    private static final String ARG_FRIEND_UID = "friendUid";
    private static final String ARG_FRIEND_NAME = "friendName";

    private String friendUid;
    private String friendName;

    private GiftsMixAdapter adapter;
    private TextView tvGiftHeader;

    public GiftsRecommendFragment() {
        super(R.layout.fragment_gifts); 
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
        
        // ✅ GridLayoutManager 설정: 헤더는 전체 너비(2칸)를 차지하도록 설정
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // 어댑터에서 현재 아이템이 헤더인지 확인하여 Span 크기 결정
                if (adapter != null && adapter.getItemViewType(position) == GiftsMixAdapter.TYPE_HEADER) {
                    return 2; // 헤더는 2칸 모두 차지
                }
                return 1; // 일반 상품은 1칸만 차지
            }
        });
        
        rv.setLayoutManager(layoutManager);

        adapter = new GiftsMixAdapter();
        rv.setAdapter(adapter);

        if (friendName != null && !friendName.isEmpty()) {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText(friendName + " 추천 선물");
        } else {
            tvGiftHeader.setVisibility(View.VISIBLE);
            tvGiftHeader.setText("추천 선물");
        }

        loadData();
    }

    private void loadData() {
        if (friendUid == null || friendUid.isEmpty()) {
            Toast.makeText(getContext(), "친구 정보가 없어요", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().getUserByUid(friendUid, new FirebaseManager.OnUserLoadedListener() {
            @Override
            public void onLoaded(Map<String, Object> userData) {
                List<String> receivedIds = (List<String>) userData.get("receivedGifts");
                adapter.setReceivedGiftIds(receivedIds);

                final ArrayList<String> interests = new ArrayList<>();
                Object rawInts = userData.get("interests");
                if (rawInts instanceof List) {
                    interests.addAll((List<String>) rawInts);
                }

                FirebaseManager.getInstance().getMyFriendRelation(friendUid, new FirebaseManager.OnRelationLoadedListener() {
                    @Override
                    public void onLoaded(String relation) {
                        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
                            @Override
                            public void onLoaded(List<Product> allProducts) {
                                List<Product> top6 = Recommender.topN(allProducts, relation, interests, 6);
                                adapter.setModeFriend(top6, allProducts);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("Reco", "getAllProducts error", e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("Reco", "getMyFriendRelation error", e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Reco", "getUserByUid error", e);
            }
        });
    }
}