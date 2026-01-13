package com.example.giftyfy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Recommender {

    private static final int TAG_BONUS = 5; // interests 1개 겹치면 +5

    public static List<Product> topN(List<Product> all, String relation, List<String> interests, int n) {
        if (all == null) return new ArrayList<>();
        if (relation == null) relation = "미설정";
        if (interests == null) interests = new ArrayList<>();

        Set<String> interestSet = new HashSet<>(interests);

        List<Scored> scored = new ArrayList<>();
        for (Product p : all) {
            if (p == null) continue;
            int score = 0;

            // 1) 관계 점수
            score += getRelationScore(p, relation);

            // 2) interests 태그 매칭 점수
            score += getTagScore(p, interestSet);

            scored.add(new Scored(p, score));
        }

        Collections.sort(scored, (a, b) -> Integer.compare(b.score, a.score)); // 높은 점수 먼저

        List<Product> result = new ArrayList<>();
        int limit = Math.min(n, scored.size());
        for (int i = 0; i < limit; i++) result.add(scored.get(i).p);
        return result;
    }

    private static int getRelationScore(Product p, String relation) {
        try {
            // Product의 relationScores가 Map<String, Integer>로 복구됨에 따라
            // 안전하게 형변환하여 처리
            Map<String, Integer> map = p.getRelationScores();
            if (map == null) return 0;

            Object v = map.get(relation);
            if (v instanceof Number) return ((Number) v).intValue();
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int getTagScore(Product p, Set<String> interestSet) {
        try {
            List<String> tags = p.getTags(); 
            if (tags == null || tags.isEmpty()) return 0;

            int match = 0;
            for (String t : tags) {
                if (t != null && interestSet.contains(t)) match++;
            }
            return match * TAG_BONUS;
        } catch (Exception e) {
            return 0;
        }
    }

    private static class Scored {
        Product p;
        int score;
        Scored(Product p, int score) { this.p = p; this.score = score; }
    }
}