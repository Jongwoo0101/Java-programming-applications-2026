package com.melkie;

import com.melkie.config.GroqConfigLoader;
import com.melkie.data.PersonaRepository;
import com.melkie.engine.TextAnalysisEngine;
import com.melkie.llm.GroqClient;
import com.melkie.llm.GroqDialogueService;
import com.melkie.model.GameData;
import com.melkie.service.ChatSessionService;
import com.melkie.ui.ConsoleUI;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_DATA_PATH = "resources/personas.json";

    public static void main(String[] args) {
        configureUtf8Console();

        String jsonPath = args.length > 0 ? args[0] : DEFAULT_DATA_PATH;

        GameData gameData = loadGameDataOrExit(jsonPath);
        if (gameData == null) {
            return;
        }

        TextAnalysisEngine textAnalysisEngine = new TextAnalysisEngine(gameData.getConfig());
        GroqDialogueService dialogueService = buildDialogueService();
        ChatSessionService chatSessionService =
                new ChatSessionService(textAnalysisEngine, dialogueService, gameData.getPersonas());

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new ConsoleUI(gameData, scanner, chatSessionService).run();
        }
    }

    private static GroqDialogueService buildDialogueService() {
        String apiKey = GroqConfigLoader.loadApiKey();
        String model = GroqConfigLoader.loadModel();

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[시스템] GROQ_API_KEY가 설정되지 않았습니다. JSON 폴백 대사로만 실행합니다.");
            System.out.println("         (환경변수 GROQ_API_KEY 또는 resources/groq.properties 를 확인하세요)");
            return new GroqDialogueService(null, false);
        }

        System.out.println("[시스템] Groq API 연동 활성화 (model=" + model + ")");
        GroqClient client = new GroqClient(apiKey, model);
        return new GroqDialogueService(client, true);
    }

    private static void configureUtf8Console() {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    private static GameData loadGameDataOrExit(String jsonPath) {
        GameData gameData;
        try {
            gameData = PersonaRepository.loadFromPath(jsonPath);
        } catch (IOException e) {
            System.err.println("[오류] personas.json 로드에 실패했습니다: " + jsonPath);
            return null;
        }
        if (gameData.getPersonas().isEmpty()) {
            System.err.println("[오류] 로드된 페르소나가 없습니다. personas.json 내용을 확인하세요.");
            return null;
        }
        return gameData;
    }
}