package com.melkie.llm;

import com.melkie.data.SimpleJsonParser;
import com.melkie.engine.ChatTurn;
import com.melkie.engine.MeltingContext;
import com.melkie.model.Persona;

import java.util.List;
import java.util.Map;

public class GroqDialogueService {

    private static final double TEMPERATURE = 0.95;
    private static final int MAX_OUTPUT_TOKENS = 220;

    private final GroqClient client;
    private final boolean enabled;

    public GroqDialogueService(GroqClient client, boolean enabled) {
        this.client = client;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String generateReply(Persona persona, MeltingContext session, String userInput) {
        if (!enabled) {
            return null;
        }
        try {
            String requestBody = buildRequestBody(persona, session, userInput);
            String rawResponse = client.generateContent(requestBody);
            String text = extractText(rawResponse);
            return (text == null || text.isBlank()) ? null : text;
        } catch (Exception e) {
            System.out.println("[시스템] (Groq 응답 실패로 기본 대사로 대체합니다: " + e.getMessage() + ")");
            return null;
        }
    }

    private String buildRequestBody(Persona persona, MeltingContext session, String userInput) {
        String systemPrompt = buildSystemPrompt(persona, session);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"").append(client.getModel()).append("\",");
        sb.append("\"temperature\":").append(TEMPERATURE).append(",");
        sb.append("\"max_tokens\":").append(MAX_OUTPUT_TOKENS).append(",");
        sb.append("\"messages\":[");

        // 1. System Prompt
        sb.append("{\"role\":\"system\",\"content\":\"").append(escape(systemPrompt)).append("\"}");

        // 2. Chat History (Groq는 AI 응답을 'assistant' role로 사용합니다)
        for (ChatTurn turn : session.getHistory()) {
            sb.append(",");
            sb.append("{\"role\":\"user\",\"content\":\"").append(escape(turn.getUserText())).append("\"}");
            sb.append(",");
            sb.append("{\"role\":\"assistant\",\"content\":\"").append(escape(turn.getPersonaText())).append("\"}");
        }

        // 3. Current User Input
        sb.append(",");
        sb.append("{\"role\":\"user\",\"content\":\"").append(escape(userInput)).append("\"}");

        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String buildSystemPrompt(Persona persona, MeltingContext session) {
        String tierKey = session.getState().tierKey();
        String tierName = session.getState().displayName();

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 연애 시뮬레이션 게임 속 캐릭터 '").append(persona.getName()).append("'입니다.\n");
        sb.append("나이: ").append(persona.getAge()).append("세, MBTI: ").append(persona.getMbti()).append("\n");
        sb.append("한 줄 소개: ").append(persona.getTagline()).append("\n");
        sb.append("현재 유저와의 관계 단계는 '").append(tierName).append("'(내부 키 ").append(tierKey)
                .append(")이며, 누적 호감 텐션은 100점 만점에 ").append(session.getTension()).append("점입니다.\n");
        sb.append("이 캐릭터가 좋아하는 화제/표현: ").append(joinKeys(persona.getLoveKeywords())).append("\n");
        sb.append("이 캐릭터가 싫어하는 화제/표현: ").append(joinKeys(persona.getHateKeywords())).append("\n");
        sb.append("답변 규칙:\n");
        sb.append("1) 이 캐릭터의 나이/MBTI/성격에 어울리는 말투(반말 또는 존댓말)를 항상 유지하세요.\n");
        sb.append("2) 관계 단계에 맞는 애정 표현 수위를 지키세요. 단계가 낮을 땐 과하게 들이대지 마세요.\n");
        sb.append("3) 답변은 실제 메신저 대화처럼 1~3문장 내외의 짧고 자연스러운 구어체로 작성하세요.\n");
        sb.append("4) 문장 맨 앞에 괄호로 짧은 행동/표정 묘사를 하나만 넣으세요. 예: (살짝 웃으며)\n");
        sb.append("5) 유저의 발화 내용에 실제로 반응하는 문맥 있는 대답을 하세요. 동문서답하지 마세요.\n");
        return sb.toString();
    }

    private String joinKeys(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return "(특별히 없음)";
        return String.join(", ", map.keySet());
    }

    @SuppressWarnings("unchecked")
    private String extractText(String rawJson) {
        Object parsed = SimpleJsonParser.parse(rawJson);
        if (!(parsed instanceof Map)) {
            throw new IllegalStateException("Groq 응답 형식이 올바르지 않습니다.");
        }
        Map<String, Object> root = (Map<String, Object>) parsed;

        // OpenAI 호환 규격: choices[0].message.content
        List<Object> choices = (List<Object>) root.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Groq 응답에 choices가 없습니다.");
        }
        Map<String, Object> firstChoice = (Map<String, Object>) choices.get(0);

        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        if (message == null) {
            throw new IllegalStateException("Groq 응답에 message가 없습니다.");
        }
        
        Object textObj = message.get("content");
        if (!(textObj instanceof String)) {
            throw new IllegalStateException("Groq 응답 content가 비어 있습니다.");
        }
        return ((String) textObj).trim();
    }

    private String escape(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}