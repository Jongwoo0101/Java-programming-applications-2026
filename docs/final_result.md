# [프로젝트 계획서] PersonaSync Core Engine (Pure Java Edition)

## 1. 개발 목표

* 외부 라이브러리나 API 없이 Java SE 표준 API만을 사용하여 컴파일 및 구동이 가능한 제로 의존성(Zero-Dependency) 시스템을 구축합니다.


* LLM의 불확실한 프롬프트 제어 대신, 자바 코어 엔진이 유저의 입력을 직접 형태소/키워드 레벨에서 파싱하여 감정 상태 전이를 엄격하게 통제합니다.


* 유저 성향을 다차원 벡터로 추상화하고 수학적 매칭 알고리즘을 적용하여, 객체지향 설계와 데이터 구조화 역량을 증명합니다.



## 2. 시스템 기능

* 14인의 다채로운 페르소나 객체를 틴더 스타일(L: 거절, R: 수락)로 탐색하고 매칭하는 기능을 제공합니다.


* 자바 정규표현식(Regex)과 Stream API를 통해 입력된 대사 문자열을 고속 분석하여 텐션 점수(Melt Rate)를 실시간으로 스코어링합니다.


* Melt Rate 수치의 동적 변화에 따라 대화 분위기를 밀당(Chilly) 단계에서 능글(Flirty), 치명(Steamy) 상태로 완전히 독립되게 전이시킵니다.



## 3. 사용자 특성

* 화려한 GUI보다 시스템 내부의 엄밀한 텍스트 분석 로직과 알고리즘적 상호작용의 정확도를 중시하는 진성 탐구형 유저.
* 텍스트 기반의 몰입형 롤플레잉에 거부감이 없으며, 대화 속에서 발현되는 자신의 은밀한 성향을 수학적/객체지향적 지표로 진단받고자 하는 2030 세대.

## 4. 기능요구사항

* 유저 대사에서 '너', '선' 등의 단어를 추출하여 주도성(Dom) 수치에 가중치를 부여하고, 텍스트 길이에 따라 적극성 데이터를 다차원 배열에 누적해야 합니다.


* 텐션 점수가 30을 초과하면 Flirty 상태로, 70을 초과하면 Steamy 상태로 전환되어 출력 대사(Reply)가 변경되어야 합니다.


* 3턴의 대화 세션 종료 후, 유저의 성향 벡터와 타겟 이성 벡터 간의 코사인 유사도(Cosine Similarity)를 계산하여 백분율(%) 싱크로율로 도출해야 합니다.



## 5. 인터페이스요구사항

* 무거운 웹 프레임워크 그래픽 렌더링 대신, 순수 `Scanner`와 `System.out.println`을 활용한 CUI(Command-line User Interface) 환경으로 콘솔 내에서 구동되어야 합니다.


* 키보드 'L'(Next Card)과 'R'(Match Chat Room) 키를 통해 직관적이고 즉각적인 상호작용 피드백을 제공해야 합니다.


---

## 6. UML 다이어그램 명세 (설계 뼈대)

* **유즈케이스 다이어그램:** 유저는 '프로필 탐색', '1:1 대화 입력', '매칭 결과 확인'의 액터를 수행하며, 시스템 액터는 '정규식 키워드 파싱', '상태 전이 제어', '코사인 유사도 연산'을 백그라운드에서 처리합니다.
* **클래스 다이어그램 (도메인 VO):** `Persona` 클래스는 페르소나의 이름, MBTI, 3단계 감정 대사(chillyLine, flirtyLine, steamyLine)를 캡슐화합니다.


* **클래스 다이어그램 (상태 패턴):** 감정 상태를 통제하는 `MeltingState` 인터페이스를 선언하고, 이를 `ChillyState`, `FlirtyState`, `SteamyState` 클래스가 구현하여 다형성을 확보합니다.


* **클래스 다이어그램 (컨텍스트 및 엔진):** 세션을 통제하고 3차원 유저 벡터 배열을 관리하는 `MeltingContext`, 정규식 기반 가중치를 계산하는 `TextAnalysisEngine`, 수학적 분석을 수행하는 `MatchEngine`으로 구성됩니다.


* **시퀀스 다이어그램:** 사용자의 텍스트 입력 $\rightarrow$ `MeltingContext` 수신 $\rightarrow$ `TextAnalysisEngine`의 스코어 반환 $\rightarrow$ `MeltingContext` 내 점수 업데이트 $\rightarrow$ `MeltingState`의 상태 전이 평가(`handleTension`) $\rightarrow$ 다형성에 의한 AI 응답 반환 순서로 객체가 협력합니다.


* **액티비티 (활동) 다이어그램:** [프로그램 시작] $\rightarrow$ [14인 인스턴스 팩토리 로드] $\rightarrow$ [조건: L/R 판별] $\rightarrow$ [1:1 세션 개설] $\rightarrow$ [텍스트 분석 및 벡터 스코어 누적] $\rightarrow$ [조건: 임계치 30/70 돌파 여부] $\rightarrow$ [상태 전이] $\rightarrow$ [3턴 반복 종료] $\rightarrow$ [매칭 유사도 연산 및 출력] $\rightarrow$ [시스템 종료]의 순서도 흐름을 따릅니다.



---

## 7. 시스템 구조도 (System Architecture)

* **Presentation Layer (Console I/O):** 사용자와 터미널 간의 텍스트 기반 스트림 통신을 담당하는 최상단 레이어입니다.


* **Business Logic Layer (State Machine):** 입력된 데이터를 바탕으로 상태 패턴(State Pattern)을 적용하여 페르소나의 감정과 텐션을 제어하는 두뇌 역할을 수행합니다.


* **Data & Math Layer (Vector Processing):** 3차원 유저 배열 `[주도성, 순종성, 적극성]`을 누적하고, 이를 타겟 유저 벡터와 매칭시키는 수학적 연산 코어입니다.


* **Repository Layer (Instance Factory):** 프로그램 실행 시 하드코딩된 남녀 14인의 페르소나 객체 인스턴스를 메모리에 로드하고 리스트 컬렉션으로 제공하는 데이터 저장소입니다.



## 8. 프로그램 설계도 (Program Design)

외부 머신러닝 라이브러리의 블랙박스 의존성을 철저히 배제하고, 입력과 출력이 명확한 구조적 프로그래밍을 적용했습니다. 텍스트 분석은 해시맵(HashMap) 기반의 어휘 사전(Lexicon)과 스트림(Stream) 필터링을 결합하여 고속 처리합니다.

최종 매칭 알고리즘은 아래의 코사인 유사도(Cosine Similarity) 산출 공식을 적용합니다.

$$\text{Similarity} = \frac{\sum_{i=1}^{n} (A_i \times B_i)}{\sqrt{\sum_{i=1}^{n} A_i^2} \times \sqrt{\sum_{i=1}^{n} B_i^2}}$$

위 수학 공식을 `MatchEngine` 클래스 내부에서 순수 자바 반복문(`for`)과 `Math.pow`, `Math.sqrt` 메서드만으로 완벽히 치환하여 구현함으로써, 하드웨어 성능 저하 없이 최적의 매칭 결과를 도출하도록 프로그램 로직을 설계했습니다.