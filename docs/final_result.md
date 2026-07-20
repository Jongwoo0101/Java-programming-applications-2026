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

* **기능 설명:** 데이트 앱(틴더, 글램)처럼 매력적인 남녀 페르소나 프로필 카드를 좌우로 스와이프하며 대화 상대를 선택.
* **주요 캐릭터 라인업:**
* **지훈 (26, 바텐더):** *"낮보다는 밤이 더 긴 편이에요. 오늘 밤 비밀 얘기 할래요?"* (치명적, 능글맞음)
* **서연 (24, 대학 선배):** *"너 왜 자꾸 나한테 선 넘으려고 해? ...싫다는 건 아니고."* (츤데레, 밀당)
* **민우 (29, 직장 상사):** *"회사 밖에서는 과장 말고 오빠라고 부르라니까."* (어른스러운, 아슬아슬함)
* **동현 (20, 대학새내기):** *"저 애 아니거든요!"* (장난스러움, 똘끼)



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