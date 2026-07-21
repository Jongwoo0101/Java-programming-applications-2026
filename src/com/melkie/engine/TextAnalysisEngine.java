package com.melkie.engine;

import com.melkie.model.GameConfig;
import com.melkie.model.Persona;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NLP-Free 룰 베이스 텍스트 분석 엔진.
 * 외부 LLM/형태소 분석기 없이, java.util.regex(Pattern/Matcher)와 HashMap 어휘 사전만으로
 * 사용자의 대사 문자열을 분석하여 텐션 점수(Melt Rate)와 3차원 성향 벡터 증분을 산출한다.
 *
 * 우선순위 규칙:
 *   1) 페르소나 전용 loveKeywords / hateKeywords 가 먼저 매칭되면 그 값으로 확정(override).
 *   2) 전역(global) 긍정/부정 사전은 페르소나 전용 사전에서 이미 처리된 어간은 건너뛴다.
 *   3) 3차원 성향(주도성/순종성/적극성) 사전은 페르소나와 무관하게 독립적으로 항상 적용된다.
 */
public class TextAnalysisEngine {

    private final GameConfig config;

    public TextAnalysisEngine(GameConfig config) {
        this.config = config;
    }

    public AnalysisResult analyze(String input, Persona persona) {
        if (input == null) {
            input = "";
        }
        int tension = 0;
        List<String> matched = new ArrayList<>();
        Set<String> resolvedStems = new HashSet<>();

        // 1) 페르소나 전용 사전(우선순위 최상위) - 이 페르소나를 설레게/식게 하는 키워드
        tension += scoreDictionary(input, persona.getLoveKeywords(), resolvedStems, matched);
        tension += scoreDictionary(input, persona.getHateKeywords(), resolvedStems, matched);

        // 2) 전역 공통 사전 (이미 페르소나 사전에서 처리된 어간은 override 되어 건너뜀)
        tension += scoreDictionary(input, config.getGlobalPositiveKeywords(), resolvedStems, matched);
        tension += scoreDictionary(input, config.getGlobalNegativeKeywords(), resolvedStems, matched);

        // 3) 3차원 성향 벡터 - Stream API를 활용해 매칭된 가중치의 합을 계산
        double dominance = sumMatchedWeights(input, config.getDominanceWords());
        double submission = sumMatchedWeights(input, config.getSubmissionWords());
        double assertiveness = sumMatchedWeights(input, config.getAssertiveWords());

        return new AnalysisResult(tension, dominance, submission, assertiveness, matched);
    }

    /** 사전(dictionary)에서 매칭되는 어간을 찾아 점수를 합산하고, 매칭된 어간은 resolvedStems 에 등록한다. */
    private int scoreDictionary(String input, Map<String, Integer> dictionary,
                                 Set<String> resolvedStems, List<String> matchedOut) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
            String stem = entry.getKey();
            if (resolvedStems.contains(stem)) {
                continue;
            }
            if (containsStem(input, stem)) {
                total += entry.getValue();
                resolvedStems.add(stem);
                matchedOut.add(stem);
            }
        }
        return total;
    }

    /** Stream API로 매칭된 가중치의 합을 구한다 (java.util.stream 활용). */
    private double sumMatchedWeights(String input, Map<String, Integer> dictionary) {
        final String text = input;
        return dictionary.entrySet().stream()
                .filter(e -> containsStem(text, e.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    /** 정규표현식(Pattern/Matcher)을 통한 어간 포함 여부 검사. 수백 가지 어미 변형도 어간 하나로 커버한다. */
    private boolean containsStem(String input, String stem) {
        Pattern pattern = Pattern.compile(Pattern.quote(stem));
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
