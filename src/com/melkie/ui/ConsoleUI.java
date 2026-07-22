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
 * 매칭된 페르소나와 기본 7턴의 1:1 채팅을 진행합니다.
 * 7턴 종료 후 [결과보기 / 더 답변하기 / 매칭하기] 분기를 제공합니다.
 */
public class ConsoleUI {

    private static final int INITIAL_TURNS = 7; // 기본 7턴 시작
    private static final int EXTEND_TURNS = 3;  // 연장 시 3턴씩 추가

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
                // startChat이 true를 반환하면 최종 매칭(3번 옵션)이 성사된 것이므로 탐색을 완전히 종료함
                boolean isFinalMatch = startChat(persona);
                if (isFinalMatch) {
                    System.out.println("\n[시스템] 최종 매칭이 성사되어 탐색을 종료합니다.");
                    break;
                }
                index++; // 1번 옵션(결과보기)로 끝났으면 다음 상대로 넘어감
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

    private String getGenderStr(String gender) {
        return "MALE".equalsIgnoreCase(gender) ? "남" : "여";
    }

    private void printPersonaCard(Persona persona, int order, int total) {
        System.out.println("--------------------------------------------------");
        System.out.println("[" + order + "/" + total + "] " + persona.getName()
                + "(" + getGenderStr(persona.getGender()) + ") "
                + "(" + persona.getAge() + "세 / " + persona.getMbti() + ")");
        System.out.println("     " + persona.getTagline());
        System.out.println("--------------------------------------------------");
    }

    /**
     * 채팅 세션을 시작하고, 최종 매칭 성공(옵션 3) 여부를 반환합니다.
     * @return true: 최종 매칭 성사, false: 결과만 보고 종료
     */
    private boolean startChat(Persona persona) {
        MeltingContext ctx = new MeltingContext(persona);
        System.out.println();
        System.out.println("[시스템] '" + persona.getName() + "(" + getGenderStr(persona.getGender()) + ")'님과의 1:1 채팅방이 열렸습니다. (현재 텐션: 0)");

        int currentTurn = 1;
        int targetTurns = INITIAL_TURNS;

        // 대화 전체를 제어하는 무한 루프
        while (true) {
            // 정해진 턴까지 대화 진행
            while (currentTurn <= targetTurns) {
                speak(ctx, persona);

                System.out.print("[유저 입력 (" + currentTurn + "/" + targetTurns + "턴)] > ");
                String userInput = safeNextLine();

                AnalysisResult result = engine.analyze(userInput, persona);
                ctx.applyResult(result);

                String sign = result.getTensionDelta() >= 0 ? "+" : "";
                System.out.println("(내부 엔진 연산: 키워드 " + result.getMatchedKeywords()
                        + " -> 텐션 " + sign + result.getTensionDelta()
                        + " -> 누적 텐션 " + ctx.getTension() + ")\n");

                currentTurn++;
            }

            // 정해진 턴 수 도달 시 분기점 제시
            System.out.println("==================================================");
            System.out.println("[시스템] " + targetTurns + "회의 대화가 모두 완료되었습니다.");
            System.out.println("[1] 결과 보기   (성향 분석 & 궁합도 리포트 출력 후 다음 상대로)");
            System.out.println("[2] 더 답변하기 (질문 " + EXTEND_TURNS + "회 추가 연장)");
            System.out.println("[3] 매칭하기    (최종 매칭 확정 & 대화 물꼬 틔우기)");
            System.out.println("==================================================");
            System.out.print("선택 > ");

            String choice = safeNextLine().trim();

            if (choice.equals("1")) {
                // 1. 결과 보기 (리포트 출력 후 탐색 계속)
                speak(ctx, persona); // 마지막 텐션에 맞는 대사 한 번 출력
                System.out.println("\n[시스템] 대화 종료. 코사인 유사도 분석 중...");
                double percent = MatchEngine.similarityPercent(ctx.getUserVector(), persona.getTargetVector());
                System.out.printf(Locale.KOREA, "[결과] 당신과 %s(%s)의 페르소나 싱크로율은 %.1f%% 입니다!%n",
                        persona.getName(), getGenderStr(persona.getGender()), percent);
                printBestOverallMatch(ctx, persona);
                System.out.println();
                return false;

            } else if (choice.equals("2")) {
                // 2. 더 답변하기 (턴 수 증가 후 내부 루프 재진입)
                targetTurns += EXTEND_TURNS;
                System.out.println("\n[시스템] 대화가 " + EXTEND_TURNS + "턴 추가되었습니다. 대화를 이어갑니다.");

            } else if (choice.equals("3")) {
                // 3. 매칭하기 (최종 확정)
                System.out.println("\n==================================================");
                System.out.println("[시스템] 💘 최종 매칭이 확정되었습니다! 💘");
                System.out.println("==================================================");
                // 현재 도달한 텐션 상태에 맞는 달콤한 멘트로 대화 물꼬 틔우기
                String tierKey = ctx.getState().tierKey();
                String prefix = gameData.getConfig().randomPrefix(tierKey, ctx.getLastDelta());
                String prefixPart = prefix.isEmpty() ? "" : "(" + prefix + ") ";
                System.out.println("[" + persona.getName() + "(" + getGenderStr(persona.getGender()) + ") - " + ctx.getState().displayName() + "]: "
                        + prefixPart + "우리 이제 진짜 시작이네. 앞으로 잘 부탁해!");
                System.out.println("[시스템] " + persona.getName() + "님과의 새로운 인연이 시작됩니다...\n");
                return true;

            } else {
                System.out.println("\n[시스템] 잘못된 입력입니다. 1, 2, 3 중에서 하나를 선택해주세요.");
                // 루프가 반복되며 다시 선택지를 묻습니다.
            }
        }
    }

    private void speak(MeltingContext ctx, Persona persona) {
        String tierKey = ctx.getState().tierKey();
        String prefix = gameData.getConfig().randomPrefix(tierKey, ctx.getLastDelta());
        String line = persona.randomLine(tierKey);
        String prefixPart = prefix.isEmpty() ? "" : "(" + prefix + ") ";

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