package com.example.giftyfy;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GiftsRecommendFragment extends Fragment {

    private boolean fromFriend = false;
    private String friendName = "";

    // 추천 계산용(표시는 X)
    private String relation = "미설정";
    private ArrayList<String> interests = new ArrayList<>();
    private ArrayList<String> receivedTitles = new ArrayList<>();

    private RecyclerView rv;
    private GiftsMixAdapter adapter;

    public GiftsRecommendFragment() {
        super(R.layout.fragment_gifts_recommend);
    }

    // ✅ 선물 탭 그냥 눌러서 들어오는 기본 화면
    public static GiftsRecommendFragment newDefault() {
        GiftsRecommendFragment f = new GiftsRecommendFragment();
        Bundle b = new Bundle();
        b.putBoolean("fromFriend", false);
        b.putString("friendName", "");
        b.putString("relation", "미설정");
        b.putStringArrayList("interests", new ArrayList<>());
        b.putStringArrayList("receivedTitles", new ArrayList<>());
        f.setArguments(b);
        return f;
    }

    // ✅ 친구에서 넘어온 경우
    public static GiftsRecommendFragment newInstance(String friendName,
                                                     String relation,
                                                     ArrayList<String> interests,
                                                     ArrayList<String> receivedTitles) {
        GiftsRecommendFragment f = new GiftsRecommendFragment();
        Bundle b = new Bundle();
        b.putBoolean("fromFriend", true);
        b.putString("friendName", friendName);
        b.putString("relation", relation);
        b.putStringArrayList("interests", (interests == null) ? new ArrayList<>() : interests);
        b.putStringArrayList("receivedTitles", (receivedTitles == null) ? new ArrayList<>() : receivedTitles);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            fromFriend = getArguments().getBoolean("fromFriend", false);
            friendName = getArguments().getString("friendName", "");
            relation = getArguments().getString("relation", "미설정");

            ArrayList<String> ints = getArguments().getStringArrayList("interests");
            ArrayList<String> rec = getArguments().getStringArrayList("receivedTitles");
            if (ints != null) interests = ints;
            if (rec != null) receivedTitles = rec;
        }

        rv = view.findViewById(R.id.rv_mix);

        GridLayoutManager glm = new GridLayoutManager(requireContext(), 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // ✅ 헤더가 있을 때만 0번을 2칸
                if (fromFriend && position == 0) return 2;
                return 1;
            }
        });
        rv.setLayoutManager(glm);

        adapter = new GiftsMixAdapter(fromFriend, friendName, receivedTitles);
        rv.setAdapter(adapter);

        fetchProducts();
    }

    private void fetchProducts() {
        ProductApi api = ApiClient.get().create(ProductApi.class);

        api.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call,
                                   @NonNull Response<ProductResponse> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().products == null) {
                    Toast.makeText(requireContext(), "응답 이상: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Product> all = response.body().products; // ✅ 먼저 받아오고

                Set<String> categories = new HashSet<>();
                for (Product p : all) {
                    if (p.category != null && !p.category.trim().isEmpty()) {
                        categories.add(p.category.trim());
                    }
                }

                android.util.Log.d("CATEGORY", "카테고리 목록 = " + categories);

                // 나머지 기존 로직
                List<Product> rec = buildRecommended(all);
                adapter.setData(rec, all);
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "API 실패: " + t.toString(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private List<Product> buildRecommended(List<Product> all) {
        Set<String> receivedSet = new HashSet<>(receivedTitles);

        List<Product> copy = new ArrayList<>(all);
        Collections.sort(copy, new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                int sa = score(a, relation, interests, receivedSet);
                int sb = score(b, relation, interests, receivedSet);
                return Integer.compare(sb, sa);
            }
        });

        int k = Math.min(8, copy.size());
        return new ArrayList<>(copy.subList(0, k));
    }

    private int score(Product p, String relation, List<String> interests, Set<String> receivedSet) {
        int s = 0;

        switch (relation) {
            case "연인": s += 20; break;
            case "가족": s += 15; break;
            case "친구": s += 12; break;
            case "회사 동료": s += 8; break;
            case "어색한 사이": s += 3; break;
            default: s += 5; break;
        }

        String text = (safe(p.title) + " " + safe(p.category) + " " + safe(p.description)).toLowerCase();

        for (String it : interests) {
            String key = it.replace("#", "").trim();

            if (key.contains("디저트")) {
                if (text.contains("chocolate") || text.contains("cake") || text.contains("cookie") || text.contains("sweet"))
                    s += 12;
            } else if (key.contains("향기") || key.contains("향수")) {
                if (text.contains("fragrance") || text.contains("perfume"))
                    s += 12;
            } else if (key.contains("피부") || key.contains("스킨")) {
                if (text.contains("skincare") || text.contains("skin") || text.contains("cream"))
                    s += 12;
            } else if (key.contains("운동") || key.contains("러닝")) {
                if (text.contains("sports") || text.contains("fitness"))
                    s += 12;
            } else {
                s += 2;
            }
        }

        if (receivedSet.contains(p.title)) s -= 999;
        if (p.price >= 50) s += 3;

        return s;
    }

    private String safe(String s) { return (s == null) ? "" : s; }
}