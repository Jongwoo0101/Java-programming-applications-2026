# MelKie(멜키) 전체 코드 분석 문서

> 발표/코드 질의응답 대비용 문서입니다. 패키지 → 클래스 → 필드/메서드 순서로 "이게 왜 필요한가"까지 설명합니다.

---

## 0. 전체 아키텍처 한눈에 보기

```
com.melkie
 ├─ config   : Groq API 키/모델명을 어디서 읽어올지 담당 (환경변수 → properties 파일)
 ├─ data     : personas.json을 읽어서 자바 객체로 바꿔주는 계층 (JSON 파서 + 매핑)
 ├─ model    : 순수 데이터 클래스 (Persona, GameData, GameConfig, Gender)
 ├─ engine   : 규칙 기반 텍스트 분석 + 코사인 유사도 계산 + 채팅 세션 상태 저장
 ├─ state    : 상태 패턴(밀당→능글→치명) 구현
 ├─ llm      : Groq(OpenAI 호환) API를 직접 호출하는 HTTP 클라이언트 + 프롬프트 조립
 ├─ service  : 위 모든 계층을 조합해 "유스케이스"(한 턴 처리, 리포트 생성 등)를 제공
 ├─ ui       : Scanner/System.out 만 사용하는 콘솔 화면 계층
 └─ Main     : 모든 객체를 생성하고 연결하는 조립 루트(Composition Root)
```

**핵심 설계 원칙 (발표 때 강조 포인트)**
1. **제로 외부 의존성** — Jackson/Gson도, OkHttp도 안 씀. JSON 파싱도, HTTP 통신도 자바 표준 라이브러리(`java.net.http.HttpClient`)로 전부 직접 구현.
2. **계층 분리(관심사 분리)** — `ui`는 화면만, `service`는 로직만, `engine`은 계산만, `llm`은 외부 통신만 담당. 그래서 콘솔이 아니라 웹/GUI로 바꿔도 `ui` 패키지만 새로 짜면 됨.
3. **상태 패턴(State Pattern)** — 호감도 단계 전이를 `if-else` 없이 다형성으로 처리.
4. **폴백(Fallback) 설계** — LLM(Groq) 호출이 실패해도 JSON에 저장된 대사로 자동 전환되어 게임이 절대 멈추지 않음.
5. **텐션 계산과 대사 생성의 역할 분리** — 호감도 점수 계산은 항상 결정론적인 자바 로직(`TextAnalysisEngine`)이 맡고, Groq는 "그 상태에 맞는 자연스러운 문장"만 생성. 즉 LLM이 이상한 소리를 해도 게임 점수 자체는 흔들리지 않음.

---

## 1. `config` 패키지 — API 키 로딩

### `GroqConfigLoader`
Groq API 키/모델명을 어디서 가져올지 우선순위를 정해주는 클래스. 인스턴스를 만들 필요가 없어서 `private` 생성자로 막아둔 **정적 유틸리티 클래스(static utility class)**.

- **상수**
  - `ENV_API_KEY = "GROQ_API_KEY"`, `ENV_MODEL = "GROQ_MODEL"` : 환경변수 이름.
  - `DEFAULT_PROPERTIES_PATH = "resources/groq.properties"` : 환경변수가 없을 때 대신 읽을 설정 파일 경로.
  - `DEFAULT_MODEL = "llama-3.3-70b-versatile"` : 모델명도 없으면 쓰는 기본값.

- **`loadApiKey()`**
  1) `System.getenv("GROQ_API_KEY")`로 환경변수를 먼저 확인.
  2) 있으면 그 값을 반환(공백 제거), 없으면 `readProperty("groq.api.key")`로 파일에서 읽음.
  → **환경변수를 우선하는 이유**: 발표/시연 시 노트북마다 파일을 안 만들어도 되고, API 키를 소스/파일에 남기지 않아도 되기 때문(보안).

- **`loadModel()`**
  구조는 `loadApiKey()`와 동일하되, 파일에도 없으면 `DEFAULT_MODEL`을 반환한다는 점이 다름(모델명은 없어도 기본값으로 동작 가능해야 하므로).

- **`readProperty(String key)`**
  `Path.of(DEFAULT_PROPERTIES_PATH)`로 파일 존재를 확인 → 없으면 `null` 반환(예외 던지지 않음, 즉 "설정 안 함"은 정상 케이스로 취급) → `Properties` 객체로 파일을 읽어 `key`에 해당하는 값을 반환.

**예상 질문 대비**: "왜 Properties 클래스를 썼나?" → 외부 라이브러리(dotenv 등) 없이 자바 표준(`java.util.Properties`)만으로 key=value 설정 파일을 읽을 수 있어서, 제로 의존성 원칙을 지키기 위함.

---

## 2. `data` 패키지 — JSON 파싱 & 매핑

### 2-1. `SimpleJsonParser`
외부 JSON 라이브러리 없이 **직접 만든 재귀 하강(Recursive Descent) 파서**. JSON 문법 자체를 문자 단위로 읽어 내려가며 파싱한다.

- **필드**
  - `src` : 파싱할 원본 JSON 문자열.
  - `pos` : 현재 읽고 있는 문자 위치(커서). 파싱이 진행될수록 계속 증가.

- **`parse(String json)` (정적 메서드)**
  새 파서 인스턴스를 만들고 공백을 건너뛴 뒤 `parseValue()`를 호출 → JSON 최상위 값(객체/배열/문자열 등 무엇이든) 하나를 반환.

- **`parseObjectRoot(String json)`**
  `parse()`의 결과가 `Map`(즉 `{...}` 객체)인지 확인하고 캐스팅해서 반환. personas.json은 최상위가 항상 객체여야 하므로, 아니면 예외.

- **`parseValue()`**
  현재 위치의 문자(`{`, `[`, `"`, `t/f`, `n`, 숫자)를 보고 어떤 파싱 메서드로 분기할지 결정하는 **디스패처(dispatcher)**. JSON 문법 자체가 "다음 글자를 보면 타입을 알 수 있다"는 특징을 이용.

- **`parseObject()`**
  `{` 를 소비 → 비어있으면 즉시 반환 → `while(true)` 루프를 돌며 `"key" : value` 쌍을 계속 읽어 `LinkedHashMap`에 저장 → `,`면 계속, `}`면 종료. `LinkedHashMap`을 쓴 이유는 JSON에 적힌 **순서를 그대로 보존**하기 위함(순서가 중요한 배열형 대사 데이터 등에 유리).

- **`parseArray()`**
  구조는 `parseObject()`와 거의 동일하지만 key 없이 값만 순서대로 `ArrayList`에 저장.

- **`parseString()`**
  여는 `"` 를 소비하고, 닫는 `"`가 나올 때까지 문자를 읽음. 이스케이프 문자(`\"`, `\\`, `\n`, `\t`, `\uXXXX` 등)를 만나면 실제 문자로 변환해서 `StringBuilder`에 쌓음. 한글 대사 데이터 안에 따옴표나 줄바꿈이 들어갈 수 있으므로 이 처리가 필수.

- **`parseNumber()`**
  `-`, 숫자, `.`(소수점), `e/E`(지수 표기)까지 전부 문자 단위로 읽어서 부분 문자열을 잘라낸 뒤 `Double.parseDouble()`로 변환. JSON 표준은 정수/실수를 구분하지 않으므로 **항상 `Double`로 통일**해서 반환(그래서 나중에 매핑 코드에서 `((Double) map.get(...)).intValue()`처럼 캐스팅 후 변환하는 패턴이 반복됨).

- **`parseBoolean()` / `parseNull()`**
  `src.startsWith("true", pos)` 처럼 남은 문자열이 리터럴과 일치하는지 검사 후 커서를 이동. 못 찾으면 예외.

- **`expect(char)` / `peek()` / `skipWhitespace()`**
  각각 "이 위치에 특정 문자가 있어야 한다"는 검증, "다음 유효 문자 미리보기(공백 자동 스킵)", "공백/개행 건너뛰기"를 담당하는 파서 내부 보조 메서드.

- **`error(String msg)`**
  파싱 실패 시 현재 위치 앞뒤 20자 컨텍스트를 함께 보여주는 `IllegalArgumentException`을 만들어 반환 → JSON 오류 디버깅을 쉽게 하기 위한 장치.

**예상 질문 대비**: "왜 Gson/Jackson을 안 쓰고 직접 짰나?" → 프로젝트 요구사항(순수 자바, 제로 의존성)을 지키기 위해서이며, 재귀 하강 파서는 컴파일러/인터프리터에서 널리 쓰이는 정석적인 파싱 기법임을 강조하면 좋음.

### 2-2. `PersonaRepository`
`SimpleJsonParser`가 만들어준 **범용 Map/List 트리**를, 실제 게임에서 쓰는 **타입 안전한 도메인 객체**(`Persona`, `GameConfig`, `GameData`)로 변환(매핑)하는 계층. "역직렬화(Deserialization)" 담당.

- **`loadFromPath(String path)`**
  파일 시스템 경로에서 UTF-8로 JSON 텍스트를 읽어 `parse()` 호출. (실행 시 인자로 다른 JSON 경로를 지정할 수 있게 하기 위함.)

- **`loadFromClasspath(String resourceName)`**
  jar 안에 패키징된 리소스(classpath)에서 읽는 버전. `try-with-resources`로 스트림을 안전하게 닫음.

- **`parse(String json)`**
  1) `SimpleJsonParser.parseObjectRoot()`로 최상위 Map을 얻음.
  2) `globalPositiveKeywords`, `globalNegativeKeywords`, `dominanceWords`, `submissionWords`, `assertiveWords`를 각각 `toIntMap()`으로 변환(전역 어휘 사전들).
  3) `prefixes`(상태별 대사 접두사 후보 목록)를 `Map<String, List<String>>` 형태로 변환.
  4) 이 값들로 `GameConfig`를 하나 생성.
  5) `personas` 배열을 순회하며 각 원소를 `toPersona()`로 변환해 리스트에 담음.
  6) 최종적으로 `GameData(personas, config)`를 반환.

- **`toPersona(Map<String, Object> map)`**
  JSON의 한 페르소나 객체를 `Persona`로 변환하는 핵심 매핑 로직.
  - `id`, `age`는 JSON에서 `Double`로 오므로 `.intValue()`로 변환.
  - `gender`는 문자열("MALE"/"FEMALE")을 `Gender.fromJsonValue()`로 enum 변환.
  - `targetVector`는 **항상 3차원으로 강제 고정**한다 — JSON에 3개보다 적게 들어와도 배열 길이 오류(`ArrayIndexOutOfBoundsException`)가 안 나도록 방어적으로 처리(`Math.min(3, size)`).
  - `loveKeywords`/`hateKeywords`는 `toIntMap()`으로, `dialogues`는 상태별(`CHILLY`/`FLIRTY`/`STEAMY`) 대사 리스트 맵으로 변환.

- **`toIntMap(Map<String, Object>)` / `toStringList(List<Object>)`**
  JSON의 범용 타입(`Object`)을 실제 필요한 타입(`Integer`, `String`)으로 안전하게 캐스팅해주는 재사용 헬퍼.

**예상 질문 대비**: "targetVector가 JSON에 없거나 이상하면?" → `targetVectorRaw`가 `null`이면 `double[3]`의 기본값(전부 0.0)을 그대로 사용하고, 있어도 3개까지만 채워 넣으므로 절대 예외가 나지 않는 방어적 코드임을 설명.

---

## 3. `model` 패키지 — 순수 데이터 클래스

### 3-1. `Gender` (enum)
`MALE`, `FEMALE` 두 값만 가지는 열거형. 원래는 `String`("MALE"/"FEMALE")으로 비교하다가, 오타나 대소문자 실수가 생길 수 있어(Primitive Obsession 문제) 타입으로 승격했다.
- `displayLabel` 필드 : 화면에 보여줄 한 글자("남"/"여").
- `displayLabel()` : getter.
- `fromJsonValue(String raw)` : JSON의 문자열을 enum으로 변환. `null`이거나 "MALE"/"FEMALE"이 아니면 예외를 던져 **잘못된 데이터가 조용히 통과되는 것을 막음(Fail Fast)**.

### 3-2. `Persona`
14명의 캐릭터 한 명 한 명을 표현하는 도메인 객체.
- **필드**: `id`, `name`, `age`, `mbti`, `gender`, `tagline`(한줄소개), `targetVector`(주도성/순종성/적극성 3차원 목표 벡터), `loveKeywords`(설레게 하는 단어→가중치), `hateKeywords`(정 떨어지는 단어→가중치), `dialogues`(상태별 대사 목록).
- **getter들**: 단순 필드 반환.
- **`randomLine(String tierKey)`**
  현재 관계 단계(`tierKey`, 예: "FLIRTY")에 맞는 대사 리스트에서 `ThreadLocalRandom`으로 하나를 무작위로 뽑아 반환. 대사가 없으면 `"..."`을 반환해 **UI가 절대 null을 받지 않도록** 보장.
- **`displayNameWithGender()`**
  콘솔에 "이름(성별)" 형태로 보여주기 위한 문자열 조합(예: "신유나(여)").
- **`toString()`**
  디버깅/로그용 문자열 표현.

### 3-3. `GameConfig`
전역(모든 페르소나 공통) 어휘 사전과, 대사 앞에 붙는 접두사 풀을 담는다.
- **상수** `WARM_DELTA_THRESHOLD = 15`, `COLD_DELTA_THRESHOLD = 0` : 직전 턴의 텐션 변화량(`lastDelta`)이 이 값 이상/이하면 "설렘(WARM)"/"식음(COLD)" 접두사 풀을 쓰겠다는 기준값. 원래는 코드 안에 `15`, `0`이라는 **매직 넘버**로 박혀 있던 것을 의미 있는 이름의 상수로 뽑아낸 리팩터링 결과.
- **필드**: 전역 긍/부정 키워드 맵, 주도성/순종성/적극성 단어 맵, `prefixes`(상태 키 → 접두사 후보 리스트).
- **`randomPrefix(String tierKey, int lastDelta)`**
  1) `lastDelta`가 임계값 이상이면 `"WARM"`, 이하면 `"COLD"`, 그 사이면 현재 상태(`tierKey`) 자체를 키로 사용.
  2) 해당 키에 대응하는 접두사 리스트를 찾고, 없으면 `tierKey`의 리스트로 대체, 그것도 없으면 빈 문자열.
  3) 있으면 그 중 하나를 무작위로 골라 반환.
  → 즉, **직전 반응이 좋았는지 나빴는지에 따라 대사 앞의 짧은 감탄사/반응이 달라지는 연출**을 담당.

### 3-4. `GameData`
`personas`(리스트)와 `config`를 함께 담는 단순 컨테이너(DTO). `PersonaRepository.parse()`의 최종 반환 타입.

---

## 4. `engine` 패키지 — 분석/계산/세션 상태

### 4-1. `AnalysisResult`
한 번의 텍스트 분석 결과를 담는 **불변(immutable) 값 객체**.
- `tensionDelta`(이번 발화로 텐션이 얼마나 변했는지), `dominanceDelta`/`submissionDelta`/`assertivenessDelta`(3차원 성향 벡터 증분), `matchedKeywords`(어떤 단어가 매칭됐는지 — 디버그/연출용).
- 생성자에서 `matchedKeywords`를 `Collections.unmodifiableList()`로 감싸 **외부에서 리스트를 변경하지 못하도록** 방어.

### 4-2. `ChatTurn`
한 턴에 오간 (유저 발화, 페르소나 응답) 쌍을 기록하는 불변 객체. Groq에 멀티턴 대화 맥락을 통째로 넘기기 위해 `MeltingContext.history`에 쌓인다.

### 4-3. `MatchEngine`
순수 수학 계산만 담당하는 정적 유틸리티 클래스.
- **`cosineSimilarity(double[] a, double[] b)`**
  코사인 유사도 공식(내적 ÷ (벡터 크기의 곱))을 `for` 루프와 `Math.pow`/`Math.sqrt`만으로 직접 구현. 두 벡터가 완전히 같은 방향이면 1.0, 직각이면 0.0, 반대 방향이면 -1.0에 가까워짐.
- **`similarityPercent(double[] userVector, double[] targetVector)`**
  - 유저가 대화 내내 아무 성향 신호도 안 줬다면(`isZeroVector`가 true) 벡터가 `[0,0,0]`이 되어 코사인 유사도 계산이 불가능(분모가 0)해지므로, 이때는 `[10,10,10]`이라는 **중립 기본값**으로 보정.
  - 코사인 유사도 결과가 음수면 0%로 클램프하고, 그 외에는 100을 곱해 퍼센트로 스케일링.
- **`isZeroVector(double[] v)`**
  배열의 모든 값이 0인지 검사하는 private 헬퍼.

### 4-4. `MeltingContext`
**한 명의 페르소나와의 1:1 채팅 세션 상태를 통째로 들고 있는 클래스.** 상태 패턴의 "Context" 역할.
- **필드**
  - `persona` : 이 세션의 대화 상대.
  - `state` : 현재 관계 단계(초기값 `ChillyState.INSTANCE`).
  - `tension` : 0~100 사이의 누적 호감도 점수.
  - `userVector` : `double[3]`, 유저의 [주도성, 순종성, 적극성] 누적치.
  - `lastDelta` : 바로 직전 턴의 텐션 변화량(접두사 선택에 사용).
  - `history` : 지금까지의 대화 기록(`ChatTurn` 리스트).
- **`applyResult(AnalysisResult result)`**
  1) `lastDelta` 갱신.
  2) `tension`에 변화량을 더하되 `clamp()`로 0~100 범위를 벗어나지 않게 강제.
  3) `userVector`의 3개 축에 각각 증분을 누적.
  4) **`state = state.evaluateTransition(tension)`** — 다형성 호출 한 줄로 상태 전이 여부를 판단(if-else 없음이 핵심 설계 포인트).
- **`recordTurn(String userText, String personaText)`**
  이번 턴의 대화를 `history`에 추가.
- **`getHistory()`**
  `Collections.unmodifiableList()`로 감싸 반환 — 외부(LLM 서비스 등)가 세션의 내부 기록을 실수로 변경하지 못하게 방어.
- **`clamp(int, int, int)`**
  값이 min~max를 벗어나지 않게 잘라주는 private 정적 헬퍼.

### 4-5. `TextAnalysisEngine`
**규칙 기반(rule-based)** 텍스트 분석 엔진 — LLM도, 형태소 분석기도 안 쓰고 `java.util.regex`와 `HashMap` 사전만으로 유저 발화를 채점한다.
- **필드** `config`(전역 사전들을 담은 `GameConfig`).
- **`analyze(String input, Persona persona)`**
  우선순위 규칙대로 점수를 누적:
  1) 페르소나 전용 `loveKeywords`/`hateKeywords`를 먼저 매칭(가장 우선순위 높음 — "이 캐릭터만의" 반응).
  2) 그 다음 전역 공통 긍정/부정 사전을 매칭하되, **이미 1번에서 처리된 어간(stem)은 건너뜀**(중복 채점 방지, override 개념).
  3) 주도성/순종성/적극성 3개 사전은 위와 무관하게 항상 독립적으로 적용(Stream API로 합산).
  4) 결과를 `AnalysisResult`로 포장해 반환.
- **`scoreDictionary(...)`**
  사전을 순회하며 `resolvedStems`(이미 처리된 어간 집합)에 없는 것만 매칭 검사 → 매칭되면 점수 누적 + `resolvedStems`/`matchedOut`에 기록. **"우선순위가 높은 사전이 이미 다룬 단어는 낮은 사전이 다시 건드리지 않는다"**는 규칙을 구현하는 핵심 메서드.
- **`sumMatchedWeights(...)`**
  `dictionary.entrySet().stream().filter(...).mapToInt(...).sum()` — Stream API로 매칭된 항목들의 가중치 합을 구함(3차원 성향 벡터 계산에 사용, 여긴 override 규칙이 적용 안 됨).
- **`containsStem(String input, String stem)`**
  `Pattern.compile(Pattern.quote(stem))`로 정규식 특수문자를 이스케이프한 뒤 `input`에 포함되는지 검사. `Pattern.quote()`를 쓰는 이유: 어간에 `.`, `(` 같은 정규식 특수문자가 섞여 있어도 **문자 그대로** 매칭하기 위함.

**예상 질문 대비**: "형태소 분석 없이 단순 포함 검사만 하면 오탐(false positive)이 생기지 않나?" → 맞는 지적이며, 이 프로젝트는 정교한 NLP 대신 "빠르고 예측 가능한 규칙 기반 채점"을 목표로 했다고 설명하면 좋음(설계 트레이드오프를 인지하고 있다는 인상을 줌).

---

## 5. `state` 패키지 — 상태 패턴

### `MeltingState` (인터페이스)
- `tierKey()` : JSON `dialogues` 맵의 키("CHILLY"/"FLIRTY"/"STEAMY")와 매핑되는 값.
- `displayName()` : 화면에 보여줄 한글 이름("밀당"/"능글"/"치명").
- `evaluateTransition(int tension)` : 누적 텐션을 보고 다음 상태를 결정. **한 번 올라간 상태는 절대 내려가지 않는 단방향 전이(one-way escalation)**가 규칙.

### `ChillyState` (초기 상태, "밀당")
- `INSTANCE`라는 단 하나의 정적 인스턴스만 존재(Singleton 패턴 + 생성자 `private`). 상태 객체 자체는 아무 가변 데이터도 없으므로 굳이 매번 새로 만들 필요가 없기 때문.
- `evaluateTransition`: tension≥70이면 `SteamyState`로, ≥30이면 `FlirtyState`로, 그 외엔 자기 자신(`this`) 유지.

### `FlirtyState` ("능글")
- tension≥70이면 `SteamyState`로, 아니면 자기 자신 유지. (30 미만으로 절대 안 떨어짐 — 단방향 전이 원칙)

### `SteamyState` ("치명", 최종 상태)
- `evaluateTransition`은 항상 `this`만 반환 — 더 이상 전이가 없는 종착역.

**왜 다형성으로 짰는가(발표 강조 포인트)**: `if (state == "CHILLY" && tension >= 70) ...` 식의 분기문 없이, `state.evaluateTransition(tension)` 한 줄이면 각 상태 클래스가 "나 다음에 뭐가 될지"를 스스로 판단한다. 새로운 상태를 추가해도 기존 코드를 수정할 필요가 없다(개방-폐쇄 원칙, OCP).

---

## 6. `llm` 패키지 — Groq API 연동

### 6-1. `GroqClient`
Groq의 OpenAI 호환 REST API를 **`java.net.http.HttpClient`만으로 직접 호출**하는 클라이언트(외부 SDK 없음).
- **필드**: `apiKey`, `model`, `httpClient`(연결 타임아웃 10초로 미리 빌드해둠).
- **`ENDPOINT`**: `"https://api.groq.com/openai/v1/chat/completions"` — Groq가 제공하는 OpenAI 호환 엔드포인트.
- **`generateContent(String requestBodyJson)`**
  1) `HttpRequest`를 만들되 `Content-Type: application/json`과 `Authorization: Bearer {apiKey}` 헤더를 설정(Groq/OpenAI 인증 방식은 Bearer 토큰).
  2) `POST` 바디로 이미 만들어진 JSON 문자열을 그대로 보냄.
  3) 응답을 UTF-8 문자열로 받고, `statusCode()`가 200이 아니면 본문 내용을 포함한 `IOException`을 던짐(에러 원인 추적 용이).

### 6-2. `GroqDialogueService`
"시스템 프롬프트 조립 + Groq 호출 + 응답 파싱 + 실패 시 폴백"을 모두 담당하는 서비스.
- **상수** `TEMPERATURE = 0.95`(답변의 창의성/무작위성 정도, 높을수록 다양한 표현), `MAX_OUTPUT_TOKENS = 220`(응답 길이 제한 — 짧은 메신저 대화체를 유도).
- **필드** `client`, `enabled`(API 키가 없으면 `false`로 설정되어 아예 호출 자체를 시도하지 않음).
- **`generateReply(Persona persona, MeltingContext session, String userInput)`**
  `enabled`가 false면 즉시 `null` 반환(→ 상위 서비스가 폴백 대사를 쓰도록 신호). 활성화 상태면 `try`로 감싸 요청을 보내고, **어떤 예외가 나든(`Exception e`) 게임이 죽지 않도록 잡아서 `null`을 반환** + 콘솔에 실패 사유만 짧게 출력. 이게 "제로 다운타임" 폴백 설계의 핵심.
- **`buildRequestBody(...)`**
  Groq(OpenAI 호환) Chat Completions 규격의 JSON을 **문자열 조립(StringBuilder)**으로 직접 만든다.
  - `model`, `temperature`, `max_tokens` 지정.
  - `messages` 배열의 첫 원소로 `system` 역할에 `buildSystemPrompt()` 결과를 넣음.
  - 그 다음, 세션의 `history`(지금까지의 대화)를 순서대로 `user`/`assistant` 역할 쌍으로 추가 — **멀티턴 문맥 유지**의 핵심.
  - 마지막으로 이번 턴의 실제 유저 입력을 `user` 메시지로 추가.
- **`buildSystemPrompt(Persona persona, MeltingContext session)`**
  캐릭터의 이름/나이/MBTI/한줄소개, 현재 관계 단계와 텐션 점수, 좋아하는/싫어하는 화제(`loveKeywords`/`hateKeywords`의 키들)를 문장으로 조립하고, 답변 규칙(말투 유지, 관계 단계에 맞는 수위, 1~3문장, 괄호 행동 묘사 하나, 문맥에 맞게 답할 것)을 명시. **"텐션 계산은 자바가, 문장 생성은 LLM이"** 라는 역할 분담이 여기서 드러남.
- **`joinKeys(Map<String, Integer> map)`**
  키워드 맵의 키(단어)들만 콤마로 이어붙여 프롬프트에 넣기 좋은 문자열로 변환. 비어있으면 `"(특별히 없음)"`.
- **`extractText(String rawJson)`**
  Groq 응답(OpenAI 호환 포맷)에서 `choices[0].message.content` 경로를 따라 실제 답변 텍스트만 꺼냄. 각 단계(`choices` 없음, `message` 없음, `content`가 문자열이 아님)마다 명확한 예외 메시지를 던져 **어디서 실패했는지 바로 알 수 있게** 함.
- **`escape(String raw)`**
  JSON 문자열 안에 들어갈 수 없는 문자(`"`, `\`, 개행, 탭, 제어문자)를 이스케이프 시퀀스로 직접 변환. 외부 JSON 라이브러리를 안 쓰기 때문에 **요청 JSON을 만들 때도 이스케이프를 손수 처리**해야 함을 보여주는 부분.

**예상 질문 대비**: "왜 JSON을 문자열로 직접 조립하나? 위험하지 않나?" → 제로 의존성 원칙 때문이며, 그 대신 `escape()`로 특수문자를 철저히 처리해 안전성을 확보했다고 설명.

---

## 7. `service` 패키지 — 유스케이스 조합

### 7-1. `ChatBranchChoice` (enum)
7턴 대화 종료 후 사용자에게 주는 3가지 선택지를 표현. 원래 `"1"`, `"2"`, `"3"` 문자열을 코드 곳곳에서 직접 비교하던 것을 열거형으로 승격(매직 스트링 제거).
- `SHOW_RESULT`(결과 보기), `EXTEND_CHAT`(더 답변하기), `CONFIRM_MATCH`(매칭하기), `INVALID`(잘못된 입력).
- `fromInput(String rawInput)` : 문자열을 받아 위 enum 값으로 변환하는 정적 팩토리 메서드. `switch`문으로 "1"/"2"/"3"만 매칭하고 나머지는 `INVALID`.

### 7-2. `MatchReport`
세션 종료 후 산출되는 결과를 담는 불변 값 객체(DTO).
- `chattedPersona`(방금 대화한 상대), `syncPercentWithChattedPersona`(그 상대와의 싱크로율), `betterMatch`(`Optional<Persona>` — 전체 중 더 잘 맞는 상대가 있으면), `betterMatchPercent`.
- `Optional`을 쓴 이유: "더 잘 맞는 상대가 없을 수도 있다"는 경우를 `null`이 아니라 타입으로 명시적으로 표현하기 위함(Null-safety).

### 7-3. `PersonaBrowseService`
페르소나 탐색(성별 필터링) 유스케이스. `Scanner`/`System.out`에 전혀 의존하지 않는 순수 로직이라 UI를 바꿔도 재사용 가능.
- **`parseGenderChoice(String rawInput)`**
  `"M"`, `"남"` → `Gender.MALE`, `"F"`, `"여"` → `Gender.FEMALE`, 그 외(빈 값, 이상한 입력 등)는 `Optional.empty()`(= "전체 보기"로 해석). 대소문자 무시(`toUpperCase`).
- **`filterByGender(List<Persona>, Optional<Gender>)`**
  필터가 비어있으면 전체 리스트를 복사해서 반환, 있으면 해당 성별만 걸러 새 리스트로 반환.

### 7-4. `ChatSessionService`
**게임의 핵심 비즈니스 로직을 조합하는 서비스.** `TextAnalysisEngine`(텐션 계산), `MatchEngine`(유사도), `GroqDialogueService`(대사 생성)를 조합.
- **필드**: `textAnalysisEngine`, `dialogueService`, `allPersonas`(전체 페르소나 목록 — "더 잘 맞는 상대" 탐색에 필요).
- **`openSession(Persona persona)`**: 새 `MeltingContext`를 만들어 반환.
- **`processTurn(MeltingContext session, Persona persona, String userInput)`**
  유저 입력을 `textAnalysisEngine.analyze()`로 분석 → 결과를 `session.applyResult()`로 세션에 반영 → 분석 결과를 그대로 반환(UI가 로그로 보여줄 수 있게).
- **`generateReply(...)`**
  먼저 `dialogueService.generateReply()`(Groq 호출)를 시도 → 결과가 있으면 그대로 반환 → 없으면(`null`/공백) `fallbackLine()`으로 대체. **이 메서드 하나가 "LLM 우선, 실패 시 JSON 폴백" 전체 흐름의 진입점.**
- **`fallbackLine(...)`**
  현재 상태(`tierKey`)에 맞는 접두사(`config.randomPrefix`)와 대사(`persona.randomLine`)를 조합해 `"(접두사) 대사"` 형태의 한 줄을 만듦. 접두사가 없으면 대사만.
- **`isLlmEnabled()`**: `dialogueService.isEnabled()`를 그대로 위임(UI가 "API 키 없음" 안내 문구를 보여줄지 판단하는 데 사용).
- **`buildReport(MeltingContext session, Persona persona)`**
  1) `MatchEngine.similarityPercent()`로 대화 상대와의 싱크로율 계산.
  2) **트롤링 페널티**: 텐션이 30 미만이면 `(tension+10)/100.0`이라는 `penaltyRatio`(0~1 사이)를 계산해 유사도에 곱함 — 대화를 대충/못하게 했으면 아무리 벡터가 잘 맞아도 결과 % 를 깎는 장치.
  3) 전체 `allPersonas`를 순회하며 유저 벡터와 가장 잘 맞는 후보(`best`)를 찾되, **이 후보에도 동일한 `penaltyRatio`를 적용**(공정성 — 대화한 상대만 페널티 받으면 불공평하므로).
  4) `best`가 현재 대화 상대와 다르면 "더 좋은 매칭이 있다"는 정보를 `Optional`로 포장.
  5) 최종적으로 `MatchReport`를 만들어 반환.

**예상 질문 대비**: "penaltyRatio는 왜 있는가?" → 유저가 성의 없이 대화해서 호감도(텐션)가 낮은데도 코사인 유사도만으로 100%에 가까운 궁합이 나오면 게임의 몰입감이 떨어지므로, 텐션이 낮으면 궁합도 결과 자체를 깎아 "성의 있는 대화"를 유도하는 게임 밸런스 장치.

---

## 8. `ui` 패키지 — 콘솔 프레젠테이션 계층

### `ConsoleUI`
`Scanner` + `System.out.println`만으로 구동되는 화면 계층. **로직은 전혀 모르고 오직 입출력만** 담당(모든 계산은 `ChatSessionService`/`PersonaBrowseService`에 위임).

- **상수**
  - `INITIAL_TURNS = 7` : 기본 대화 턴 수.
  - `EXTEND_TURNS = 3` : "더 답변하기" 선택 시 추가되는 턴 수.
  - `TYPING_DELAY_MS = 18` : 타이핑 효과 글자당 지연(ms).
  - `THINKING_DELAY_MS = 550` : 페르소나가 "생각하는" 듯한 지연(ms).

- **필드**: `gameData`, `scanner`, `chatSessionService`.

- **`run()`**
  배너 출력 → `selectBrowseTargets()`로 성별 필터링된 후보 목록을 얻음 → 후보가 없으면 즉시 종료 안내 → `browseAndMatch()`로 실제 탐색/대화 진행 → 종료 인사.

- **`selectBrowseTargets()`**
  사용자에게 M/F/A 입력을 받아 `PersonaBrowseService.parseGenderChoice()`로 해석 → `printGenderFilterNotice()`로 안내 문구 출력 → `filterByGender()`로 최종 후보 리스트 반환.

- **`printGenderFilterNotice(Optional<Gender>)`**
  필터가 없으면 "전체 탐색", 있으면 "남성/여성만 탐색" 문구 출력.

- **`browseAndMatch(List<Persona> candidates)`**
  후보 목록(`remaining`)이 남아있는 동안 반복:
  1) `printPersonaList()`로 번호 매긴 전체 목록 출력.
  2) 번호 입력받아 `parseIndexChoice()`로 유효성 검증(범위 밖/숫자 아님이면 안내 후 `continue`).
  3) 선택된 페르소나로 `runChatSession()` 실행 → 최종 매칭이 확정되면(`true`) 전체 탐색 종료, 아니면(`false`, "결과 보기"로 끝남) 다시 목록으로 돌아와 계속 탐색 — **"다음 후보에도 못 가고 종료돼버리는" 예외 상황을 없애기 위한 while 루프 구조**.
  4) `"0"` 또는 `"Q"` 입력 시 즉시 종료.

- **`printPersonaList(List<Persona>)`**
  `[번호] 이름(성별) (나이세 / MBTI) 한줄소개` 형태로 전체 후보를 출력(요청하신 "번호 목록에서 바로 선택" UI).

- **`parseIndexChoice(String rawInput, int size)`**
  문자열을 정수로 파싱 시도 → 1~size 범위 안이면 (0-based) 인덱스로 변환해 반환, 아니면 `-1`(무효)로 반환. `NumberFormatException`을 잡아 숫자가 아닌 입력도 안전하게 처리.

- **`runChatSession(Persona persona)`**
  1) 새 세션(`MeltingContext`) 열고 채팅방 시작 안내 출력.
  2) LLM 비활성 상태면 API 키 설정 안내 문구 출력.
  3) 첫 인사는 아직 유저 발화가 없으므로 `fallbackLine()`(JSON 대사)로 시작.
  4) `while(true)` 루프: `runTurns()`로 목표 턴까지 대화 진행 → `askBranchChoice()`로 분기 선택 받기 → `SHOW_RESULT`면 `presentResult()` 후 `false` 반환, `EXTEND_CHAT`이면 목표 턴 수를 늘리고 계속, `CONFIRM_MATCH`면 `presentFinalMatchConfirmation()` 후 `true` 반환, `INVALID`면 안내 후 재입력.

- **`runTurns(MeltingContext, Persona, int fromTurn, int toTurn)`**
  지정된 구간만큼 턴을 반복: 화제 힌트 출력 → 유저 입력 받기 → `processTurn()`으로 분석 → `printEngineTrace()`로 내부 연산 로그 출력(디버그/발표용 시각화) → 잠깐 "생각 중" 지연 → `generateReply()`로 답변 생성해 출력 → `session.recordTurn()`으로 기록. 완료된 턴 번호(`turn`)를 반환해 다음 구간 계산에 이어붙임.

- **`printReplyHint(Persona)`**
  페르소나의 `loveKeywords` 중 하나를 무작위로 뽑아 "귀띔" 문구로 보여줌 — 완전 자유 입력이지만 참고할 힌트를 살짝 주는 **하이브리드 입력 방식**.

- **`printEngineTrace(AnalysisResult, MeltingContext)`**
  매칭된 키워드, 이번 턴 텐션 변화량(부호 포함), 누적 텐션을 한 줄로 출력 — 내부 로직이 "블랙박스"가 아니라는 걸 보여주는 발표용 로그.

- **`askBranchChoice(int turnsSoFar)`**
  1/2/3 선택지를 출력하고 입력을 `ChatBranchChoice.fromInput()`으로 변환해 반환.

- **`presentResult(MeltingContext, Persona)`**
  Groq에게 "짧은 작별 인사"를 만들어달라는 **메타 지시문**(`"(오늘 대화는 여기까지 하자면서...)"`)을 `generateReply()`에 넘겨 자연스러운 마무리 대사를 받음 → `buildReport()`로 최종 리포트 계산 → 싱크로율(%)과, 있다면 "더 잘 맞는 상대" 정보를 출력.

- **`presentFinalMatchConfirmation(MeltingContext, Persona)`**
  마찬가지로 메타 지시문(축하 인사 요청)을 Groq에 넘겨 확정 멘트를 받아 출력하고 안내 문구로 마무리.

- **`printPersonaLine(Persona, String tierDisplayName, String line)`**
  `"[이름(성별) - 상태]: "` 접두 출력 후 `printTyping()`으로 한 글자씩 출력.

- **`printTyping(String text)`**
  글자를 한 자씩 출력하며 `sleepQuietly(TYPING_DELAY_MS)`로 지연 — 실제 메신저처럼 타자 치는 느낌을 연출.

- **`sleepQuietly(long millis)`**
  `Thread.sleep()`을 감싸 `InterruptedException`을 처리(인터럽트 상태를 다시 세팅하는 `Thread.currentThread().interrupt()` 관례를 따름).

- **`printBanner()`**
  게임 시작 시 타이틀 배너 출력.

- **`safeNextLine()`**
  `scanner.hasNextLine()`을 먼저 확인해 입력 스트림이 끝났을 때(EOF) 예외 없이 빈 문자열을 반환하도록 방어.

---

## 9. `Main` — 조립 루트(Composition Root)

- **상수** `DEFAULT_DATA_PATH = "resources/personas.json"`.
- **`main(String[] args)`**
  1) `configureUtf8Console()`로 콘솔 인코딩을 UTF-8로 고정(한글 깨짐 방지).
  2) 실행 인자가 있으면 그 경로를, 없으면 기본 경로를 JSON 경로로 사용.
  3) `loadGameDataOrExit()`로 데이터 로드(실패하면 `null` 반환받아 프로그램 조용히 종료).
  4) `TextAnalysisEngine`, `GroqDialogueService`(`buildDialogueService()`), `ChatSessionService`를 순서대로 생성하며 의존성을 주입(Dependency Injection을 손으로 직접 함 — "Poor Man's DI").
  5) `Scanner`를 UTF-8로 만들고 `try-with-resources`로 감싸 `ConsoleUI.run()` 실행 → 프로그램 종료 시 자동으로 `Scanner`가 닫힘.
- **`buildDialogueService()`**
  `GroqConfigLoader`로 키/모델을 로드 → 키가 없으면 "폴백 전용" 서비스(`enabled=false`)를 만들고 안내 문구 출력 → 있으면 `GroqClient`를 만들어 활성화된 서비스를 반환.
- **`configureUtf8Console()`**
  `System.out`/`System.err`을 UTF-8 인코딩의 `PrintStream`으로 교체 — 윈도우 등 기본 인코딩이 다른 환경에서도 한글이 깨지지 않게 하기 위함.
- **`loadGameDataOrExit(String jsonPath)`**
  `PersonaRepository.loadFromPath()`를 호출하되 `IOException`을 잡아 에러 메시지 출력 후 `null` 반환. 로드는 성공했지만 페르소나가 0명이면(빈 JSON) 별도로 에러 처리 — **"부분적으로 성공한 것처럼 보이지만 실제로는 못 쓰는 상태"를 조기에 차단**.

---

## 10. 전체 실행 흐름 요약 (발표 스크립트용)

1. `Main`이 UTF-8 콘솔 설정 → `personas.json` 로드 → Groq 연동 여부 결정 → 모든 서비스 객체를 조립.
2. `ConsoleUI`가 성별 필터를 물어보고, 조건에 맞는 페르소나를 번호 목록으로 보여줌.
3. 유저가 번호를 고르면 `MeltingContext`(세션)가 열리고 7턴 대화가 시작됨.
4. 매 턴: 유저 입력 → `TextAnalysisEngine`이 규칙 기반으로 텐션/성향 벡터 변화 계산 → `MeltingContext`에 반영(상태 전이 포함) → `GroqDialogueService`가 시스템 프롬프트+대화 기록을 담아 Groq API 호출 → 성공하면 그 응답을, 실패하면 JSON 폴백 대사를 타이핑 효과로 출력.
5. 7턴이 끝나면 [결과 보기 / 더 답변하기 / 매칭하기] 중 선택.
6. "결과 보기"를 고르면 `MatchEngine`의 코사인 유사도 + 텐션 기반 페널티로 싱크로율을 계산해 리포트 출력, 그리고 목록으로 복귀해 다른 페르소나를 계속 탐색 가능.
7. "매칭하기"를 고르면 최종 확정 멘트를 출력하고 전체 탐색 종료.

---

## 11. 교수님이 물어볼 만한 핵심 질문 & 모범 답변 포인트

| 예상 질문 | 답변 포인트 |
|---|---|
| 왜 순수 자바로만 짰나? | 과제 요구사항(제로 외부 의존성) 준수, JSON 파싱/HTTP 통신까지 표준 라이브러리로 직접 구현해 자바의 기본기를 보여주기 위함 |
| 상태 패턴을 왜 썼나? | if-else 중첩 없이 다형성으로 "밀당→능글→치명" 단방향 전이를 표현, 새 상태 추가 시 기존 코드 수정 불필요(OCP) |
| LLM이 실패하면 어떻게 되나? | `GroqDialogueService.generateReply()`가 예외를 잡아 `null` 반환 → `ChatSessionService`가 자동으로 JSON 폴백 대사로 전환, 게임이 절대 멈추지 않음 |
| 텐션 계산과 LLM 응답의 관계는? | 텐션/상태 전이는 100% 결정론적인 `TextAnalysisEngine`이 계산, Groq는 그 상태에 맞는 "문장 생성"만 담당 — 점수의 공정성/일관성 확보 |
| 코사인 유사도를 왜 썼나? | 유저의 3차원 성향 벡터와 각 페르소나의 목표 벡터 간 방향 유사도를 측정해 "성향이 비슷한 정도"를 정량화하기 위함 |
| penaltyRatio(트롤링 페널티)는 왜? | 대화를 성의 없이 해서 텐션이 낮으면, 벡터상 궁합이 좋아도 결과 %를 깎아 게임의 몰입감/공정성 확보 |
| 왜 서비스/UI/엔진 계층을 나눴나? | 관심사 분리 — UI를 웹으로 바꿔도 `service`/`engine`은 그대로 재사용 가능, 단위 테스트도 I/O 없이 가능 |