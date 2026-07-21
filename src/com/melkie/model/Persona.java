package com.melkie.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 14인의 페르소나를 표현하는 순수 자바 도메인 객체.
 * JSON(personas.json)으로부터 역직렬화되어 생성되며, 행동 특성 데이터를
 * 하드코딩하지 않고 외부 설정으로 완전히 분리(관심사 분리)한다.
 */
public class Persona {

    private final int id;
    private final String name;
    private final int age;
    private final String mbti;
    private final String gender;   // "MALE" / "FEMALE"
    private final String tagline;

    /** [주도성(Dominance), 순종성(Submission), 적극성(Assertiveness)] 목표 벡터 */
    private final double[] targetVector;

    /** 이 페르소나를 유독 설레게 하는 키워드 -> 가중치 (양수) */
    private final Map<String, Integer> loveKeywords;

    /** 이 페르소나의 호감도를 유독 떨어뜨리는 키워드 -> 가중치 (음수) */
    private final Map<String, Integer> hateKeywords;

    /** 감정 상태(CHILLY/FLIRTY/STEAMY)별 대사 목록 */
    private final Map<String, List<String>> dialogues;

    public Persona(int id, String name, int age, String mbti, String gender, String tagline,
                   double[] targetVector,
                   Map<String, Integer> loveKeywords,
                   Map<String, Integer> hateKeywords,
                   Map<String, List<String>> dialogues) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.mbti = mbti;
        this.gender = gender;
        this.tagline = tagline;
        this.targetVector = targetVector;
        this.loveKeywords = loveKeywords;
        this.hateKeywords = hateKeywords;
        this.dialogues = dialogues;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getMbti() { return mbti; }
    public String getGender() { return gender; }
    public String getTagline() { return tagline; }
    public double[] getTargetVector() { return targetVector; }
    public Map<String, Integer> getLoveKeywords() { return loveKeywords; }
    public Map<String, Integer> getHateKeywords() { return hateKeywords; }

    /** 특정 상태(tierKey)에서 랜덤한 대사 한 줄을 뽑아 반환 (동적 텍스트 조립의 재료) */
    public String randomLine(String tierKey) {
        List<String> lines = dialogues.get(tierKey);
        if (lines == null || lines.isEmpty()) {
            return "...";
        }
        int idx = ThreadLocalRandom.current().nextInt(lines.size());
        return lines.get(idx);
    }

    @Override
    public String toString() {
        return name + "(" + age + "/" + mbti + "/" + tagline + ")";
    }
}
