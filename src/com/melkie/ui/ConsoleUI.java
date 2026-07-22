package com.melkie.ui;

import com.melkie.engine.AnalysisResult;
import com.melkie.engine.MeltingContext;
import com.melkie.model.Gender;
import com.melkie.model.GameConfig;
import com.melkie.model.GameData;
import com.melkie.model.Persona;
import com.melkie.service.ChatBranchChoice;
import com.melkie.service.ChatSessionService;
import com.melkie.service.MatchReport;
import com.melkie.service.PersonaBrowseService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * 무거운 그래픽 렌더링 없이, 순수 Scanner + System.out.println 만으로 구동되는
 * CUI(Command-line User Interface) 프레젠테이션 계층.
 *
 * 이 클래스의 책임은 오직 "입력을 읽고 출력을 그리는 것"뿐이다.
 * 텐션 계산, 상태 전이, 코사인 유사도 같은 비즈니스 로직은 전혀 알지 못하며,
 * 전부 {@link ChatSessionService} / {@link PersonaBrowseService}에 위임한다.
 *
 * 전체 흐름:
 *   [성별 선택] -> [틴더 탐색: L/R/Q] -> [매칭 시 기본 {@value #INITIAL_TURNS}턴 대화]
 *   -> [턴 종료 시 분기: 1.결과보기 / 2.더답변하기 / 3.매칭하기]
 */
public class ConsoleUI {

    /** 대화 시작 시 기본으로 진행하는 턴 수 */
    private static final int INITIAL_TURNS = 7;

    /** "더 답변하기"를 선택했을 때 추가되는 턴 수 */
    private static final int EXTEND_TURNS = 3;

    private final GameData gameData;
    private final Scanner scanner;
    private final ChatSessionService chatSessionService;

    public ConsoleUI(GameData gameData, Scanner scanner, ChatSessionService chatSessionService) {
        this.gameData = gameData;
        this.scanner = scanner;
        this.chatSessionService = chatSessionService;
    }

    public void run() {
        printBanner();

        List<Persona> candidates = selectBrowseTargets();
        if (candidates.isEmpty()) {
            System.out.println("[시스템] 조건에 맞는 페르소나가 없습니다. 프로그램을 종료합니다.");
            return;
        }

        browseAndMatch(candidates);

        System.out.println("\n[시스템] MelKie를 종료합니다. 좋은 인연 되세요!");
    }

    // ------------------------------------------------------------
    // 1) 성별 선택 & 탐색 대상 필터링
    // ------------------------------------------------------------

    private List<Persona> selectBrowseTargets() {
        System.out.print("[시스템] 매칭을 원하는 상대방의 성별을 선택해주세요 (M: 남성 / F: 여성 / A: 무관) > ");
        String rawChoice = safeNextLine();

        Optional<Gender> genderFilter = PersonaBrowseService.parseGenderChoice(rawChoice);
        printGenderFilterNotice(genderFilter);

        return PersonaBrowseService.filterByGender(gameData.getPersonas(), genderFilter);
    }

    private void printGenderFilterNotice(Optional<Gender> genderFilter) {
        if (genderFilter.isEmpty()) {
            System.out.println("\n[시스템] 성별 무관으로 전체 페르소나를 탐색합니다.");
            return;
        }
        String label = genderFilter.get() == Gender.MALE ? "남성" : "여성";
        System.out.println("\n[시스템] '" + label + "' 페르소나들만 탐색합니다.");
    }

    // ------------------------------------------------------------
    // 2) 틴더 스타일 탐색 루프 (L: 다음 / R: 매칭 / Q: 종료)
    // ------------------------------------------------------------

    private void browseAndMatch(List<Persona> candidates) {
        int index = 0;
        while (index < candidates.size()) {
            Persona persona = candidates.get(index);
            printPersonaCard(persona, index + 1, candidates.size());
            System.out.print("[L: 다음(Next) / R: 매칭(Match) / Q: 종료(Quit)] > ");

            String input = safeNextLine().trim().toUpperCase(Locale.ROOT);
            if (input.equals("Q")) {
                return;
            }
            if (input.equals("R")) {
                boolean finalMatchConfirmed = runChatSession(persona);
                if (finalMatchConfirmed) {
                    System.out.println("\n[시스템] 최종 매칭이 성사되어 탐색을 종료합니다.");
                    return;
                }
                index++; // "결과 보기"로 마무리되었으면 다음 상대로 계속 탐색
            } else {
                System.out.println("[시스템] " + persona.getName() + "님을 넘겼습니다.\n");
                index++;
            }
        }
    }

    private void printBanner() {
        System.out.println("==================================================");
        System.out.println("   MelKie (멜키) - 순수 자바 코어 엔진 페르소나 매칭 시뮬레이터");
        System.out.println("==================================================");
    }

    private void printPersonaCard(Persona persona, int order, int total) {
        System.out.println("--------------------------------------------------");
        System.out.println("[" + order + "/" + total + "] " + persona.displayNameWithGender()
                + " (" + persona.getAge() + "세 / " + persona.getMbti() + ")");
        System.out.println("     " + persona.getTagline());
        System.out.println("--------------------------------------------------");
    }

    // ------------------------------------------------------------
    // 3) 채팅 세션 (기본 7턴 + 결과보기/더답변하기/매칭하기 분기)
    // ------------------------------------------------------------

    /**
     * 채팅 세션을 진행한다.
     *
     * @return 사용자가 "매칭하기"를 선택해 최종 매칭이 확정되면 true, "결과 보기"로 세션이
     *         종료되면 false.
     */
    private boolean runChatSession(Persona persona) {
        MeltingContext session = chatSessionService.openSession(persona);
        System.out.println();
        System.out.println("[시스템] '" + persona.displayNameWithGender()
                + "'님과의 1:1 채팅방이 열렸습니다. (현재 텐션: 0)");

        int completedTurns = 0;
        int targetTurns = INITIAL_TURNS;

        while (true) {
            completedTurns = runTurns(session, persona, completedTurns, targetTurns);

            ChatBranchChoice choice = askBranchChoice(targetTurns);
            switch (choice) {
                case SHOW_RESULT:
                    presentResult(session, persona);
                    return false;
                case EXTEND_CHAT:
                    targetTurns += EXTEND_TURNS;
                    System.out.println("\n[시스템] 대화가 " + EXTEND_TURNS + "턴 추가되었습니다. 대화를 이어갑니다.");
                    break;
                case CONFIRM_MATCH:
                    presentFinalMatchConfirmation(session, persona);
                    return true;
                case INVALID:
                default:
                    System.out.println("\n[시스템] 잘못된 입력입니다. 1, 2, 3 중에서 하나를 선택해주세요.");
                    break;
            }
        }
    }

    /** fromTurn(포함) ~ toTurn(포함) 구간의 대화를 진행하고, 완료된 턴 수를 반환한다. */
    private int runTurns(MeltingContext session, Persona persona, int fromTurn, int toTurn) {
        int turn = fromTurn;
        while (turn < toTurn) {
            turn++;
            speak(session, persona);

            System.out.print("[유저 입력 (" + turn + "/" + toTurn + "턴)] > ");
            String userInput = safeNextLine();

            AnalysisResult result = chatSessionService.processTurn(session, persona, userInput);
            printEngineTrace(result, session);
        }
        return turn;
    }

    private void printEngineTrace(AnalysisResult result, MeltingContext session) {
        String sign = result.getTensionDelta() >= 0 ? "+" : "";
        System.out.println("(내부 엔진 연산: 키워드 " + result.getMatchedKeywords()
                + " -> 텐션 " + sign + result.getTensionDelta()
                + " -> 누적 텐션 " + session.getTension() + ")\n");
    }

    private ChatBranchChoice askBranchChoice(int turnsSoFar) {
        System.out.println("==================================================");
        System.out.println("[시스템] " + turnsSoFar + "회의 대화가 모두 완료되었습니다.");
        System.out.println("[1] 결과 보기   (성향 분석 & 궁합도 리포트 출력 후 다음 상대로)");
        System.out.println("[2] 더 답변하기 (질문 " + EXTEND_TURNS + "회 추가 연장)");
        System.out.println("[3] 매칭하기    (최종 매칭 확정 & 대화 물꼬 틔우기)");
        System.out.println("==================================================");
        System.out.print("선택 > ");
        return ChatBranchChoice.fromInput(safeNextLine());
    }

    private void presentResult(MeltingContext session, Persona persona) {
        speak(session, persona); // 마지막 텐션에 맞는 대사 한 번 더 출력하고 마무리
        System.out.println("\n[시스템] 대화 종료. 코사인 유사도 분석 중...");

        MatchReport report = chatSessionService.buildReport(session, persona);
        System.out.printf(Locale.KOREA, "[결과] 당신과 %s의 페르소나 싱크로율은 %.1f%% 입니다!%n",
                persona.displayNameWithGender(), report.getSyncPercentWithChattedPersona());

        report.getBetterMatch().ifPresent(best ->
                System.out.printf(Locale.KOREA,
                        "[참고] 전체 페르소나 중 당신의 성향과 가장 잘 맞는 상대는 %s (%.1f%%) 입니다.%n",
                        best.displayNameWithGender(), report.getBetterMatchPercent()));

        System.out.println();
    }

    private void presentFinalMatchConfirmation(MeltingContext session, Persona persona) {
        System.out.println("\n==================================================");
        System.out.println("[시스템] 최종 매칭이 확정되었습니다!");
        System.out.println("==================================================");

        GameConfig config = gameData.getConfig();
        String tierKey = session.getState().tierKey();
        String prefix = config.randomPrefix(tierKey, session.getLastDelta());
        String prefixPart = prefix.isEmpty() ? "" : "(" + prefix + ") ";

        System.out.println("[" + persona.displayNameWithGender() + " - " + session.getState().displayName() + "]: "
                + prefixPart + "우리 이제 진짜 시작이네. 앞으로 잘 부탁해!");
        System.out.println("[시스템] " + persona.getName() + "님과의 새로운 인연이 시작됩니다...\n");
    }

    private void speak(MeltingContext session, Persona persona) {
        GameConfig config = gameData.getConfig();
        String tierKey = session.getState().tierKey();
        String prefix = config.randomPrefix(tierKey, session.getLastDelta());
        String line = persona.randomLine(tierKey);
        String prefixPart = prefix.isEmpty() ? "" : "(" + prefix + ") ";

        System.out.println("[" + persona.displayNameWithGender() + " - " + session.getState().displayName() + "]: "
                + prefixPart + line);
    }

    // ------------------------------------------------------------
    // 공통 유틸
    // ------------------------------------------------------------

    private String safeNextLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "";
    }
}
