package com.melkie.service;

import com.melkie.model.Gender;
import com.melkie.model.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 페르소나 탐색 유스케이스를 담당하는 서비스.
 * 콘솔 입출력(Scanner/System.out)에 전혀 의존하지 않는 순수 로직이므로,
 * 추후 GUI/웹 등 다른 UI로 교체되더라도 이 클래스는 그대로 재사용할 수 있다.
 */
public final class PersonaBrowseService {

    private PersonaBrowseService() {
    }

    /**
     * 사용자가 입력한 원본 문자열("M", "F", "A", "남", "여" 등)을 성별 필터로 해석한다.
     * 값이 비어있거나 알 수 없는 입력이면 "무관(전체 보기)"으로 간주하여 빈 Optional을 반환한다.
     */
    public static Optional<Gender> parseGenderChoice(String rawInput) {
        if (rawInput == null) {
            return Optional.empty();
        }
        String normalized = rawInput.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("M") || normalized.equals("남")) {
            return Optional.of(Gender.MALE);
        }
        if (normalized.equals("F") || normalized.equals("여")) {
            return Optional.of(Gender.FEMALE);
        }
        return Optional.empty();
    }

    /** 주어진 성별 필터에 맞는 페르소나만 골라 새 목록으로 반환한다. 필터가 비어있으면 전체를 반환한다. */
    public static List<Persona> filterByGender(List<Persona> personas, Optional<Gender> genderFilter) {
        if (genderFilter.isEmpty()) {
            return new ArrayList<>(personas);
        }
        Gender wanted = genderFilter.get();
        List<Persona> filtered = new ArrayList<>();
        for (Persona persona : personas) {
            if (persona.getGender() == wanted) {
                filtered.add(persona);
            }
        }
        return filtered;
    }
}
