package com.example.giftyfy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GiftsRecommendFragment extends Fragment {

    private boolean fromFriend = false;
    private String friendName = "";

    private String relation = "미설정";
    private ArrayList<String> interests = new ArrayList<>();
    private ArrayList<String> receivedTitles = new ArrayList<>();

    private RecyclerView rv;
    private GiftsMixAdapter adapter;

    public GiftsRecommendFragment() {
        super(R.layout.fragment_gifts_recommend);
    }

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
                if (fromFriend && position == 0) return 2;
                return 1;
            }
        });
        rv.setLayoutManager(glm);

        adapter = new GiftsMixAdapter(fromFriend, friendName, receivedTitles);
        rv.setAdapter(adapter);

        fetchProductsFromFirestore();
    }

    private void fetchProductsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> allProducts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Product p = doc.toObject(Product.class);
                                p.setId(doc.getId());
                                allProducts.add(p);
                            } catch (Exception e) {
                                Log.e("GiftsRecommendFragment", "문서 변환 에러: " + doc.getId(), e);
                            }
                        }

                        if (!allProducts.isEmpty()) {
                            Log.d("GiftsRecommendFragment", "Firestore에서 " + allProducts.size() + "개의 상품 로드 성공");
                            List<Product> recommended = buildRecommended(allProducts);
                            adapter.setData(recommended, allProducts);
                        } else {
                            Log.d("GiftsRecommendFragment", "상품 데이터가 없어 API에서 가져옵니다.");
                            fetchAndStoreFromApi();
                        }
                    } else {
                        Log.e("GiftsRecommendFragment", "Firestore 로드 에러: ", task.getException());
                    }
                });
    }

    private void fetchAndStoreFromApi() {
        ProductApi api = ApiClient.get().create(ProductApi.class);
        api.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> apiProducts = response.body().products;
                    if (apiProducts != null && !apiProducts.isEmpty()) {
                        Log.d("GiftsRecommendFragment", "API 응답 성공: " + apiProducts.size() + "개");
                        
                        // 1. 카테고리 다양성을 위해 리스트 섞기
                        Collections.shuffle(apiProducts);
                        
                        // 2. 150개 추출 (또는 전체가 150개 미만이면 전체)
                        int targetSize = Math.min(150, apiProducts.size());
                        saveToFirestore(apiProducts.subList(0, targetSize));
                    } else {
                        Log.e("GiftsRecommendFragment", "API 응답은 성공했으나 상품 리스트가 비어있음");
                    }
                } else {
                    Log.e("GiftsRecommendFragment", "API 응답 실패: code=" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                Log.e("GiftsRecommendFragment", "API 통신 실패 (네트워크 확인 필요)", t);
            }
        });
    }

    private void saveToFirestore(List<Product> products) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Firestore Batch는 한 번에 500개까지 가능하므로 150개는 한 번에 가능
        WriteBatch batch = db.batch();

        for (Product p : products) {
            enrichProduct(p); // 환율 적용 및 정교한 태깅
            DocumentReference ref = db.collection("products").document();
            batch.set(ref, p);
        }

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("GiftsRecommendFragment", "Firestore에 데이터 저장 완료 (" + products.size() + "개)");
                if (isAdded()) {
                    Toast.makeText(getContext(), "데이터베이스 구축 완료! (150개)", Toast.LENGTH_SHORT).show();
                    fetchProductsFromFirestore();
                }
            } else {
                Log.e("GiftsRecommendFragment", "Firestore 저장 실패", task.getException());
            }
        });
    }

    private void enrichProduct(Product p) {
        // 1. 환율 적용 (USD -> KRW, 1200원) 및 100원 단위 반올림
        double rawPrice = p.getPrice() * 1200;
        double roundedPrice = Math.round(rawPrice / 100.0) * 100;
        p.setPrice(roundedPrice);

        String cat = safe(p.getCategory()).toLowerCase();
        String title = safe(p.getTitle()).toLowerCase();
        String desc = safe(p.getDescription()).toLowerCase();
        String fullText = title + " " + cat + " " + desc;

        List<String> tags = new ArrayList<>();
        Map<String, Integer> scores = new HashMap<>();

        // 기본 점수 설정
        scores.put("연인", 10);
        scores.put("가족", 10);
        scores.put("친구", 10);
        scores.put("회사 동료", 10);

        // 2. 정교한 키워드 기반 태깅 및 관계 점수 설정
        if (fullText.contains("perfume") || fullText.contains("fragrance") || fullText.contains("lipstick") || fullText.contains("jewelry")) {
            tags.addAll(Arrays.asList("로맨틱성공적", "향기컬렉터", "기념일추천"));
            scores.put("연인", 30);
            scores.put("친구", 15);
        } else if (fullText.contains("laptop") || fullText.contains("smartphone") || fullText.contains("watch") || fullText.contains("tablet")) {
            tags.addAll(Arrays.asList("얼리어답터", "기계덕후", "생산성최고"));
            scores.put("연인", 20);
            scores.put("친구", 25);
            scores.put("가족", 15);
        } else if (fullText.contains("skin") || fullText.contains("cream") || fullText.contains("serum") || fullText.contains("mask")) {
            tags.addAll(Arrays.asList("피부관리진심러", "뷰티덕후", "자기관리"));
            scores.put("친구", 20);
            scores.put("연인", 20);
        } else if (fullText.contains("kitchen") || fullText.contains("cooking") || fullText.contains("furniture") || fullText.contains("home")) {
            tags.addAll(Arrays.asList("집꾸미기", "프로집사", "요리왕"));
            scores.put("가족", 30);
            scores.put("친구", 15);
        } else if (fullText.contains("stationery") || fullText.contains("notebook") || fullText.contains("pen") || fullText.contains("office")) {
            tags.addAll(Arrays.asList("데스크테리어", "열정회사원", "실용주의자"));
            scores.put("회사 동료", 30);
            scores.put("친구", 15);
        } else if (fullText.contains("sport") || fullText.contains("gym") || fullText.contains("fitness") || fullText.contains("running")) {
            tags.addAll(Arrays.asList("운동하는사람", "건강이최고", "오운완"));
            scores.put("친구", 25);
            scores.put("가족", 15);
        } else {
            tags.add("가성비갑");
            tags.add("센스쟁이");
        }

        // 중복 태그 제거 및 설정
        Set<String> tagSet = new HashSet<>(tags);
        p.setTags(new ArrayList<>(tagSet));
        p.setRelationScores(scores);
    }

    private List<Product> buildRecommended(List<Product> all) {
        Set<String> receivedSet = new HashSet<>(receivedTitles);
        List<Product> copy = new ArrayList<>(all);
        Collections.sort(copy, (a, b) -> {
            int sa = score(a, relation, interests, receivedSet);
            int sb = score(b, relation, interests, receivedSet);
            return Integer.compare(sb, sa);
        });
        return new ArrayList<>(copy.subList(0, Math.min(8, copy.size())));
    }

    private int score(Product p, String relation, List<String> interests, Set<String> receivedSet) {
        int s = 0;
        s += p.getScoreForRelation(relation);
        String text = (safe(p.getTitle()) + " " + safe(p.getCategory()) + " " + safe(p.getDescription())).toLowerCase();
        
        for (String it : interests) {
            String key = it.replace("#", "").trim().toLowerCase();
            if (text.contains(key)) s += 20; // 관심사 매칭 점수 상향
        }

        if (receivedSet.contains(p.getTitle())) s -= 999;
        
        // 원화 기준 가격 점수 (예: 10만원 이상이면 선물로서 가치 상승)
        if (p.getPrice() >= 100000) s += 10;

        return s;
    }

    private String safe(String s) { return (s == null) ? "" : s; }
}
