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
    private double price; // 기존 double 유지
    private String thumbnail;
    private String description;
    private String category;
    private String productUrl; 
    
    private List<String> tags = new ArrayList<>();
    
    @Exclude
    private boolean wish = false;
    
    private Map<String, Integer> relationScores = new HashMap<>(); // 기존 Integer 유지

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Integer> getRelationScores() { return relationScores; }
    public void setRelationScores(Map<String, Integer> relationScores) { this.relationScores = relationScores; }

    public boolean isWish() { return wish; }
    public void setWish(boolean wish) { this.wish = wish; }
}