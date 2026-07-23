package com.melkie.engine;

/**
 * Gemini에게 멀티턴 대화 맥락을 넘겨주기 위해, 한 턴에 오간
 * (유저 발화, 페르소나 응답) 쌍을 기록하는 불변 값 객체.
 */
public final class ChatTurn {

    private final String userText;
    private final String personaText;

    public ChatTurn(String userText, String personaText) {
        this.userText = userText;
        this.personaText = personaText;
    }

    public String getUserText() { return userText; }
    public String getPersonaText() { return personaText; }
}
