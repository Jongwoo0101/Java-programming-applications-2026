package com.melkie.ui;

import com.melkie.engine.AnalysisResult;
import com.melkie.engine.MatchEngine;
import com.melkie.engine.MeltingContext;
import com.melkie.engine.TextAnalysisEngine;
import com.melkie.model.GameData;
import com.melkie.model.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * 무거운 그래픽 렌더링 없이, 순수 Scanner + System.out.println 만으로 구동되는
 * CUI(Command-line User Interface). 틴더 스타일 탐색(L: Next / R: Match) 후,
 * 매칭된 페르소나와 7턴의 1:1 채팅을 진행하고 코사인 유사도로 싱크로율을 산출한다.
 */
public class ConsoleUI {

    // [수정 포인트 1] 대화 턴 수를 3턴에서 7턴으로 증가
    private static final int TOTAL_TURNS = 7;

    private final GameData gameData;
    private final Scanner scanner;
    private final TextAnalysisEngine engine;

    public ConsoleUI(GameData gameData, Scanner scanner) {
        this.gameData = gameData;
        this.scanner = scanner;
        this.engine = new TextAnalysisEngine(gameData.getConfig());
    }

    public void run() {
        printBanner();

        // [수정 포인트 2] 프로그램 시작 시 성별 선택 로직 추가
        System.out.print("[시스템] 매칭을 원하는 상대방의 성별을 선택해주세요 (M: 남성 / F: 여성 / A: 무관) > ");
        String genderChoice = safeNextLine().trim().toUpperCase(Locale.ROOT);

        String targetGender = null;
        if (genderChoice.equals("M") || genderChoice.equals("남")) {
            targetGender = "MALE";
            System.out.println("\n[시스템] '남성' 페르소나들만 탐색합니다.");
        } else if (genderChoice.equals("F") || genderChoice.equals("여")) {
            targetGender = "FEMALE";
            System.out.println("\n[시스템] '여성' 페르소나들만 탐색합니다.");
        } else {
            System.out.println("\n[시스템] 성별 무관으로 전체 페르소나를 탐색합니다.");
        }

        // 선택한 성별에 맞게 페르소나 리스트 필터링
        List<Persona> personas = new ArrayList<>();
        for (Persona p : gameData.getPersonas()) {
            if (targetGender == null || p.getGender().equals(targetGender)) {
                personas.add(p);
            }
        }

        if (personas.isEmpty()) {
            System.out.println("[시스템] 조건에 맞는 페르소나가 없습니다. 프로그램을 종료합니다.");
            return;
        }

        int index = 0;
        while (index < personas.size()) {
            Persona persona = personas.get(index);
            printPersonaCard(persona, index + 1, personas.size());
            System.out.print("[L: 다음(Next) / R: 매칭(Match) / Q: 종료(Quit)] > ");

            String input = safeNextLine().trim().toUpperCase(Locale.ROOT);
            if (input.equals("Q")) {
                break;
            } else if (input.equals("R")) {
                startChat(persona);
                index++;
            } else {
                System.out.println("[시스템] " + persona.getName() + "님을 넘겼습니다.\n");
                index++;
            }
        }

        System.out.println("\n[시스템] MelKie를 종료합니다. 좋은 인연 되세요!");
    }

    private void printBanner() {
        System.out.println("==================================================");
        System.out.println("   MelKie (멜키) - 순수 자바 코어 엔진 페르소나 매칭 시뮬레이터");
        System.out.println("==================================================");
    }

    // [수정 포인트 3] 성별 텍스트 변환 헬퍼 메서드
    private String getGenderStr(String gender) {
        return "MALE".equalsIgnoreCase(gender) ? "남" : "여";
    }

    private void printPersonaCard(Persona persona, int order, int total) {
        System.out.println("--------------------------------------------------");
        // 프로필 카드에도 성별 표시 추가
        System.out.println("[" + order + "/" + total + "] " + persona.getName()
                + "(" + getGenderStr(persona.getGender()) + ") "
                + "(" + persona.getAge() + "세 / " + persona.getMbti() + ")");
        System.out.println("     " + persona.getTagline());
        System.out.println("--------------------------------------------------");
    }

    private void startChat(Persona persona) {
        MeltingContext ctx = new MeltingContext(persona);
        System.out.println();
        // 채팅방 입장 메시지에 성별 표시 추가
        System.out.println("[시스템] '" + persona.getName() + "(" + getGenderStr(persona.getGender()) + ")'님과의 1:1 채팅방이 열렸습니다. (현재 텐션: 0)");

        for (int turn = 1; turn <= TOTAL_TURNS; turn++) {
            speak(ctx, persona);

            System.out.print("[유저 입력 (" + turn + "/" + TOTAL_TURNS + "턴)] > ");
            String userInput = safeNextLine();

            AnalysisResult result = engine.analyze(userInput, persona);
            ctx.applyResult(result);

            String sign = result.getTensionDelta() >= 0 ? "+" : "";
            System.out.println("(내부 엔진 연산: 키워드 " + result.getMatchedKeywords()
                    + " -> 텐션 " + sign + result.getTensionDelta()
                    + " -> 누적 텐션 " + ctx.getTension() + ")");

            if (turn < TOTAL_TURNS) {
                System.out.println();
            }
        }

        // 마지막 반응 한 번 더 출력 (최종 상태의 대사로 대화 마무리)
        System.out.println();
        speak(ctx, persona);

        System.out.println("\n[시스템] 대화 종료. 코사인 유사도 분석 중...");
        double percent = MatchEngine.similarityPercent(ctx.getUserVector(), persona.getTargetVector());
        System.out.printf(Locale.KOREA, "[결과] 당신과 %s(%s)의 페르소나 싱크로율은 %.1f%% 입니다!%n",
                persona.getName(), getGenderStr(persona.getGender()), percent);

        printBestOverallMatch(ctx, persona);
        System.out.println();
    }

    private void speak(MeltingContext ctx, Persona persona) {
        String tierKey = ctx.getState().tierKey();
        String prefix = gameData.getConfig().randomPrefix(tierKey, ctx.getLastDelta());
        String line = persona.randomLine(tierKey);
        String prefixPart = prefix.isEmpty() ? "" : "(" + prefix + ") ";

        // 채팅 출력 포맷: [이름(성별) - 상태]: (행동) 대사
        System.out.println("[" + persona.getName() + "(" + getGenderStr(persona.getGender()) + ") - " + ctx.getState().displayName() + "]: "
                + prefixPart + line);
    }

    private void printBestOverallMatch(MeltingContext ctx, Persona current) {
        Persona best = null;
        double bestPercent = -1.0;
        for (Persona candidate : gameData.getPersonas()) {
            double pct = MatchEngine.similarityPercent(ctx.getUserVector(), candidate.getTargetVector());
            if (pct > bestPercent) {
                bestPercent = pct;
                best = candidate;
            }
        }
        if (best != null && best != current) {
            System.out.printf(Locale.KOREA, "[참고] 전체 페르소나 중 당신의 성향과 가장 잘 맞는 상대는 %s(%s) (%.1f%%) 입니다.%n",
                    best.getName(), getGenderStr(best.getGender()), bestPercent);
        }
    }

    private String safeNextLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }
}