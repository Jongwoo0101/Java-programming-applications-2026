package com.melkie.service;

import com.melkie.engine.AnalysisResult;
import com.melkie.engine.MatchEngine;
import com.melkie.engine.MeltingContext;
import com.melkie.engine.TextAnalysisEngine;
import com.melkie.model.Persona;

import java.util.List;
import java.util.Optional;

/**
 * 1:1 채팅 세션의 유스케이스(비즈니스 로직)를 담당하는 서비스.
 * TextAnalysisEngine(텍스트 분석)과 MatchEngine(코사인 유사도)을 조합하여
 * "한 턴 처리"와 "최종 리포트 생성"을 제공한다.
 *
 * 이 클래스는 System.out/Scanner를 전혀 알지 못한다 — 화면에 무엇을, 어떻게 보여줄지는
 * 전적으로 ui.ConsoleUI(프레젠테이션 계층)의 책임이다. 덕분에 이 서비스는 콘솔이 아닌
 * 다른 UI(웹/GUI)에서도 그대로 재사용할 수 있고, 단위 테스트도 I/O 없이 가능하다.
 */
public class ChatSessionService {

    private final TextAnalysisEngine textAnalysisEngine;
    private final List<Persona> allPersonas;

    public ChatSessionService(TextAnalysisEngine textAnalysisEngine, List<Persona> allPersonas) {
        this.textAnalysisEngine = textAnalysisEngine;
        this.allPersonas = allPersonas;
    }

    /** 특정 페르소나와의 새로운 채팅 세션(컨텍스트)을 연다. */
    public MeltingContext openSession(Persona persona) {
        return new MeltingContext(persona);
    }

    /** 유저의 한 마디를 분석하여 세션 상태(텐션, 성향 벡터, 상태 전이)에 반영한다. */
    public AnalysisResult processTurn(MeltingContext session, Persona persona, String userInput) {
        AnalysisResult result = textAnalysisEngine.analyze(userInput, persona);
        session.applyResult(result);
        return result;
    }

    /**
     * 현재까지 누적된 성향 벡터를 바탕으로 매칭 리포트를 생성한다.
     * 대화 상대(persona)보다 사용자 성향과 더 잘 맞는 페르소나가 전체 목록에 있다면 함께 알려준다.
     */
    public MatchReport buildReport(MeltingContext session, Persona persona) {
        double syncPercent = MatchEngine.similarityPercent(session.getUserVector(), persona.getTargetVector());

        Persona best = null;
        double bestPercent = -1.0;
        for (Persona candidate : allPersonas) {
            double candidatePercent = MatchEngine.similarityPercent(session.getUserVector(), candidate.getTargetVector());
            if (candidatePercent > bestPercent) {
                bestPercent = candidatePercent;
                best = candidate;
            }
        }

        boolean hasBetterMatch = best != null && best != persona;
        Optional<Persona> betterMatch = hasBetterMatch ? Optional.of(best) : Optional.empty();
        double betterMatchPercent = hasBetterMatch ? bestPercent : 0.0;

        return new MatchReport(persona, syncPercent, betterMatch, betterMatchPercent);
    }
}
