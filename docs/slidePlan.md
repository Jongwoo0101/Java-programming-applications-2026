## 📑 [파트 1] 슬라이드 1 ~ 12 (담당자 1)

### Slide 10: 자료구조 적용 1 - `List`와 `ArrayList` (1)

**PPT에 들어갈 코드 (`PersonaRepository.java`)**

```java
// 19명의 AI 프로필 데이터를 담기 위한 리스트 초기화
List<Object> personasRaw = (List<Object>) root.get("personas");
List<Persona> personas = new ArrayList<>();

if (personasRaw != null) {
    for (Object personaObj : personasRaw) {
        // 파싱된 데이터를 ArrayList에 순차적으로 추가
        personas.add(toPersona((Map<String, Object>) personaObj));
    }
}

```

### Slide 11: 자료구조 적용 1 - `List`와 `ArrayList` (2)

**PPT에 들어갈 코드 (`MeltingContext.java`)**

```java
// 멀티턴 대화 기록을 시간 순서대로 저장하기 위한 ArrayList
private final List<ChatTurn> history = new ArrayList<>();

/** 이번 턴의 (유저 발화, AI 응답)을 대화 기록에 추가한다. */
public void recordTurn(String userText, String personaText) {
    // 과거 기록은 수정되지 않고 항상 끝에만 추가(Append)됨
    history.add(new ChatTurn(userText, personaText));
}

/** LLM 요청 시 함께 전달할 지금까지의 대화 기록 (읽기 전용) */
public List<ChatTurn> getHistory() {
    return Collections.unmodifiableList(history);
}

```

### Slide 12: 자료구조 적용 2 - `Map`과 `HashMap` (1)

**PPT에 들어갈 코드 (`Persona.java` & `TextAnalysisEngine.java`)**

```java
// 특정 AI 프로필이 선호하거나 싫어하는 키워드 사전 (Key-Value)
private final Map<String, Integer> loveKeywords;
private final Map<String, Integer> hateKeywords;

// 검색 속도 O(1)을 활용한 실시간 텍스트 채점 엔진 로직
private int scoreDictionary(String input, Map<String, Integer> dictionary) {
    int total = 0;
    for (Map.Entry<String, Integer> entry : dictionary.entrySet()) {
        String stem = entry.getKey();
        // HashMap의 Key(단어)가 유저의 채팅에 포함되어 있다면 Value(점수)를 즉시 더함
        if (containsStem(input, stem)) {
            total += entry.getValue(); 
        }
    }
    return total;
}

```

---

## 📑 [파트 2] 슬라이드 13 ~ 24 (담당자 2)

*20~24번 슬라이드는 프로젝트 결과, 회고, Q&A 등이므로 코드가 들어가지 않습니다.*

### Slide 13: 자료구조 적용 2 - `Map`과 `LinkedHashMap` (2)

**PPT에 들어갈 코드 (`SimpleJsonParser.java` - 자체 구현 JSON 파서)**

```java
private Map<String, Object> parseObject() {
    // JSON의 입력 순서를 그대로 보장하기 위해 일반 HashMap이 아닌 LinkedHashMap 사용
    Map<String, Object> map = new LinkedHashMap<>();
    
    expect('{');
    while (true) {
        String key = parseString();
        expect(':');
        Object value = parseValue();
        
        map.put(key, value); // Key-Value 구조로 객체 매핑
        
        if (peek() == '}') break;
        expect(',');
    }
    return map;
}

```

### Slide 15: 코사인 유사도의 순수 Java 코드 구현

*(14번 슬라이드는 코사인 유사도 수학 공식 이미지를 넣으시면 됩니다.)*
**PPT에 들어갈 코드 (`MatchEngine.java`)**

```java
/** 외부 수학 라이브러리 없이 구현한 코사인 유사도 (Cosine Similarity) */
public static double cosineSimilarity(double[] a, double[] b) {
    double dotProduct = 0.0;
    double magnitudeA = 0.0;
    double magnitudeB = 0.0;

    for (int i = 0; i < a.length; i++) {
        dotProduct += a[i] * b[i]; // 내적 계산
        magnitudeA += Math.pow(a[i], 2);
        magnitudeB += Math.pow(b[i], 2);
    }

    double denominator = Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB);
    if (denominator == 0.0) return 0.0;
    
    return dotProduct / denominator; // 방향성(비율) 비교 반환
}

```

### Slide 16: 알고리즘 한계 극복 - 비매너 유저 페널티 로직

**PPT에 들어갈 코드 (`ChatSessionService.java`)**

```java
public MatchReport buildReport(MeltingContext session, Persona persona) {
    double syncPercent = MatchEngine.similarityPercent(session.getUserVector(), persona.getTargetVector());

    // [트롤링 방어] 텐션이 30점 미만이면 매칭률 강제 폭락 (비율의 맹점 보완)
    double penaltyRatio = 1.0;
    if (session.getTension() < 30) {
        penaltyRatio = Math.max(0.0, (session.getTension() + 10) / 100.0);
    }

    // 대화한 상대 및 추천 상대의 최종 매칭률에 페널티 일괄 적용
    syncPercent = syncPercent * penaltyRatio;
    
    // (중략 ... 전체 후보 추천 로직)
    return new MatchReport(persona, syncPercent, betterMatch, betterMatchPercent);
}

```

### Slide 17: 객체지향 설계 - 상태 패턴 (State Pattern) 적용

**PPT에 들어갈 코드 (`MeltingState.java` & `ChillyState.java`)**

```java
// 1. 상태 패턴 인터페이스
public interface MeltingState {
    MeltingState evaluateTransition(int tension); // 다형성을 이용한 상태 전이 평가
}

// 2. 초기 상태 (밀당 단계) 구현체
public final class ChillyState implements MeltingState {
    public static final ChillyState INSTANCE = new ChillyState();

    @Override
    public MeltingState evaluateTransition(int tension) {
        if (tension >= 70) {
            return SteamyState.INSTANCE; // 치명 단계로 전이
        } else if (tension >= 30) {
            return FlirtyState.INSTANCE; // 능글 단계로 전이
        }
        return this; // 상태 유지
    }
}

```

### Slide 18: LLM 연동을 위한 자바 HTTP 통신 코드

**PPT에 들어갈 코드 (`GroqClient.java`)**

```java
// JDK 11+ 내장 HttpClient를 이용한 외부 라이브러리 없는 REST API 호출
public String generateContent(String requestBodyJson) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Authorization", "Bearer " + apiKey) // 인증 토큰
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
            .build();

    HttpResponse<String> response = 
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    return response.body();
}

```

### Slide 19: 서비스 장애 대비 - Fallback (안전망) 시스템

**PPT에 들어갈 코드 (`GroqDialogueService.java` & `ChatSessionService.java`)**

```java
// 1. GroqDialogueService 내부 (LLM 호출 실패 시)
try {
    String rawResponse = client.generateContent(requestBody);
    return extractText(rawResponse);
} catch (Exception e) {
    // API 장애, Rate Limit 초과 시 앱이 뻗지 않고 null 반환
    return null; 
}

// 2. ChatSessionService 내부 (안전망 가동)
String llmReply = dialogueService.generateReply(persona, session, userInput);

if (llmReply != null && !llmReply.isBlank()) {
    return llmReply; // 1순위: 성공 시 AI 자연어 대사 출력
}
// 2순위: 실패 시 로컬 JSON에 미리 저장된 오프라인 대사(Fallback) 출력 (무중단 서비스)
return fallbackLine(session, persona, config); 

```