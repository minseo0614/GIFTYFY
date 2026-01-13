package com.example.giftyfy;

import java.util.List;
import java.util.Map;

public class Product {

    private String id;                 // 문서 id (수동으로 setId)
    private String title;              // 이름
    private int price;                 // 가격
    private String thumbnail;          // 이미지 url
    private String category;           // 카테고리
    private String description;        // 설명

    private List<String> tags;         // 적합 태그 (최대 3개)
    private Map<String, Long> relationScores; // 관계별 점수 (Firestore는 Long으로 읽히는 경우 많음)

    // ✅ Firestore용 필수: 빈 생성자
    public Product() {}

    // ---- getter / setter ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Long> getRelationScores() { return relationScores; }
    public void setRelationScores(Map<String, Long> relationScores) { this.relationScores = relationScores; }
}