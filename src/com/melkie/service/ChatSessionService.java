package com.melkie.service;

import com.melkie.engine.AnalysisResult;
import com.melkie.engine.MatchEngine;
import com.melkie.engine.MeltingContext;
import com.melkie.engine.TextAnalysisEngine;
import com.melkie.llm.GroqDialogueService;
import com.melkie.model.GameConfig;
import com.melkie.model.Persona;

import java.util.List;
import java.util.Optional;

/**
 * 1:1 채팅 세션의 유스케이스(비즈니스 로직)를 담당하는 서비스.
 * TextAnalysisEngine(텍스트 분석/텐션 판정), MatchEngine(코사인 유사도),
 * GeminiDialogueService(자연어 대사 생성)를 조합하여 "한 턴 처리"와
 * "최종 리포트 생성"을 제공한다.
 *
 * 이 클래스는 System.out/Scanner를 전혀 알지 못한다 — 화면에 무엇을, 어떻게 보여줄지는
 * 전적으로 ui.ConsoleUI(프레젠테이션 계층)의 책임이다. 덕분에 이 서비스는 콘솔이 아닌
 * 다른 UI(웹/GUI)에서도 그대로 재사용할 수 있고, 단위 테스트도 I/O 없이 가능하다.
 */
public class ChatSessionService {

    private final TextAnalysisEngine textAnalysisEngine;
    private final GroqDialogueService dialogueService;
    private final List<Persona> allPersonas;

    public ChatSessionService(TextAnalysisEngine textAnalysisEngine,
                               GroqDialogueService dialogueService,
                               List<Persona> allPersonas) {
        this.textAnalysisEngine = textAnalysisEngine;
        this.dialogueService = dialogueService;
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
     * 유저 발화(또는 메타 지시문)에 대한 페르소나의 다음 대사를 생성한다.
     * Gemini가 활성화되어 있고 호출이 성공하면 그 결과를 그대로 반환하고,
     * 실패/비활성 상태라면 JSON에 정의된 폴백 대사 + 접두사 조합으로 대체한다.
     * 반환값은 항상 화면에 바로 출력 가능한 완성된 한 줄이다.
     */
    public String generateReply(MeltingContext session, Persona persona, GameConfig config, String userInput) {
        String llmReply = dialogueService.generateReply(persona, session, userInput);
        if (llmReply != null && !llmReply.isBlank()) {
            return llmReply;
        }
        return fallbackLine(session, persona, config);
    }

    /** JSON personas.json의 dialogues + prefixes만으로 구성한 폴백 대사 한 줄. */
    public String fallbackLine(MeltingContext session, Persona persona, GameConfig config) {
        String tierKey = session.getState().tierKey();
        String prefix = config.randomPrefix(tierKey, session.getLastDelta());
        String line = persona.randomLine(tierKey);
        return prefix.isEmpty() ? line : "(" + prefix + ") " + line;
    }

    public boolean isLlmEnabled() {
        return dialogueService.isEnabled();
    }
    /**
     * 현재까지 누적된 성향 벡터를 바탕으로 매칭 리포트를 생성한다.
     * 대화 상대(persona)보다 사용자 성향과 더 잘 맞는 페르소나가 전체 목록에 있다면 함께 알려준다.
     */
    public MatchReport buildReport(MeltingContext session, Persona persona) {
        // 1. 순수 벡터 기반 코사인 유사도 계산
        double syncPercent = MatchEngine.similarityPercent(session.getUserVector(), persona.getTargetVector());

        // 2. 텐션(호감도) 기반 공통 페널티 비율 계산 (대화를 망쳤을 경우 방어)
        double penaltyRatio = 1.0;
        if (session.getTension() < 30) {
            penaltyRatio = Math.max(0.0, (session.getTension() + 10) / 100.0);
        }

        // 대화한 페르소나에게 페널티 적용
        syncPercent = syncPercent * penaltyRatio;

        Persona best = null;
        double bestPercent = -1.0;
        for (Persona candidate : allPersonas) {
            double candidatePercent = MatchEngine.similarityPercent(session.getUserVector(), candidate.getTargetVector());
            
            // [핵심 수정] 다른 후보들을 탐색할 때도 유저의 트롤링 페널티를 똑같이 적용!
            candidatePercent = candidatePercent * penaltyRatio; 

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
