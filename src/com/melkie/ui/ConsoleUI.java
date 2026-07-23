package com.melkie.ui;

import com.melkie.engine.AnalysisResult;
import com.melkie.engine.MeltingContext;
import com.melkie.model.Gender;
import com.melkie.model.GameData;
import com.melkie.model.Persona;
import com.melkie.service.ChatBranchChoice;
import com.melkie.service.ChatSessionService;
import com.melkie.service.MatchReport;
import com.melkie.service.PersonaBrowseService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * 무거운 그래픽 렌더링 없이, 순수 Scanner + System.out.println 만으로 구동되는
 * CUI(Command-line User Interface) 프레젠테이션 계층.
 *
 * 이 클래스의 책임은 오직 "입력을 읽고 출력을 그리는 것"뿐이다.
 * 텐션 계산, 상태 전이, 코사인 유사도, LLM 호출 같은 로직은 전혀 알지 못하며,
 * 전부 {@link ChatSessionService} / {@link PersonaBrowseService}에 위임한다.
 *
 * [변경사항]
 * 1) 기존 "L(다음)/R(매칭)/Q(종료)" 틴더 스타일 1개씩 넘기기 방식을 폐기하고,
 *    선택한 성별에 맞는 페르소나 전체를 번호 매긴 목록으로 한 번에 보여준 뒤
 *    번호를 입력해 바로 대화를 시작하는 방식으로 변경했다.
 * 2) 페르소나의 대사를 JSON 무작위 추출 대신 Gemini API로 실시간 생성하여
 *    유저가 방금 한 말의 문맥에 맞는 자연스러운 답변을 받아온다.
 * 3) 타이핑 효과와 "생각 중" 지연을 넣어 실제 메신저 대화 같은 리듬을 살렸다.
 *
 * 전체 흐름:
 *   [성별 선택] -> [번호 목록에서 상대 선택] -> [매칭 시 기본 {@value #INITIAL_TURNS}턴 대화]
 *   -> [턴 종료 시 분기: 1.결과보기 / 2.더답변하기 / 3.매칭하기]
 */
public class ConsoleUI {

    /** 대화 시작 시 기본으로 진행하는 턴 수 */
    private static final int INITIAL_TURNS = 7;

    /** "더 답변하기"를 선택했을 때 추가되는 턴 수 */
    private static final int EXTEND_TURNS = 3;

    /** 한 글자씩 출력되는 타이핑 효과 속도 (ms/글자) */
    private static final long TYPING_DELAY_MS = 18;

    /** 페르소나가 "생각하는" 것처럼 보이도록 응답 전에 주는 지연 시간 (ms) */
    private static final long THINKING_DELAY_MS = 550;

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
    // 2) 번호 목록에서 상대 선택 (기존 L/R/Q 틴더 브라우징을 대체)
    // ------------------------------------------------------------

    private void browseAndMatch(List<Persona> candidates) {
        List<Persona> remaining = new ArrayList<>(candidates);

        while (!remaining.isEmpty()) {
            printPersonaList(remaining);
            System.out.print("[대화할 상대의 번호를 입력하세요 (0: 종료)] > ");
            String rawInput = safeNextLine().trim();

            if (rawInput.equals("0") || rawInput.equalsIgnoreCase("Q")) {
                return;
            }

            int index = parseIndexChoice(rawInput, remaining.size());
            if (index < 0) {
                System.out.println("[시스템] 잘못된 입력입니다. 목록에 있는 번호를 입력해주세요.\n");
                continue;
            }

            Persona persona = remaining.get(index);
            boolean finalMatchConfirmed = runChatSession(persona);
            if (finalMatchConfirmed) {
                System.out.println("\n[시스템] 최종 매칭이 성사되어 탐색을 종료합니다.");
                return;
            }
            System.out.println(); // "결과 보기"로 세션이 끝나면 목록을 다시 보여주고 계속 탐색
        }
    }

    /**
     * ex)
     * [1] 신유나(여) (22세 / ESFP) 앙큼당돌 댄서
     * [2] 서연(여) (24세 / ISTJ) 반전 츤데레 선배
     * ------
     */
    private void printPersonaList(List<Persona> candidates) {
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < candidates.size(); i++) {
            Persona p = candidates.get(i);
            System.out.printf(Locale.KOREA, "[%d] %s (%d세 / %s) %s%n",
                    i + 1, p.displayNameWithGender(), p.getAge(), p.getMbti(), p.getTagline());
        }
        System.out.println("--------------------------------------------------");
    }

    private int parseIndexChoice(String rawInput, int size) {
        try {
            int idx = Integer.parseInt(rawInput);
            if (idx >= 1 && idx <= size) {
                return idx - 1;
            }
        } catch (NumberFormatException ignored) {
            // 숫자가 아니면 무효 처리
        }
        return -1;
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
        if (!chatSessionService.isLlmEnabled()) {
            System.out.println("[시스템] (Gemini API 키가 설정되지 않아 기본 대사로 진행합니다. resources/gemini.properties를 확인하세요.)");
        }

        // 세션 첫 인사는 아직 유저 발화가 없으므로 JSON 폴백 대사로 시작한다.
        printPersonaLine(persona, session.getState().displayName(),
                chatSessionService.fallbackLine(session, persona, gameData.getConfig()));

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
            printReplyHint(persona);
            System.out.print("[유저 입력 (" + turn + "/" + toTurn + "턴)] > ");
            String userInput = safeNextLine();

            AnalysisResult result = chatSessionService.processTurn(session, persona, userInput);
            printEngineTrace(result, session);

            sleepQuietly(THINKING_DELAY_MS);
            String replyLine = chatSessionService.generateReply(session, persona, gameData.getConfig(), userInput);
            printPersonaLine(persona, session.getState().displayName(), replyLine);

            session.recordTurn(userInput, replyLine);
        }
        return turn;
    }

    /** 선택지 + 자율 입력 하이브리드: 참고용 화제 힌트를 살짝 보여주되, 완전히 자유롭게 입력해도 된다. */
    private void printReplyHint(Persona persona) {
        if (!persona.getLoveKeywords().isEmpty()) {
            List<String> keys = new ArrayList<>(persona.getLoveKeywords().keySet());
            int randomIndex = ThreadLocalRandom.current().nextInt(keys.size());
            String hintWord = keys.get(randomIndex);

            System.out.println("(귀띔: '" + hintWord + "' 같은 이야기를 좋아하는 것 같다...)");
        }
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
        sleepQuietly(THINKING_DELAY_MS);
        String farewell = chatSessionService.generateReply(session, persona, gameData.getConfig(),
                "(오늘 대화는 여기까지 하자면서 아쉬운 듯 짧게 인사해줘)");
        printPersonaLine(persona, session.getState().displayName(), farewell);

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

        sleepQuietly(THINKING_DELAY_MS);
        String confirmationLine = chatSessionService.generateReply(session, persona, gameData.getConfig(),
                "(방금 정식으로 매칭이 확정됐다는 소식을 듣고 기뻐하며 앞으로 잘 지내보자는 인사를 건네줘)");
        printPersonaLine(persona, session.getState().displayName(), confirmationLine);

        System.out.println("[시스템] " + persona.getName() + "님과의 새로운 인연이 시작됩니다...\n");
    }

    // ------------------------------------------------------------
    // 공통 출력 유틸 (타이핑 효과 포함)
    // ------------------------------------------------------------

    private void printPersonaLine(Persona persona, String tierDisplayName, String line) {
        System.out.print("[" + persona.displayNameWithGender() + " - " + tierDisplayName + "]: ");
        printTyping(line);
        System.out.println();
    }

    /** 사람이 카톡을 치듯 한 글자씩 출력하는 타이핑 연출 효과. */
    private void printTyping(String text) {
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i));
            sleepQuietly(TYPING_DELAY_MS);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printBanner() {
        System.out.println("==================================================");
        System.out.println("   MelKie (멜키) - 페르소나 매칭 시뮬레이터");
        System.out.println("==================================================");
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
