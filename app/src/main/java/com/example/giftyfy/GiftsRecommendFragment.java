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

    private static final String TAG = "GiftsRecommendFragment";

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
            interests = getArguments().getStringArrayList("interests");
            receivedTitles = getArguments().getStringArrayList("receivedTitles");
            if (interests == null) interests = new ArrayList<>();
            if (receivedTitles == null) receivedTitles = new ArrayList<>();
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
        Log.d(TAG, "Step 1: Firestore 조회 시도...");
        
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Product> allProducts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Product p = doc.toObject(Product.class);
                                p.setId(doc.getId());
                                allProducts.add(p);
                            } catch (Exception e) {
                                Log.e(TAG, "문서 변환 에러", e);
                            }
                        }

                        if (!allProducts.isEmpty()) {
                            Log.d(TAG, "Step 2: Firestore 데이터 로드 성공 (" + allProducts.size() + "개)");
                            adapter.setData(new ArrayList<>(), allProducts);
                        } else {
                            Log.d(TAG, "Step 2: Firestore가 비어있음 -> 네이버 API 호출 시작");
                            fetchAndStoreFromNaverApi();
                        }
                    } else {
                        Log.e(TAG, "Step 2: Firestore 접근 실패", task.getException());
                    }
                });
    }

    private void fetchAndStoreFromNaverApi() {
        Log.d(TAG, "Step 3: 네이버 API 호출 중...");
        ProductApi api = ApiClient.get().create(ProductApi.class);

        // "생일선물" 키워드로 100개 요청
        api.searchShop("생일선물", 100, 1, "sim").enqueue(new Callback<NaverShoppingResponse>() {
            @Override
            public void onResponse(@NonNull Call<NaverShoppingResponse> call, @NonNull Response<NaverShoppingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NaverShoppingResponse.NaverItem> items = response.body().items;
                    if (items != null && !items.isEmpty()) {
                        Log.d(TAG, "Step 4: 네이버 API 응답 성공 (아이템 " + items.size() + "개)");
                        List<Product> mappedProducts = new ArrayList<>();
                        for (NaverShoppingResponse.NaverItem item : items) {
                            // 단순 세트 상품이 너무 많으면 필터링 (필요 시 조정 가능)
                            if (item.title.contains("세트") || item.title.contains("구성") || item.title.contains("1+1")) {
                                if (mappedProducts.size() > 20) continue; 
                            }

                            Product p = new Product();
                            p.setTitle(item.title.replaceAll("<[^>]*>", "").trim());
                            String pStr = item.lprice.replaceAll("[^0-9]", "");
                            p.setPrice(pStr.isEmpty() ? 0 : Double.parseDouble(pStr));
                            p.setThumbnail(item.image);
                            p.setProductUrl(item.link); // ✅ 상세 페이지 URL 저장
                            p.setCategory(safe(item.category1) + " " + safe(item.category2) + " " + safe(item.category3));

                            enrichProduct(p);
                            mappedProducts.add(p);
                        }
                        saveToFirestore(mappedProducts);
                    } else {
                        Log.w(TAG, "Step 4: 네이버 API 응답은 성공했으나 아이템이 비어있음");
                    }
                } else {
                    Log.e(TAG, "Step 4: 네이버 API 응답 실패 (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<NaverShoppingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Step 4: API 호출 에러", t);
            }
        });
    }

    private void saveToFirestore(List<Product> products) {
        if (products.isEmpty()) {
            Log.w(TAG, "저장할 상품 리스트가 비어있습니다.");
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();
        
        Log.d(TAG, "Step 5: Firestore Batch 저장 시작 (" + products.size() + "개)");
        
        for (Product p : products) {
            DocumentReference ref = db.collection("products").document();
            batch.set(ref, p);
        }
        
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Step 6: Firestore 저장 완료!");
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "데이터 저장 완료! (" + products.size() + "개)", Toast.LENGTH_SHORT).show();
                    fetchProductsFromFirestore(); // 저장 후 다시 불러와서 UI 갱신
                }
            } else {
                Log.e(TAG, "Step 6: Firestore 저장 실패", task.getException());
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "저장 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void enrichProduct(Product p) {
        String full = (p.getTitle() + " " + p.getCategory()).toLowerCase();
        List<String> tags = new ArrayList<>();
        Map<String, Integer> scores = new HashMap<>();

        // 기본 점수 설정
        scores.put("가족", 10); scores.put("친구", 10); scores.put("연인", 10); scores.put("회사", 10); scores.put("어사", 10);

        if (match(full, "케이크", "마카롱", "초콜릿", "쿠키", "디저트")) {
            tags.add("#디저트러버");
            scores.put("친구", 30); scores.put("연인", 25);
        }
        if (match(full, "와인", "위스키", "전통주", "술")) {
            tags.add("#애주가");
            scores.put("친구", 30);
        }
        if (match(full, "향수", "디퓨저", "캔들", "핸드크림")) {
            tags.add("#향기컬렉터");
            scores.put("연인", 30);
        }
        if (match(full, "반지", "목걸이", "귀걸이", "팔찌", "쥬얼리", "주얼리")) {
            tags.add("#주얼리수집가");
            scores.put("연인", 30);
        }
        if (match(full, "샤프", "볼펜", "노트", "다이어리", "문구", "필기")) {
            tags.add("#문구가좋아");
            scores.put("친구", 25); scores.put("회사", 10); scores.put("연인", 5);
        }
        if (match(full, "영양제", "비타민", "홍삼")) {
            tags.add("#영양제신봉자");
            scores.put("가족", 30);
        }

        p.setTags(new ArrayList<>(new HashSet<>(tags)));
        p.setRelationScores(scores);
    }

    private boolean match(String text, String... keywords) {
        for (String k : keywords) { if (text.contains(k)) return true; }
        return false;
    }

    private String safe(String s) { return (s == null) ? "" : s; }
}
