package com.example.giftyfy;

import java.util.*;

public class Recommender {

    private static final int RELATION_WEIGHT = 10;   // 관계점수 가중치
    private static final int TAG_MATCH_BONUS = 40;   // 태그 1개 매칭 보너스
    private static final int COVERAGE_BONUS = 20;    // 태그 커버리지 보정
    private static final int MAX_PER_CATEGORY = 2;   // 같은 카테고리 최대 노출

    public static List<Product> topN(List<Product> all, String relation, List<String> interests, int n) {
        return recommendTopN(all, relation, interests, n);
    }

    public static List<Product> recommendTopN(
            List<Product> allProducts,
            String friendRelation,
            List<String> friendInterests,
            int n
    ) {
        if (allProducts == null) return new ArrayList<>();
        if (friendRelation == null || friendRelation.trim().isEmpty()) friendRelation = "미설정";
        if (friendInterests == null) friendInterests = new ArrayList<>();

        Set<String> interestSet = new HashSet<>();
        for (String s : friendInterests) {
            if (s == null) continue;
            interestSet.add(normalizeTag(s));
            if (interestSet.size() == 3) break;
        }
        int interestCount = interestSet.size();

        List<Scored> candidates = new ArrayList<>();

        for (Product p : allProducts) {
            if (p == null) continue;

            int relationScore = getRelationScore(p, friendRelation);
            int tagMatch = countTagMatches(p, interestSet);

            // ✅ [수정] 관계 점수가 0이더라도 태그 매칭이 있으면 후보군에 포함
            // 관계 점수가 아예 없는 '어색', '미설정' 등도 관심사 기반으로 추천될 수 있게 함
            if (relationScore <= 0 && tagMatch <= 0) continue;

            double coverage = 0.0;
            if (interestCount > 0) coverage = (double) tagMatch / (double) interestCount;

            int score =
                    relationScore * RELATION_WEIGHT
                            + tagMatch * TAG_MATCH_BONUS
                            + (int) Math.round(coverage * COVERAGE_BONUS);

            candidates.add(new Scored(p, score, tagMatch, relationScore));
        }

        // 점수 높은 순 정렬
        Collections.sort(candidates, (a, b) -> {
            if (b.score != a.score) return b.score - a.score;
            if (b.tagMatch != a.tagMatch) return b.tagMatch - a.tagMatch;
            if (b.relationScore != a.relationScore) return b.relationScore - a.relationScore;
            return safe(a.p.getTitle()).compareTo(safe(b.p.getTitle()));
        });

        List<Product> result = new ArrayList<>();
        Map<String, Integer> catCnt = new HashMap<>();

        for (Scored s : candidates) {
            if (result.size() >= n) break;

            String cat = normalizeCategory(s.p.getCategory());
            if (!cat.isEmpty()) {
                int used = catCnt.getOrDefault(cat, 0);
                if (used >= MAX_PER_CATEGORY) continue;
                catCnt.put(cat, used + 1);
            }
            result.add(s.p);
        }

        // 부족하면 후보군 중 중복되지 않은 것을 무작위로 채움
        if (result.size() < n) {
            for (Scored s : candidates) {
                if (result.size() >= n) break;
                if (!result.contains(s.p)) result.add(s.p);
            }
        }
        
        // 그래도 부족하면(상품이 적을 때) 전체 리스트에서 보충
        if (result.size() < n) {
            for (Product p : allProducts) {
                if (result.size() >= n) break;
                if (!result.contains(p)) result.add(p);
            }
        }

        return result;
    }

    private static int getRelationScore(Product p, String relation) {
        Map<String, Integer> map = p.getRelationScores();
        if (map == null) return 0;
        
        // ✅ [개선] 데이터베이스에 관계 정보가 명시적으로 없을 때의 기본값 처리
        Integer v = map.get(relation);
        if (v != null) return v;
        
        // '미설정', '어색', '동료' 등이 Map에 없을 때 기본 점수 1 부여 (태그 매칭을 위해 노출 기회 제공)
        return 1; 
    }

    private static int countTagMatches(Product p, Set<String> interestSet) {
        if (interestSet == null || interestSet.isEmpty()) return 0;
        List<String> tags = p.getTags();
        if (tags == null || tags.isEmpty()) return 0;

        int match = 0;
        for (String t : tags) {
            if (t == null) continue;
            if (interestSet.contains(normalizeTag(t))) match++;
        }
        return match;
    }

    private static String normalizeTag(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("#")) s = s.substring(1);
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeCategory(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static class Scored {
        Product p;
        int score;
        int tagMatch;
        int relationScore;

        Scored(Product p, int score, int tagMatch, int relationScore) {
            this.p = p;
            this.score = score;
            this.tagMatch = tagMatch;
            this.relationScore = relationScore;
        }
    }
}
