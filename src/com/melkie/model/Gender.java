package com.melkie.model;

import java.util.Locale;

/**
 * 페르소나 및 매칭 선호 성별을 표현하는 열거형.
 * 기존에는 "MALE"/"FEMALE" 문자열을 그대로 여기저기서 비교했으나(Primitive Obsession),
 * 이를 타입으로 승격시켜 오타/대소문자 문제를 컴파일 타임에 방지한다.
 */
public enum Gender {

    MALE("남"),
    FEMALE("여");

    private final String displayLabel;

    Gender(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    /** 콘솔 출력용 한 글자 표기 (예: "남", "여") */
    public String displayLabel() {
        return displayLabel;
    }

    /** personas.json의 "MALE"/"FEMALE" 문자열을 Gender로 변환한다. */
    public static Gender fromJsonValue(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("gender 값이 비어 있습니다.");
        }
        switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "MALE":
                return MALE;
            case "FEMALE":
                return FEMALE;
            default:
                throw new IllegalArgumentException("알 수 없는 gender 값입니다: " + raw);
        }
    }
}
