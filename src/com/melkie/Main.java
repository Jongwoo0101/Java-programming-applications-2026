package com.melkie;

import com.melkie.data.PersonaRepository;
import com.melkie.model.GameData;
import com.melkie.ui.ConsoleUI;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * MelKie (멜키) - 순수 자바 코어 엔진 기반 페르소나 매칭 시뮬레이터
 * 엔트리 포인트.
 *
 * 실행 방법 (프로젝트 루트 기준):
 *   javac -encoding UTF-8 -d bin $(find src -name "*.java")
 *   java -Dfile.encoding=UTF-8 -cp bin com.melkie.Main resources/personas.json
 *
 * 인자를 주지 않으면 기본 경로 "resources/personas.json" 을 사용한다.
 */
public class Main {

    public static void main(String[] args) {
        // 콘솔 한글 깨짐 방지: 표준 입출력을 UTF-8로 강제 고정
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        String jsonPath = args.length > 0 ? args[0] : "resources/personas.json";

        GameData gameData;
        try {
            gameData = PersonaRepository.loadFromPath(jsonPath);
        } catch (IOException e) {
            System.err.println("[오류] personas.json 로드에 실패했습니다: " + jsonPath);
            System.err.println("       실행 위치(프로젝트 루트)를 확인하거나, 경로를 인자로 전달하세요.");
            System.err.println("       예) java -cp bin com.melkie.Main /path/to/personas.json");
            return;
        }

        if (gameData.getPersonas().isEmpty()) {
            System.err.println("[오류] 로드된 페르소나가 없습니다. personas.json 내용을 확인하세요.");
            return;
        }

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new ConsoleUI(gameData, scanner).run();
        }
    }
}
