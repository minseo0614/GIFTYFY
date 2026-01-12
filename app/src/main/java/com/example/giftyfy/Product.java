package com.example.giftyfy;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    @DocumentId
    private String id;
    
    private String title;
    private double price; // int -> double로 변경
    private String thumbnail;
    private String description;
    private String category;
    
    private List<String> tags = new ArrayList<>();
    
    @Exclude
    private boolean wish = false;
    
    private Map<String, Integer> relationScores = new HashMap<>();

    public Product() {}

    public Product(String title, double price, String thumbnail, String description, String category, List<String> tags) {
        this.title = title;
        this.price = price;
        this.thumbnail = thumbnail;
        this.description = description;
        this.category = category;
        this.tags = (tags != null) ? tags : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Exclude
    public boolean isWish() { return wish; }
    
    @Exclude
    public void setWish(boolean wish) { this.wish = wish; }
    
    public Map<String, Integer> getRelationScores() { return relationScores; }
    public void setRelationScores(Map<String, Integer> relationScores) { this.relationScores = relationScores; }

    public int getScoreForRelation(String relation) {
        if (relationScores != null && relationScores.containsKey(relation)) {
            Integer score = relationScores.get(relation);
            return (score != null) ? score : 0;
        }
        return 0;
    }
}
