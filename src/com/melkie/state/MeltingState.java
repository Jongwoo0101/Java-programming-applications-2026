package com.melkie.state;

/**
 * 감정 상태를 통제하는 상태 패턴(State Pattern)의 핵심 인터페이스.
 * 밀당(Chilly) -> 능글(Flirty) -> 치명(Steamy) 로 이어지는 상태 전이를
 * 다형성(Polymorphism)으로 완벽히 통제하여, 논리적 흐름이 끊기는 것을 원천 차단한다.
 */
public interface MeltingState {

    /** personas.json의 dialogues 맵 키("CHILLY"/"FLIRTY"/"STEAMY")와 매핑되는 값 */
    String tierKey();

    /** 사용자에게 보여줄 한글 상태명 (예: "밀당", "능글", "치명") */
    String displayName();

    /**
     * 누적 텐션 점수(0~100)를 바탕으로 다음 상태를 판단한다.
     * 상태는 한 번 올라가면 내려가지 않는 단방향 전이(one-way escalation)를 따른다.
     */
    MeltingState evaluateTransition(int tension);
}
