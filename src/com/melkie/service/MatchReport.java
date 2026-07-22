package com.melkie.service;

import com.melkie.model.Persona;

import java.util.Optional;

/**
 * 채팅 세션 종료 후 산출되는 매칭 결과를 담는 불변 값 객체(Value Object).
 * ConsoleUI는 이 객체의 값만 그대로 출력하면 되므로, 계산 로직과 출력 로직이 완전히 분리된다.
 */
public final class MatchReport {

    private final Persona chattedPersona;
    private final double syncPercentWithChattedPersona;
    private final Optional<Persona> betterMatch;
    private final double betterMatchPercent;

    public MatchReport(Persona chattedPersona,
                        double syncPercentWithChattedPersona,
                        Optional<Persona> betterMatch,
                        double betterMatchPercent) {
        this.chattedPersona = chattedPersona;
        this.syncPercentWithChattedPersona = syncPercentWithChattedPersona;
        this.betterMatch = betterMatch;
        this.betterMatchPercent = betterMatchPercent;
    }

    public Persona getChattedPersona() {
        return chattedPersona;
    }

    public double getSyncPercentWithChattedPersona() {
        return syncPercentWithChattedPersona;
    }

    /** 대화 상대보다 사용자 성향과 더 잘 맞는 페르소나가 전체 목록에 있다면 반환한다. */
    public Optional<Persona> getBetterMatch() {
        return betterMatch;
    }

    public double getBetterMatchPercent() {
        return betterMatchPercent;
    }
}
