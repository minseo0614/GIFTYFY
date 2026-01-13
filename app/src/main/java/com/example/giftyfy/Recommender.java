package com.example.giftyfy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            // Product 안에 relationScores 같은 맵이 있다고 가정
            // (너가 이전에 쓰던 p.getRelationScores() 기준)
            Map<String, ?> map = p.getRelationScores();
            if (map == null) return 0;

            Object v = map.get(relation);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v instanceof String) {
                try { return Integer.parseInt((String) v); } catch (Exception ignore) {}
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int getTagScore(Product p, Set<String> interestSet) {
        try {
            List<String> tags = p.getTags(); // Product에 tags 리스트가 있어야 함
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