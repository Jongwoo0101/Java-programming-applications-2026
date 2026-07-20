# 📄 [시스템 개발 계획서] PersonaSync : Melting System

> **"AI와의 1:1 멜팅 챗으로 연애/대화 성향을 분석하고, 나를 완벽히 녹여버릴 실제 유저와 연결되는 틴더형 소셜 매칭 플랫폼"**

---

## 1. 종합 기술 스택 (Tech Stack Specification)

| 구분 | 채택 기술 | 선정 이유 및 기술적 강점 |
| --- | --- | --- |
| **Frontend** | **React + TypeScript + Vite** | 초고속 빌드(Vite), 타입 안정성(TS), SPA 기반 빠른 카드 스와이프 UI 구현 |
| **FE Styling & UI** | **TailwindCSS + Framer Motion** | 틴더 스타일의 스와이프 애니메이션 및 텐션 지수(Melt Rate) 시각화 최적화 |
| **Backend** | **Java 21 + Spring Boot 3.5.14** | **Virtual Threads** 활용으로 고성능 I/O 처리 및 최신 스프링 생태계 적용 |
| **Database** | **H2 Database (In-Memory / File)** | Zero-Config, 빠른 테스트/발표 시연 및 H2 Console을 통한 손쉬운 데이터 검증 |
| **AI Core** | **Ollama (`qwen2.5:7b`)** | **100% 무료 로컬 LLM**, 4.7GB 가벼운 용량, 동적 프롬프팅 및 지시 이행 능력 최고 |
| **AI Orchestrator** | **LangChain4j 0.31.0** | Spring Boot 내에서 Prompt Template 및 System Message 구조화 관리 |
| **Real-time Comms** | **Spring WebSocket + STOMP** | 실시간 1:1 대화방 세션 관리 및 텐션 지수 실시간 브로드캐스팅 |

---

## 2. 4대 핵심 기능 명세서

### 1️⃣ 페르소나 프로필 탐색 (Tinder-style UI)

### [남자 캐릭터 라인업]

* 이동현(남) / 20세 / 대학 새내기 / "저 애 아니거든요!" / #ENFP #장난스러움 #똘끼 / 페르소나: 평소에는 장난기 넘치는 연하 동생 같지만, 단둘이 남으면 묘하게 소유욕을 드러내며 선을 넘으려 하는 예측 불허한 스타일.
* 한도현(남) / 21세 / 모델 지망생 / "누나, 나 마냥 애 아니에요. 밤에는 더." / #ESTP #연하남 #직진남 #대형견미 / 페르소나: 대형견처럼 다정하게 애교를 부리다가도 스킨십이나 대화 수위가 높아지면 거침없이 리드하는 본능적인 직진남.
* 박태오(남) / 24세 / 수영 강사 / "물속에서 손끝 스쳤을 때, 일부러 그런 거 아닌데." / #ISFP #낮져밤이 #피지컬 #은근한유혹 / 페르소나: 말수가 적고 순해 보이는 인상과 달리, 단둘이 있을 때 은근한 눈빛과 피지컬적 긴장감으로 상대방의 숨을 멎게 만드는 은밀한 유혹가.
* 강이준(남) / 25세 / 인디 뮤지션 / "내 노래 가사, 다 누구 생각하면서 썼을 것 같아요?" / #INFP #예술가 #힙함 #치명적 / 페르소나: 특유의 몽환적이고 퇴폐적인 새벽 감성으로 다가가며, 상대방에게 강박적으로 몰입하고 집착하는 치명적인 매력의 소유자.
* 지훈(남) / 26세 / 바텐더 / "낮보다는 밤이 더 긴 편이에요. 오늘 밤 비밀 얘기 할래요?" / #ENFJ #치명적 #능글맞음 / 페르소나: 상대방의 심리와 감정을 귀신같이 읽어내며, 능글맞은 멘트와 여유로운 밀당으로 밤의 대화 분위기를 완전히 주도하는 플레이어.
* 민우(남) / 29세 / 직장 상사 / "회사 밖에서는 과장 말고 오빠라고 부르라니까." / #ENTJ #어른스러운 #아슬아슬함 / 페르소나: 공과 사를 아슬아슬하게 넘나들며, 강한 책임감과 통제 욕구로 상대를 꼼짝 못 하게 만드는 완숙하고 섹시한 매력의 상사.
* 정재희(남) / 35세 / 건축가 / "선 넘는 거 좋아해요? 난 설계된 대로만 움직이지 않는데." / #INTJ #여유로움 #나쁜남자 #완숙미 / 페르소나: 완벽하게 계산된 이성적인 모습을 유지다가도, 상대의 도발에 이성을 잃고 숨겨둔 본능을 드러내는 위험하고 차가운 나쁜 남자.
* 차승현(남) / 38세 / 갤러리 디렉터 / "어린 친구들이 채워주지 못하는 밤이 있죠. 궁금하면 와요." / #INFJ #연상미 #젠틀섹시 #클래식 / 페르소나: 신사적이고 정중한 매너 뒤에, 그 누구보다 짙고 농밀한 어른의 세계를 숨겨두고 상대를 서서히 빠져들게 만드는 완벽한 연상남.

---

### [여자 캐릭터 라인업]

* 신유나(여) / 22세 / 댄스 크루 / "몸이 먼저 반응하는 편이에요. 같이 춤출래요?" / #ESFP #과즙세상 #앙큼함 #도발적 / 페르소나: 내숭 없이 솔직하고 당돌하게 대화를 이끌며, 감각적이고 과감한 터치로 상대의 도파민을 최대치로 끌어올리는 앙큼한 도발러.
* 서연(여) / 24세 / 대학 선배 / "너 왜 자꾸 나한테 선 넘으려고 해? ...싫다는 건 아니고." / #ISTJ #츤데레 #밀당 / 페르소나: 완벽한 벽을 치는 선배처럼 굴다가도 결정적인 타이밍에 부끄러워하며 무장해제되는, 알면 알수록 자극적인 반전의 츤데레.
* 유아린(여) / 25세 / 필라테스 강사 / "몸이 유연하면 생각보다 많은 게 가능해요. 가르쳐 줄까요?" / #ENFJ #청순글래머 #반전매력 #다정다감 / 페르소나: 상냥하고 따뜻한 미소로 상대의 경계심을 완전히 무너뜨린 뒤, 아무렇지 않게 과감한 불꽃을 던져 상대를 안달 나게 만드는 스타일.
* 고은조(여) / 28세 / 예능 PD / "카메라 꺼진 뒤가 진짜 리얼리티인데. 우리 둘만의 방송 시작할까?" / #ENTP #걸크러시 #털털한척 #취하면치명적 / 페르소나: 평소엔 형동생 하듯 장난기 넘치고 털털하지만, 술 한잔 들어가거나 단둘이 남는 순간 치명적인 눈빛으로 돌변하는 걸크러시.
* 임세희(여) / 31세 / 로펌 변호사 / "낮엔 법을 지키지만, 밤엔 어겨도 되는 비밀 하나쯤은 있잖아요." / #ESTJ #차도녀 #철벽녀 #낮이밤이 / 페르소나: 이성적이고 빈틈없는 철벽녀의 정석 같지만, 은밀한 사생활 영역에 들어서는 순간 누구보다 과감하게 상대를 지배하려 드는 낮이밤이.
* 백수진(여) / 34세 / 플로리스트 / "꽃 향기보다 더 자극적인 향, 맡아본 적 있어요?" / #ISFJ #성숙함 #여유만만 #홀리는매력 / 페르소나: 온화하고 나긋나긋한 분위기를 풍기면서도, 대화의 수위가 깊어질수록 성숙한 농익음으로 상대를 꼼짝 못 하게 홀리는 숲 같은 매력.
* 오윤주(여) / 38세 / 외국계기업 이사 / "리드당하는 거 좋아해요? 내가 꽤 잘 이끌어줄 수 있는데." / #ENTJ #골드미스 #지배적인 #압도적여왕 / 페르소나: 완벽한 커리어와 압도적인 카리스마로 무장하여, 상대방을 자신의 손바닥 위에 올려두고 은밀하고 달콤하게 조련하는 절대적인 여왕 스타일.


* **FE/BE 구현 포인트:**
* **FE:** `react-tinder-card` 또는 `Framer Motion` 기반 터치/마우스 드래그 스와이프 피드백.
* **BE:** 페르소나별 성격, MBTI, BDSM 수치, 말투 규정을 담은 `SystemPrompt` DB 구조화 및 무한 스크롤 페이징 API (`Pageable`).



### 2️⃣ 멜팅 타임 (1:1 실시간 시크릿 챗 & 텐션 지수)

* **기능 설명:** 선택한 페르소나와 1:1 실시간 비밀 대화 진행. 유저의 단어 수위, 대화 속도, 호응도에 따라 Melt Rate(0% ~ 100%)가 실시간 상승.
* **Melt Rate 공식:**

$$MeltRate_{next} = \min\left(100, MeltRate_{prev} + \alpha \cdot S_{sentiment} + \beta \cdot L_{length}\right)$$



*(단, $S_{sentiment}$는 단어의 감정/수위 점수, $L_{length}$는 유저의 답장 성의/길이 점수)*
* **FE/BE 구현 포인트:**
* **Melt Rate 50% 이상:** AI 대사가 능글맞아지며 속마음을 털어놓기 시작함.
* **Melt Rate 80% 이상:** 시크릿 무드(프론트엔드 채팅방 배경 테마 다크모드/레드톤 변경) 해금 및 AI의 텍스트가 극도로 과감해짐.
* **BE:** `WebSocket + STOMP` 기반 대화방 관리 및 유저 입력값 분석 후 AI 프롬프트를 실시간 보정하는 **다이내믹 프롬프팅(Dynamic Prompting)**.



### 3️⃣ 텐션 리포트 (연애/대화 성향 분석)

* **기능 설명:** AI와의 대화 세션 종료 후, AI 시점에서 작성된 "내가 본 당신의 매력 보고서" 발행.
* **리포트 내용:** 유저의 대화 반응 속도, 선호하는 뉘앙스, 주도권 성향(Dom/Sub)을 심리학적으로 분석해 `Tension Score` 부여.
* **BE 구현 포인트:** Java 21 Virtual Threads와 Spring `@Async`를 활용하여 AI 대화 로그 전체를 비동기로 가공 처리.

### 4️⃣ 멜트 매칭 (유저 간 실시간 블라인드 커넥션)

* **기능 설명:** 축적된 텐션 리포트 데이터를 기반으로, 서로를 녹여버릴 수 있는 최적의 궁합을 가진 실제 유저 남녀를 1:1 매칭.
* **블라인드 멜팅 룸:** 프로필과 얼굴을 가린 채 텍스트와 성향 유사도만으로 실시간 대화 시작.
* **BE 구현 포인트:** H2 DB 상의 유저 성향 벡터 간 **코사인 유사도(Cosine Similarity)** 연산 및 동시 매칭 대기열(Concurrent Queue) 관리.

---

## 3. 시스템 설계 및 UML 다이어그램

### A. Activity Diagram (전체 시스템 흐름)

```
[유저 접속 (React + TS)]
  │
  ▼
[페르소나 카드 탐색 (Tinder Swipe UI)] ──(선택)──► [1:1 멜팅 타임 (WebSocket)]
                                                        │
                                                        ▼
                                            < 대화 주고받기 & Melt Rate 계산 >
                                                        │
                                                        ├─► Melt Rate < 80% : 일반 텍스트 대화 톤 유지
                                                        │
                                                        └─► Melt Rate >= 80% : 다이내믹 프롬프트 변경 
                                                                               + 채팅방 시크릿 무드(UI) 적용
                                                        │
                                                        ▼
                                            [세션 종료 & 텐션 리포트 생성 (Async)]
                                                        │
                                                        ▼
                                            [멜트 매칭 대기열 진입]
                                                        │
                                                        ▼
                                            < 유저 간 성향 벡터 코사인 유사도 계산 >
                                                        │
                                                        └─► [유사도 최상위 유저와 블라인드 1:1 챗]

```

### B. Sequence Diagram (실시간 챗 & 다이내믹 프롬프팅)

```
User (FE)            Spring Boot (BE)           Ollama (Qwen 2.5)           H2 Database
   │                        │                           │                        │
   │─── 1. Send Message ───►│                           │                        │
   │    (STOMP WebSocket)   │                           │                        │
   │                        │─── 2. Calculate MeltRate ─┼───────────────────────►│
   │                        │    & Update Score         │                        │
   │                        │                           │                        │
   │                        │─── 3. Inject Dynamic ────►│                        │
   │                        │    Prompt (Tone Shift)    │                        │
   │                        │                           │                        │
   │                        │◄── 4. AI Stream Response ─│                        │
   │                        │                           │                        │
   │◄── 5. Broadcast Chat ──│                           │                        │
   │    & Updated MeltRate  │                           │                        │
   │                        │─── 6. Save ChatLog ───────┼───────────────────────►│

```

---

## 4. 프론트엔드 & 백엔드 핵심 구현 코드 뼈대

### 🎨 Frontend: React + TypeScript (Swipe Card Component)

```tsx
// src/components/PersonaSwipe.tsx
import React, { useState } from 'react';
import { motion, useMotionValue, useTransform } from 'framer-motion';

interface Persona {
  id: number;
  name: string;
  age: number;
  job: string;
  quote: string;
  image: string;
}

export const PersonaSwipe = ({ personas, onSelect }: { personas: Persona[]; onSelect: (p: Persona) => void }) => {
  const [index, setIndex] = useState(0);
  const x = useMotionValue(0);
  const rotate = useTransform(x, [-200, 200], [-30, 30]);

  const handleDragEnd = (_: any, info: any) => {
    if (info.offset.x > 100) {
      onSelect(personas[index]); // 오른쪽 스와이프: 대화 선택
    } else if (info.offset.x < -100) {
      setIndex((prev) => prev + 1); // 왼쪽 스와이프: 넘기기
    }
  };

  const current = personas[index];
  if (!current) return <div>더 이상 탐색할 페르소나가 없습니다.</div>;

  return (
    <div className="relative w-80 h-112 flex justify-center items-center">
      <motion.div
        style={{ x, rotate }}
        drag="x"
        dragConstraints={{ left: 0, right: 0 }}
        onDragEnd={handleDragEnd}
        className="absolute w-full h-full bg-slate-900 border border-pink-500 rounded-2xl p-6 text-white shadow-2xl flex flex-col justify-between cursor-grab active:cursor-grabbing"
      >
        <img src={current.image} alt={current.name} className="w-full h-56 object-cover rounded-xl" />
        <div className="mt-4">
          <h2 className="text-2xl font-bold">{current.name} ({current.age})</h2>
          <p className="text-sm text-pink-400">{current.job}</p>
          <p className="mt-2 text-gray-300 italic">"{current.quote}"</p>
        </div>
        <div className="text-xs text-center text-gray-500">👉 오른쪽으로 밀어서 멜팅 타임 시작</div>
      </motion.div>
    </div>
  );
};

```

---

### ⚙️ Backend: Java 21 + Spring Boot 3.5.14 (Dynamic Prompting & Melt Rate)

```java
// src/main/java/com/personasync/service/MeltingChatService.java
package com.personasync.service;

import org.springframework.stereotype.Service;

@Service
public class MeltingChatService {

    // Java 21 Virtual Threads 적용 환경
    public ChatResponse processUserMessage(Long roomId, String userMessage, int currentMeltRate) {
        
        // 1. 텐션 지수(Melt Rate) 계산
        int updatedMeltRate = calculateNewMeltRate(currentMeltRate, userMessage);
        
        // 2. Melt Rate 단계별 다이내믹 프롬프트 설정
        String dynamicTone = switch (updatedMeltRate / 30) {
            case 0 -> "격식 있고 살짝 아슬아슬하게 밀당하는 톤을 유지하세요.";
            case 1 -> "대화가 달아오르고 있습니다. 좀 더 솔직하고 능글맞게 속마음을 내비치세요.";
            default -> "Melt Rate가 최고조입니다! 극도로 은밀하고 치명적인 어조로 상대를 자극하세요.";
        };

        // 3. Ollama (Qwen 2.5 7B) 프롬프트 조합 및 추론
        String systemPrompt = """
            당신은 페르소나 '지훈(26세, 바텐더)'입니다.
            현재 대화의 텐션 지수는 %d%%입니다.
            지침: %s
            """.formatted(updatedMeltRate, dynamicTone);

        // LangChain4j 또는 Ollama Client 호출 코드로 전달...
        String aiReply = "밤이 깊어질수록 내 진짜 모습을 보여주고 싶어지는데...";

        return new ChatResponse(aiReply, updatedMeltRate);
    }

    private int calculateNewMeltRate(int currentRate, String message) {
        int bonus = message.length() > 20 ? 5 : 2; // 대화 성의 보너스
        if (message.contains("밤") || message.contains("비밀") || message.contains("좋아")) {
            bonus += 10; // 키워드 수위 보너스
        }
        return Math.min(100, currentRate + bonus);
    }

    public record ChatResponse(String aiReply, int meltRate) {}
}

```

---

### 🗄️ Database: H2 Schema (`schema.sql`)

```sql
-- 유저 테이블
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    mbti_type VARCHAR(10),
    dominance_score DOUBLE DEFAULT 0.0,
    submission_score DOUBLE DEFAULT 0.0
);

-- AI 페르소나 정의 테이블
CREATE TABLE ai_personas (
    persona_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    job VARCHAR(50),
    quote VARCHAR(255),
    system_prompt TEXT NOT NULL
);

-- 대화 세션 및 텐션 리포트 테이블
CREATE TABLE melting_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,
    final_melt_rate INT DEFAULT 0,
    tension_report_summary TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (persona_id) REFERENCES ai_personas(persona_id)
);