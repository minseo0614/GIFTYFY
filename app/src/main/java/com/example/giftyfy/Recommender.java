package com.example.giftyfy;

import java.util.*;

public class Recommender {

    private static final int RELATION_WEIGHT = 10;   // 관계점수 가중치
    private static final int TAG_MATCH_BONUS = 40;   // 태그 1개 매칭 보너스
    private static final int COVERAGE_BONUS = 20;    // 태그 커버리지 보정
    private static final int MAX_PER_CATEGORY = 2;   // 같은 카테고리 최대 노출

    // ✅ 기존 코드들이 호출하던 메서드(절대 이름 바꾸지 마)
    public static List<Product> topN(List<Product> all, String relation, List<String> interests, int n) {
        return recommendTopN(all, relation, interests, n);
    }

    // ✅ 새 알고리즘(내가 설계한 것)
    public static List<Product> recommendTopN(
            List<Product> allProducts,
            String friendRelation,
            List<String> friendInterests,
            int n
    ) {
        if (allProducts == null) return new ArrayList<>();
        if (friendRelation == null || friendRelation.trim().isEmpty()) friendRelation = "미설정";
        if (friendInterests == null) friendInterests = new ArrayList<>();

        // interests 최대 3개만 사용 + "#", 공백 통일
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

            // ✅ 관계 하드 필터: 0이면 제외
            if (relationScore <= 0) continue;

            int tagMatch = countTagMatches(p, interestSet);

            double coverage = 0.0;
            if (interestCount > 0) coverage = (double) tagMatch / (double) interestCount;

            int score =
                    relationScore * RELATION_WEIGHT
                            + tagMatch * TAG_MATCH_BONUS
                            + (int) Math.round(coverage * COVERAGE_BONUS);

            candidates.add(new Scored(p, score, tagMatch, relationScore));
        }

        // 정렬 + tie-break
        Collections.sort(candidates, (a, b) -> {
            if (b.score != a.score) return b.score - a.score;
            if (b.tagMatch != a.tagMatch) return b.tagMatch - a.tagMatch;
            if (b.relationScore != a.relationScore) return b.relationScore - a.relationScore;
            return safe(a.p.getTitle()).compareTo(safe(b.p.getTitle()));
        });

        // ✅ 카테고리 다양성 적용해서 topN 뽑기
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

        // 부족하면 제한 없이 채우기
        if (result.size() < n) {
            for (Scored s : candidates) {
                if (result.size() >= n) break;
                if (!result.contains(s.p)) result.add(s.p);
            }
        }

        return result;
    }

    private static int getRelationScore(Product p, String relation) {
        Map<String, Integer> map = p.getRelationScores();
        if (map == null) return 0;
        Integer v = map.get(relation);
        return (v == null) ? 0 : v;
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