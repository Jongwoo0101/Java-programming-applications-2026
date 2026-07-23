package com.melkie.engine;

import com.melkie.model.Persona;
import com.melkie.state.ChillyState;
import com.melkie.state.MeltingState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 한 명의 페르소나와의 1:1 채팅 세션을 통제하는 컨텍스트(Context) 클래스.
 * 상태 패턴(State Pattern)의 Context 역할을 수행하며, 텐션 점수와 유저의
 * 3차원 성향 벡터[주도성, 순종성, 적극성]를 누적 관리한다.
 *
 * 이 세션에서 오간 대화 기록(history)을 함께 보관한다.
 * LLM API는 상태가 없는(stateless) 호출이므로, 이전 대화 맥락을
 * 매 요청마다 넘겨줘야 자연스러운 멀티턴 대화가 이어진다.
 */
public class MeltingContext {

    private final Persona persona;
    private MeltingState state = ChillyState.INSTANCE;
    private int tension = 0;
    private final double[] userVector = new double[3]; // [주도성, 순종성, 적극성]
    private int lastDelta = 0;
    
    // 멀티턴 대화를 위한 기록 리스트
    private final List<ChatTurn> history = new ArrayList<>();

    public MeltingContext(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() { return persona; }
    public MeltingState getState() { return state; }
    public int getTension() { return tension; }
    public double[] getUserVector() { return userVector; }
    public int getLastDelta() { return lastDelta; }

    /** 분석 결과를 세션에 반영하고, 다형성을 통해 상태 전이를 평가한다. */
    public void applyResult(AnalysisResult result) {
        this.lastDelta = result.getTensionDelta();
        this.tension = clamp(this.tension + result.getTensionDelta(), 0, 100);

        userVector[0] += result.getDominanceDelta();
        userVector[1] += result.getSubmissionDelta();
        userVector[2] += result.getAssertivenessDelta();

        // 다형성에 의한 상태 전이 평가 (if-else 없이 각 상태 객체가 스스로 판단)
        this.state = state.evaluateTransition(tension);
    }

    /** 이번 턴의 (유저 발화, 페르소나 응답)을 대화 기록에 추가한다. */
    public void recordTurn(String userText, String personaText) {
        history.add(new ChatTurn(userText, personaText));
    }

    /** LLM 요청 시 함께 전달할 지금까지의 대화 기록 (읽기 전용). */
    public List<ChatTurn> getHistory() {
        return Collections.unmodifiableList(history);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}