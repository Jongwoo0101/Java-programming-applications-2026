package com.melkie.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 전역(글로벌) 어휘 사전과 동적 접두사 풀을 담는 설정 객체.
 * personas.json 최상단에서 로드되며, 모든 페르소나에게 공통 적용되는 기본 룰을 담는다.
 */
public class GameConfig {

    private final Map<String, Integer> globalPositiveKeywords;
    private final Map<String, Integer> globalNegativeKeywords;
    private final Map<String, Integer> dominanceWords;
    private final Map<String, Integer> submissionWords;
    private final Map<String, Integer> assertiveWords;
    private final Map<String, List<String>> prefixes;

    public GameConfig(Map<String, Integer> globalPositiveKeywords,
                       Map<String, Integer> globalNegativeKeywords,
                       Map<String, Integer> dominanceWords,
                       Map<String, Integer> submissionWords,
                       Map<String, Integer> assertiveWords,
                       Map<String, List<String>> prefixes) {
        this.globalPositiveKeywords = globalPositiveKeywords;
        this.globalNegativeKeywords = globalNegativeKeywords;
        this.dominanceWords = dominanceWords;
        this.submissionWords = submissionWords;
        this.assertiveWords = assertiveWords;
        this.prefixes = prefixes;
    }

    public Map<String, Integer> getGlobalPositiveKeywords() { return globalPositiveKeywords; }
    public Map<String, Integer> getGlobalNegativeKeywords() { return globalNegativeKeywords; }
    public Map<String, Integer> getDominanceWords() { return dominanceWords; }
    public Map<String, Integer> getSubmissionWords() { return submissionWords; }
    public Map<String, Integer> getAssertiveWords() { return assertiveWords; }

    /** tensionDelta 크기에 따라 다른 접두사 풀에서 랜덤하게 하나를 뽑는다. */
    public String randomPrefix(String tierKey, int lastDelta) {
        String key;
        if (lastDelta >= 15) {
            key = "WARM";
        } else if (lastDelta <= 0) {
            key = "COLD";
        } else {
            key = tierKey;
        }
        List<String> pool = prefixes.get(key);
        if (pool == null || pool.isEmpty()) {
            pool = prefixes.get(tierKey);
        }
        if (pool == null || pool.isEmpty()) {
            return "";
        }
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }
}
