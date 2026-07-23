# MelKie (멜키) — Groq LLM 연동 순수 자바 코어 페르소나 매칭 시뮬레이터

순수 자바(Java SE) 표준 API만을 사용하여 구축된 **Zero-Dependency 콘솔 연애 시뮬레이션 프로젝트**입니다.
외부 라이브러리(Gson, Jackson, 공식 LLM SDK 등)를 일절 사용하지 않고 독자적인 JSON 파서와 HTTP 통신 클라이언트를 직접 구현했습니다. 

최신 업데이트를 통해 정해진 대사를 출력하던 방식에서 벗어나, **Groq API(`llama-3.3-70b-versatile`)를 연동하여 유저의 이전 대화 문맥을 기억하는 실시간 멀티턴(Multi-turn) 자연어 대화**를 지원합니다.

---

## 실행 및 설정 방법

JDK 17 이상 환경을 권장합니다 (테스트 환경: OpenJDK 21).

### 1. Groq API 키 설정
자연스러운 AI 대화를 위해 Groq API 키가 필요합니다. 다음 두 가지 방법 중 하나로 설정하세요.
*   **방법 A (환경 변수 - 권장):** OS 환경 변수에 `GROQ_API_KEY`를 등록합니다.
*   **방법 B (프로퍼티 파일):** 프로젝트 루트의 `resources` 폴더 안에 `groq.properties` 파일을 생성하고 아래와 같이 입력합니다.
    ```properties
    groq.api.key=gsk_발급받은키문자열
    groq.model=llama-3.3-70b-versatile
    ```
    *(키가 설정되지 않거나 네트워크 오류 시, 게임이 멈추지 않고 JSON의 기본 대사를 출력하는 안전망(Fallback)이 작동합니다.)*

### 2. 컴파일 및 실행
```bash
# 1) 전체 소스코드 컴파일 (UTF-8 인코딩)
cd MelKie
javac -encoding UTF-8 -d bin $(find src -name "*.java")

# 2) 프로그램 실행 (프로젝트 루트에서 실행해야 resources/personas.json을 자동으로 찾습니다)
java -cp bin com.melkie.Main

# JSON 데이터 경로를 직접 지정하고 싶다면:
java -cp bin com.melkie.Main /path/to/personas.json

```

---

## 게임 진행 흐름 및 조작법

기존 틴더 스타일(1개씩 넘기기)에서 **번호 리스트 선택 방식**으로 직관적으로 개편되었습니다.

1. **성별 필터링:** 매칭을 원하는 성별(M: 남성 / F: 여성 / A: 무관)을 선택합니다.
2. **페르소나 선택:** 필터링된 19인의 페르소나 목록이 출력되면, 대화하고 싶은 상대의 번호를 입력합니다. (0 입력 시 종료)
3. **1:1 채팅 진행 (기본 7턴):**
* 매 턴마다 해당 캐릭터가 선호하는 화제(키워드)가 랜덤으로 귀띔(힌트)됩니다.
* 자유롭게 텍스트를 입력하면, 내부 규칙 엔진이 텐션(호감도)과 성향을 채점하고, Groq LLM이 문맥에 맞는 자연스러운 답장을 실시간으로 생성합니다.
* 텐션 점수가 30점, 70점을 돌파할 때마다 `밀당(Chilly) → 능글(Flirty) → 치명(Steamy)` 단계로 상태가 전이되며 캐릭터의 태도가 바뀝니다.


4. **턴 종료 후 분기 (3가지):**
* `[1] 결과 보기:` 성향 분석 및 코사인 유사도(%) 매칭 리포트를 출력하고 이전 탐색 목록으로 돌아갑니다.
* `[2] 더 답변하기:` 대화를 3턴 추가 연장합니다.
* `[3] 매칭하기:` 최종 매칭을 확정 짓고 게임을 성공적으로 종료합니다.



---

## 프로젝트 구조 (계층형 Clean Architecture)

각 계층은 자신보다 안쪽(도메인에 가까운) 계층만 의존하고, 바깥쪽(콘솔 I/O 등)을 알지 못하도록 엄격히 분리되었습니다. 이를 통해 추후 UI를 웹이나 GUI로 변경하더라도 코어 로직은 100% 재사용 가능합니다.

```text
MelKie/
├── resources/
│   ├── personas.json          # 19인 캐릭터 스펙 + 전역 어휘 사전 (욕설 필터 포함)
│   └── groq.properties        # API 키 설정 파일 (git 제외 권장)
├── src/com/melkie/
│   ├── Main.java              # 조립 루트(Composition Root) — 객체 생성 및 의존성 주입
│   │
│   ├── model/                 # 도메인 엔티티 (순수 데이터)
│   │   ├── Persona.java       # 페르소나 속성 (JSON 매핑)
│   │   └── Gender.java        # 성별 Enum (타입 안정성 보장)
│   │
│   ├── state/                 # 상태 패턴 (State Pattern) — 감정 전이 통제
│   │   ├── MeltingState.java  # 인터페이스 (Chilly, Flirty, Steamy 구현체)
│   │
│   ├── engine/                # 코어 연산 엔진 (채점 및 수학 계산)
│   │   ├── TextAnalysisEngine.java # 정규식(Regex) 기반 키워드 점수 채점관
│   │   ├── MeltingContext.java     # 현재 세션의 텐션, 유저 벡터, 대화 기록(history) 보관
│   │   ├── ChatTurn.java           # LLM에게 전달할 한 턴의 대화 캡슐
│   │   └── MatchEngine.java        # 코사인 유사도 연산기
│   │
│   ├── llm/                   # AI 통신 계층
│   │   ├── GroqClient.java         # JDK 표준 HttpClient 기반 Groq REST API 호출
│   │   └── GroqDialogueService.java# 프롬프트 조립 및 OpenAI 규격 JSON 파싱
│   │
│   ├── service/               # 유스케이스 계층 (비즈니스 로직 총괄)
│   │   ├── ChatSessionService.java # 분석, LLM 호출, 페널티 기반 최종 리포트 산출
│   │   └── PersonaBrowseService.java# 필터링 및 리스트 탐색 관리
│   │
│   ├── data/                  # 인프라 계층 (JSON 입출력)
│   │   ├── SimpleJsonParser.java   # 순수 자바 재귀 하강 JSON 파서
│   │   └── PersonaRepository.java  # 데이터 매핑 및 차원 충돌 방지(Vector 3D 강제 고정)
│   │
│   └── ui/                    # 프레젠테이션 계층 (화면 출력 및 입력)
│       └── ConsoleUI.java          # Scanner 입력, 랜덤 힌트 제공, 타이핑 애니메이션 출력
└── README.md

```

---

## 핵심 시스템 및 로직 상세

### 1. 역할 분담: 규칙 엔진(판정) vs LLM(연출)

본 프로젝트는 LLM의 할루시네이션(환각)이나 우연성에 게임의 핵심 수치가 흔들리는 것을 막기 위해 철저한 **책임 분리**를 적용했습니다.

* **엔진 (판정):** 유저의 발화는 무조건 `TextAnalysisEngine`의 정규식 어휘 사전을 거칩니다. 호감도의 상승/하락, 상태 전이(밀당→능글)는 100% 코드가 통제합니다.
* **LLM (연출):** `GroqDialogueService`는 엔진이 넘겨준 "현재 호감도", "감정 상태", "이전 대화 기록(ChatTurn)"을 프롬프트로 받아, **수치에 걸맞은 대사만을 자연스럽게 창작**하여 반환합니다.

### 2. 코사인 유사도와 '트롤링 페널티' 로직

유저가 쌓은 3차원 성향 벡터 `[주도성, 순종성, 적극성]`과 캐릭터의 이상형(Target Vector)을 수학적 코사인 유사도로 비교하여 싱크로율을 산출합니다.

* **어뷰징 방지:** 만약 유저가 대화 중 욕설을 하거나 고의로 텐션을 바닥(30점 미만)으로 떨어뜨릴 경우, 벡터가 일치하더라도 `ChatSessionService`에서 강제로 **최종 매칭률에 강력한 페널티 비율을 곱해버립니다.**
* 결과적으로 대화를 망치면 본인뿐 아니라 추천되는 다른 페르소나와의 매칭률도 10~20%대로 동반 폭락하는 현실적인 로직이 적용되어 있습니다.

### 3. 멀티턴 (Multi-turn) Context 유지

Groq API는 상태를 저장하지 않는(Stateless) REST 통신입니다. 이를 해결하기 위해 `MeltingContext` 내부에 `List<ChatTurn> history`를 두어, 매번 통신할 때마다 **과거의 대화 내역 전체를 System Prompt와 함께 누적하여 전송**합니다. 덕분에 캐릭터가 유저의 방금 전 대답을 기억하고 자연스럽게 맞받아치는 진짜 사람 같은 롤플레잉이 가능합니다.

---

## personas.json 커스터마이징 가이드

모든 캐릭터의 스펙과 채점 기준은 자바 코드가 아닌 JSON에 분리되어 있어 기획/데이터 수정이 매우 자유롭습니다.

```jsonc
{
  "globalPositiveKeywords": { "예쁘": 8, "좋아": 10, ... }, // 모든 페르소나 공통 가점 (사랑, 칭찬)
  "globalNegativeKeywords": { "싫": -12, "지랄": -30, "씨발": -30, ... }, // 공통 감점 및 강력한 욕설 필터
  "dominanceWords":  { "내가": 6, "리드": 10, ... },       // 유저 성향: 주도성(Dominance) 증가
  "submissionWords": { "알겠어": 8, "부탁해": 6, ... },    // 유저 성향: 순종성(Submission) 증가
  "assertiveWords":  { "좋아해": 10, "안아줘": 10, ... },   // 유저 성향: 적극성(Assertiveness) 증가
  
  "personas": [
    {
      "id": 1, "name": "이동현", "age": 20, "mbti": "ENFP", "gender": "MALE",
      "targetVector": [40, 25, 80],       // [주도, 순종, 적극] — 이 캐릭터의 이상형 성향
      "loveKeywords": { "듬직": 15, ... },  // 이 캐릭터 전용 가점 (전역 사전보다 우선순위 높음)
      "hateKeywords": { "애기": -15, ... }, // 이 캐릭터 전용 감점
      "dialogues": {                      // 네트워크 단절 시 사용되는 비상용(Fallback) 대사집
        "CHILLY": ["...", "..."],
        "FLIRTY": ["...", "..."],
        "STEAMY": ["...", "..."]
      }
    }
    // ... 총 19인의 페르소나
  ]
}

```

* **우선순위:** 페르소나 개인의 `love/hateKeywords`가 전역(`global`) 사전보다 먼저 검사됩니다. 동일한 단어가 처리되면 중복으로 점수가 변동되지 않습니다.
* **오류 방어:** JSON 작성 중 실수로 `targetVector`를 2개나 4개로 적더라도, `PersonaRepository`에서 3차원(`new double[3]`)으로 강제 고정하므로 프로그램이 튕기지 않습니다.
