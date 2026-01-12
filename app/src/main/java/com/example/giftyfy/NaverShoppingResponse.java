package com.example.giftyfy;

import java.util.List;

public class NaverShoppingResponse {
    public List<NaverItem> items;

    public static class NaverItem {
        public String title;
        public String link; // ✅ 상품 상세 페이지 URL 추가
        public String image;
        public String lprice;
        public String category1;
        public String category2;
        public String category3;
        public String category4;
    }
}
