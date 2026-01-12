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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiftsFragment extends Fragment {

    private RecyclerView rvGifts;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();

    public GiftsFragment() {
        super(R.layout.fragment_gifts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGifts = view.findViewById(R.id.rv_products);
        rvGifts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // ⚠️ 테스트를 위해: 탭 2에 진입하자마자 무조건 샘플 데이터를 서버에 전송해봅니다.
        Log.d("GiftsFragment", "진입 완료 - 샘플 데이터 강제 전송 시도");
        Toast.makeText(getContext(), "데이터 전송을 시도합니다...", Toast.LENGTH_SHORT).show();
        addSampleProducts(); 
    }

    private void loadProductsFromFirebase() {
        FirebaseManager.getInstance().getAllProducts(new FirebaseManager.OnProductsLoadedListener() {
            @Override
            public void onLoaded(List<Product> products) {
                if (!products.isEmpty()) {
                    Log.d("GiftsFragment", "데이터 로드 성공: " + products.size() + "개");
                    productList.clear();
                    productList.addAll(products);
                    adapter = new ProductAdapter(productList);
                    rvGifts.setAdapter(adapter);
                } else {
                    Log.d("GiftsFragment", "서버에 데이터가 여전히 없습니다.");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("GiftsFragment", "불러오기 에러", e);
            }
        });
    }

    private void addSampleProducts() {
        List<Product> samples = new ArrayList<>();
        samples.add(new Product("프리미엄 바디워시 세트", 35000, "https://m.media-amazon.com/images/I/71mZ9-v-LpL._SL1500_.jpg", "은은한 우디향의 바디워시 세트", "뷰티", Arrays.asList("향기컬렉터", "집순이", "피부관리진심러")));
        samples.add(new Product("고광택 기계식 키보드", 120000, "https://m.media-amazon.com/images/I/61p-vofkaYL._AC_SL1500_.jpg", "타건감이 좋은 적축 키보드", "전자제품", Arrays.asList("게임덕후", "프로직장인", "집돌이")));
        samples.add(new Product("홈캠핑 의자 2인용", 58000, "https://m.media-amazon.com/images/I/71-SMZmq8FL._AC_SL1500_.jpg", "거실에서도 즐기는 캠핑 분위기", "리빙", Arrays.asList("캠핑매니아", "집순이", "집돌이")));
        samples.add(new Product("제주 말차 디저트 박스", 24000, "https://img.danawa.com/prod_img/500000/441/411/img/14141144_1.jpg", "진한 말차 맛의 쿠키와 케이크", "식품", Arrays.asList("디저트러버", "빵지순례자", "카페돌이")));
        samples.add(new Product("애플워치 스포츠 루프", 65000, "https://m.media-amazon.com/images/I/71Yp-Z6-LpL._AC_SL1500_.jpg", "통기성이 뛰어난 운동용 스트랩", "패션", Arrays.asList("운동매니아", "러닝크루", "패션피플")));
        samples.add(new Product("와인 에어레이터 & 오프너", 42000, "https://m.media-amazon.com/images/I/61-P3qV5v6L._AC_SL1500_.jpg", "와인의 풍미를 더해주는 필수템", "주방", Arrays.asList("애주가", "요리꿈나무", "보드게이머")));
        samples.add(new Product("고급 가죽 명함 지갑", 89000, "https://m.media-amazon.com/images/I/81-Z6-LpL._AC_SL1500_.jpg", "첫 출근 선물로 좋은 소가죽 지갑", "패션", Arrays.asList("프로직장인", "패션피플", "상품권애호가")));
        samples.add(new Product("멀티 비타민 & 유산균", 45000, "https://m.media-amazon.com/images/I/71Z6-LpL._AC_SL1500_.jpg", "현대인을 위한 필수 영양제 세트", "건강", Arrays.asList("영양제신봉자", "다이어터", "프로직장인")));
        samples.add(new Product("귀여운 고양이 수면등", 19000, "https://m.media-amazon.com/images/I/61Z6-LpL._AC_SL1500_.jpg", "말랑말랑한 실리콘 재질의 무드등", "리빙", Arrays.asList("귀여운게최고", "댕냥이집사", "집순이")));
        samples.add(new Product("홈메이드 파스타 키트", 32000, "https://m.media-amazon.com/images/I/91Z6-LpL._AC_SL1500_.jpg", "누구나 셰프가 될 수 있는 밀키트", "식품", Arrays.asList("요리꿈나무", "고기진심러", "애주가")));

        final int[] count = {0};
        for (Product p : samples) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("products")
                .add(p)
                .addOnSuccessListener(documentReference -> {
                    count[0]++;
                    Log.d("GiftsFragment", "전송 성공: " + count[0] + "/10");
                    if (count[0] == samples.size()) {
                        Toast.makeText(getContext(), "모든 상품 등록 완료!", Toast.LENGTH_SHORT).show();
                        loadProductsFromFirebase(); // 다 올렸으면 다시 읽어오기
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GiftsFragment", "전송 실패", e);
                    Toast.makeText(getContext(), "전송 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        }
    }
}
