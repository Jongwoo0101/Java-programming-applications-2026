# MelKie (멜키) — 순수 자바 코어 엔진 페르소나 매칭 시뮬레이터

`final_result.md` 기획서를 기준으로 구현한 **Zero-Dependency 순수 자바(Java SE)** 콘솔 프로젝트입니다.
외부 라이브러리(Gson/Jackson, LLM API 등)를 전혀 사용하지 않고, JSON 파서까지 직접 구현했습니다.

## 실행 방법

JDK 17 이상이면 됩니다 (테스트 환경: OpenJDK 21).

```bash
# 1) 컴파일
cd MelKie
javac -encoding UTF-8 -d bin $(find src -name "*.java")

# 2) 실행 (프로젝트 루트에서 실행해야 resources/personas.json을 자동으로 찾습니다)
java -cp bin com.melkie.Main

# 경로를 직접 지정하고 싶다면:
java -cp bin com.melkie.Main /path/to/personas.json
```

macOS/Windows 모두 별도 설정 없이 위 두 명령으로 동일하게 동작합니다.

## 조작법

- `L` : 다음 페르소나로 넘기기 (Next / 패스)
- `R` : 매칭 시도 → 1:1 채팅 시작 (Match)
- `Q` : 게임 종료 (탐색 화면에서)

매칭 후에는 3턴 동안 자유롭게 텍스트를 입력하면 됩니다. 입력한 문장에 어떤 키워드가
들어있는지에 따라 텐션(Melt Rate)이 오르내리고, 30점/70점을 돌파할 때마다
밀당(Chilly) → 능글(Flirty) → 치명(Steamy) 순으로 페르소나의 상태가 바뀝니다.
3턴이 끝나면 코사인 유사도로 계산한 싱크로율(%)이 출력되고, 14인 중 가장 잘 맞는
페르소나도 참고로 함께 보여줍니다.

## 프로젝트 구조

```
MelKie/
├── resources/
│   └── personas.json         # ★ 콘텐츠 데이터 (페르소나 14인 + 전역 어휘 사전 + 접두사 풀)
├── src/com/melkie/
│   ├── Main.java              # 엔트리 포인트
│   ├── model/
│   │   ├── Persona.java       # 페르소나 도메인 객체
│   │   ├── GameConfig.java    # 전역 어휘 사전 / 접두사 풀
│   │   └── GameData.java      # 페르소나 목록 + 설정을 담는 컨테이너
│   ├── state/                 # 상태 패턴 (State Pattern)
│   │   ├── MeltingState.java  # 인터페이스
│   │   ├── ChillyState.java   # 밀당
│   │   ├── FlirtyState.java   # 능글
│   │   └── SteamyState.java   # 치명
│   ├── engine/
│   │   ├── TextAnalysisEngine.java  # 정규식(Pattern/Matcher) + Stream 기반 키워드 분석
│   │   ├── AnalysisResult.java      # 분석 결과 값 객체
│   │   ├── MeltingContext.java      # 세션 컨텍스트 (텐션/성향 벡터 누적, 상태 전이 평가)
│   │   └── MatchEngine.java         # 코사인 유사도 계산 (Math.pow/Math.sqrt만 사용)
│   ├── data/
│   │   ├── SimpleJsonParser.java    # 자체 구현 JSON 파서 (재귀 하강 파서, 외부 라이브러리 無)
│   │   └── PersonaRepository.java   # JSON → 도메인 객체 매핑
│   └── ui/
│       └── ConsoleUI.java           # Scanner + println 기반 CUI, 틴더 탐색 + 채팅 루프
└── README.md
```

## personas.json 구조 & 커스터마이징 가이드

행동 특성을 코드 수정 없이 JSON만 고쳐서 쉽게 바꿀 수 있도록 설계했습니다.

```jsonc
{
  "globalPositiveKeywords": { "예쁘": 8, "좋아": 10, ... },  // 모든 페르소나 공통 가점 어휘
  "globalNegativeKeywords": { "싫": -12, ... },              // 모든 페르소나 공통 감점 어휘
  "dominanceWords":  { "내가": 6, "따라와": 10, ... },        // 유저 성향 벡터: 주도성
  "submissionWords": { "알겠어": 8, "맡길게": 10, ... },      // 유저 성향 벡터: 순종성
  "assertiveWords":  { "좋아해": 10, "사랑해": 12, ... },     // 유저 성향 벡터: 적극성
  "prefixes": { "CHILLY": [...], "FLIRTY": [...], "STEAMY": [...], "WARM": [...], "COLD": [...] },
  "personas": [
    {
      "id": 1, "name": "이동현", "age": 20, "mbti": "ENFP", "gender": "MALE",
      "tagline": "장난스러운 연하남",
      "targetVector": [40, 25, 80],       // [주도성, 순종성, 적극성] — 매칭 계산의 기준 벡터
      "loveKeywords": { "남자답": 15, ... },  // 이 캐릭터만 유독 설레는 키워드 (전역 사전보다 우선 적용)
      "hateKeywords": { "귀엽": -8, ... },    // 이 캐릭터만 유독 식는 키워드 (전역 사전보다 우선 적용)
      "dialogues": {
        "CHILLY": ["...", "..."],
        "FLIRTY": ["...", "..."],
        "STEAMY": ["...", "..."]
      }
    }
  ]
}
```

### 우선순위 규칙 (TextAnalysisEngine)
1. 해당 페르소나의 `loveKeywords` / `hateKeywords`에 걸리는 단어는 그 값으로 **확정**됩니다.
2. 이미 1번에서 처리된 단어(어간)는 전역 사전에서 다시 채점하지 않습니다(중복 방지).
3. `dominanceWords` / `submissionWords` / `assertiveWords`는 페르소나와 무관하게 항상 별도로
   누적되어, 최종 3차원 벡터 `[주도성, 순종성, 적극성]`을 구성합니다.

### 새 페르소나 추가/수정하는 법
- `personas` 배열에 객체를 추가하거나 기존 항목을 수정하면 끝입니다. 코드 재컴파일 불필요
  (실행 시 매번 JSON을 새로 읽습니다).
- `targetVector`는 그 캐릭터의 "타고난 성향"을 뜻하며, 유저가 대화 중 쌓은 벡터와의
  코사인 유사도로 싱크로율이 계산됩니다.
- 대사(`dialogues`)는 상태별로 여러 줄을 넣어두면 매 턴 랜덤하게 하나씩 뽑혀 사용됩니다.

