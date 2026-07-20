## 1. 순수 Java 신경망(MLP) 라이브러리 ⭐⭐⭐⭐⭐

> TensorFlow 없이 직접 만드는 AI 라이브러리

**[구현]**

* Matrix 클래스
* Tensor 연산
* Dense Layer
* Activation
* Loss
* Optimizer(SGD, Adam)
* Backpropagation
* Mini Batch
* 모델 저장/불러오기

**[예시]**

```java
Sequential model = new Sequential();
model.add(new Dense(784,256));
model.add(new ReLU());
model.add(new Dense(256,10));
model.add(new Softmax());
model.fit(trainX, trainY);

```

**[보너스]**

* MNIST 숫자 인식

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Learning representations by back-propagating errors* (Rumelhart, Hinton, & Williams, 1986)
* **활용 포인트:** 오차역전파(Backpropagation)의 개념을 정립한 딥러닝의 성서입니다. 편미분 수식을 자바의 `Matrix` 클래스와 멀티스레딩으로 직접 구현하기 위한 수학적 베이스를 제공합니다.

---

## 2. Java 기반 Git 구현체 ⭐⭐⭐⭐⭐

> Git clone이 아니라 **Git을 직접 구현**

**[지원]**
`init`, `add`, `commit`, `branch`, `merge`, `checkout`, `status`, `log`

**[내부 구현]**

* SHA-1
* Blob
* Tree
* Commit Object
*(거의 운영체제 수준)*

**[📄 관련 논문 및 연구]**

* **근본 논문:** *A Digital Signature Based on a Conventional Encryption Function* (Ralph Merkle, 1987)
* **활용 포인트:** Git의 핵심 구조인 '머클 트리(Merkle Tree)'를 제안한 논문입니다. 커밋 히스토리와 무결성을 보장하는 암호학적 트리 구조를 자바 객체로 어떻게 구성할지 영감을 줍니다.

---

## 3. Java JVM(Bytecode) 인터프리터

> Java 프로그램을 실행하는 작은 JVM 제작

**[구현]**

* Bytecode Parser
* Stack Frame
* Operand Stack
* Constant Pool
*(엄청 어렵지만 남들은 절대 안 한다.)*

**[📄 관련 논문 및 연구]**

* **표준 문서:** *The Java Virtual Machine Specification* (Tim Lindholm & Frank Yellin)
* **활용 포인트:** 논문이라기보단 스펙 문서에 가깝지만, JVM을 만들기 위해 반드시 거쳐야 하는 문서입니다. 자바 바이트코드 옵코드(Opcode) 명세와 스택 프레임 구조가 완벽히 설명되어 있습니다.

---

## 4. Java Compiler

> Java 비슷한 언어 제작. Compiler 과목 수준.

**[코드 흐름]**

```java
int a=3;
print(a+5);

```

↓ 토큰화
↓ Parser
↓ AST
↓ Bytecode 생성
↓ 실행

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Practical Translators for LR(k) languages* (Frank DeRemer, 1969)
* **활용 포인트:** LALR 파싱 알고리즘을 정립한 논문입니다. AST(추상 구문 트리)를 구성하고 소스 코드를 토큰화하여 바이트코드로 변환하는 컴파일러 설계의 기초가 됩니다.

---

## 5. 순수 Java DBMS

> SQLite 비슷한 DB

**[지원]**
`CREATE TABLE`, `INSERT`, `DELETE`, `UPDATE`, `SELECT`, `WHERE`, `ORDER BY`, `JOIN`

**[직접 구현]**

* B+Tree
* Index
* Buffer
* Transaction

**[📄 관련 논문 및 연구]**

* **근본 논문:** *A Relational Model of Data for Large Shared Data Banks* (E. F. Codd, 1970) / *The Ubiquitous B-Tree* (Douglas Comer, 1979)
* **활용 포인트:** RDBMS의 수학적 집합론 배경(Codd)과 디스크 I/O를 최소화하기 위한 B-Tree 인덱스 구조(Comer)를 다룹니다.

---

## 6. Java 검색엔진

> Google 축소판

**[구현]**

* 크롤러
* 역색인
* TF-IDF
* PageRank
* 검색

**[📄 관련 논문 및 연구]**

* **근본 논문:** *The Anatomy of a Large-Scale Hypertextual Web Search Engine* (Sergey Brin & Lawrence Page, 1998)
* **활용 포인트:** 구글의 창업자인 래리 페이지와 세르게이 브린이 쓴 전설의 논문. 역색인(Inverted Index) 구조와 PageRank 알고리즘을 자바의 `HashMap`과 큐로 구현하기 위한 지침서입니다.

---

## 7. Java Redis 구현

> 메모리 DB

**[지원]**
`SET`, `GET`, `EXPIRE`, `DEL`, `LRANGE`, `HSET`

**[구현]**

* HashMap
* LRU
* Snapshot
* Persistence

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Dynamo: Amazon's Highly Available Key-value Store* (DeCandia et al., 2007)
* **활용 포인트:** 인메모리 분산 키-밸류 스토어의 아키텍처를 다룹니다. 메모리 데이터가 휘발되지 않도록 스냅샷(Snapshot)을 남기고 해시 테이블을 관리하는 설계의 근본입니다.

---

## 8. Java Kafka 구현

> 메시지 큐

**[흐름]**
Producer ↓ Broker ↓ Consumer

**[지원]**

* Topic
* Offset
* Partition

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Kafka: a Distributed Messaging System for Log Processing* (Jay Kreps et al., 2011)
* **활용 포인트:** 링크드인에서 카프카를 처음 개발하고 발표한 논문입니다. 디스크 기반의 순차적(Sequential) 쓰기 구조가 어떻게 메모리 큐보다 빠른 성능을 내는지 아키텍처를 배울 수 있습니다.

---

## 9. Java Docker 축소판

> Process 관리 (OS 느낌)

**[구현]**

* Namespace 흉내
* Resource 제한
* Command 실행

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Docker: lightweight Linux containers for consistent development and deployment* (Dirk Merkel, 2014)
* **활용 포인트:** OS 레벨의 가상화(cgroups, namespaces)를 자바의 `ProcessBuilder`와 런타임 환경 통제로 흉내 내기 위한 컨테이너 격리 개념을 잡을 수 있습니다.

---

## 10. Java Neural Network + CNN

> MLP가 아니라 CNN 직접 구현 (행렬연산 전부 직접)

**[구현]**

* Conv
* Pooling
* Flatten
* Softmax

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Gradient-Based Learning Applied to Document Recognition* (Yann LeCun et al., 1998)
* **활용 포인트:** MNIST 인식을 위한 LeNet-5 구조를 제안한 논문입니다. 다층 신경망(Multi-layer)을 넘어 이미지의 공간적 특성을 뽑아내는 컨볼루션(Convolution) 연산을 자바 배열로 구현할 때 필수적입니다.

---

## 11. Java 강화학습 엔진

> GridWorld 구현

**[구현]**

* Q-learning
* SARSA
* DQN까지 가능하면 미쳤다.

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Q-learning* (Christopher J. C. H. Watkins & Peter Dayan, 1992)
* **활용 포인트:** 모델 프리(Model-free) 강화학습의 뼈대입니다. 에이전트가 상태(State)와 보상(Reward)을 바탕으로 행동(Action)을 선택하는 Q-Table을 자바 2차원 배열로 구축할 수 있습니다.

---

## 12. Java 그래프 라이브러리

> NetworkX 같은 거

**[지원]**
`DFS`, `BFS`, `Dijkstra`, `Bellman`, `A*`, `Prim`, `Kruskal`, `Floyd`, `Topological Sort`

**[📄 관련 논문 및 연구]**

* **근본 논문:** *A note on two problems in connexion with graphs* (E. W. Dijkstra, 1959)
* **활용 포인트:** 다익스트라 최단 경로 알고리즘의 원본입니다. 인접 행렬과 인접 리스트의 메모리 효율성을 고려하여 대규모 노드 네트워크를 순수 자바로 최적화하는 데 도움을 줍니다.

---

## 13. Java Ray Tracer

> 3D 렌더러 (OpenGL 안 씀, 전부 수학)

**[지원]**

* Reflection
* Shadow
* Light
* Camera
* Sphere
* Plane

**[📄 관련 논문 및 연구]**

* **근본 논문:** *An improved illumination model for shaded display* (Turner Whitted, 1980)
* **활용 포인트:** 재귀적 레이트레이싱(Recursive Ray Tracing)을 제안한 논문. 빛의 반사, 굴절, 그림자 계산을 순수 3D 벡터 수학(내적, 외적)으로 풀어내는 광학 시뮬레이션의 근본입니다.

---

## 14. Java 물리엔진

> 2D Physics (게임엔진 핵심)

**[지원]**

* Collision
* Rigid Body
* Gravity
* Friction
* Spring

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Analytical Methods for Dynamic Simulation of Non-penetrating Rigid Bodies* (David Baraff, 1989)
* **활용 포인트:** 게임 엔진의 핵심인 강체(Rigid Body) 충돌과 힘의 분산을 다룹니다. 자바 클래스로 객체의 질량, 속도, 가속도 벡터를 설계하는 데 완벽한 수학적 배경을 제공합니다.

---

## 15. Java Blockchain

**[지원]**
`Wallet`, `Mining`, `Transaction`, `Merkle Tree`, `PoW`, `Block`, `Chain Validation`

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Bitcoin: A Peer-to-Peer Electronic Cash System* (Satoshi Nakamoto, 2008)
* **활용 포인트:** 작업증명(PoW)과 P2P 네트워크를 통한 합의 알고리즘의 바이블입니다. 자바의 `MessageDigest`를 활용한 SHA-256 해싱과 블록(객체) 체이닝 구조를 그대로 구현할 수 있습니다.

---

## 16. Java 운영체제 스케줄러 시뮬레이터

> CPU Scheduling (간트차트 출력)

**[지원]**

* FCFS
* SJF
* RR
* Priority
* Multilevel Queue

**[📄 관련 논문 및 연구]**

* **근본 논문:** *A Scheduling Model for Computer Systems* (Kleinrock, 1970년대 초반 큐잉 이론 관련) 또는 일반적인 OS 공룡책(Silberschatz)
* **활용 포인트:** 라운드 로빈(RR)이나 다단계 큐 알고리즘을 자바의 멀티스레딩과 우선순위 큐(PriorityQueue)로 구현하여 간트 차트(Gantt Chart)를 뽑아내는 시뮬레이션 로직에 활용됩니다.

---

## 17. Java Virtual File System

> 가짜 운영체제 파일시스템

**[지원]**
`mkdir`, `touch`, `cd`, `cp`, `mv`, `chmod`, `tree`

**[내부]**

* inode
* directory
* block

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Vnodes: An Architecture for Multiple File System Types in Sun UNIX* (S. R. Kleiman, 1986)
* **활용 포인트:** 아이노드(inode)와 블록 포인터 메커니즘의 근본입니다. 자바의 `RandomAccessFile`을 통짜 디스크로 가정하고, 그 안에서 바이너리로 파티션과 디렉토리 트리를 직접 나누는 아키텍처를 제공합니다.

---

## 18. Java 자동미분(AutoDiff) 라이브러리

> PyTorch 핵심 (그 위에 신경망도 구현 가능)

**[흐름]**
Tensor x ↓ Operation Graph ↓ Backward() ↓ Gradient 계산

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Automatic Differentiation in Machine Learning: a Survey* (Baydin et al., 2017)
* **활용 포인트:** 현대 딥러닝 프레임워크(PyTorch 등)의 코어인 'Computational Graph'와 후진 자동미분(Reverse-mode AutoDiff)을 순수 자바 객체의 트리 구조로 구현하기 위한 가장 완벽한 리뷰 논문입니다.

---

## 19. Java 분산 컴퓨팅 프레임워크

> 작은 Spark (Socket Programming 활용)

**[흐름]**
Node ↓ Master ↓ Task 분배 ↓ 결과 수집

**[📄 관련 논문 및 연구]**

* **근본 논문:** *Resilient Distributed Datasets: A Fault-Tolerant Abstraction for In-Memory Cluster Computing* (Matei Zaharia et al., 2012)
* **활용 포인트:** Apache Spark의 핵심 구조인 RDD를 제안한 논문입니다. 자바 소켓(Socket)을 이용해 여러 노드(JVM)로 워커를 띄우고, 맵리듀스(MapReduce) 작업을 분산 처리하는 네트워크 프로그래밍의 끝판왕 설계도입니다.

---

## 20. Java 자체 게임엔진

> Unity 흉내 (Java2D만 사용)

**[지원]**

* Scene
* Entity
* Component
* Physics
* Animation
* Rendering

**[📄 관련 논문 및 연구]**

* **근본 논문:** *A Data-Driven Game Object System* (Scott Bilas, 2002 GDC 발표)
* **활용 포인트:** Unity 엔진의 핵심인 컴포넌트 기반 아키텍처(ECS: Entity Component System)를 다루는 기념비적 발표 자료입니다. 자바 상속의 한계를 극복하고, 객체(Entity)에 기능(Component)을 조립하는 혁신적인 클래스 설계를 배울 수 있습니다.

#### 21. Java 기반 검색엔진 및 텍스트 파싱 분석 시스템 ⭐⭐⭐⭐⭐

> 교수님의 '국어 파괴 현상 번역 기술' 연구를 오마주한 역색인 기반 로컬 검색엔진

**[구현]**

* Crawler (웹 문서/텍스트 수집)
* Tokenizer / Parser (기호 분리 및 형태소 단위 파싱)
* Stopword (불용어 제거)
* Inverted Index (역색인) 저장
* TF-IDF 알고리즘
* REST API 기반 검색 인터페이스

**[예시]**

```java
SearchEngine engine = new SearchEngine();
// 교수님 연구를 오마주한 형태소 분석 및 토큰화 로직
engine.addDocument("뜨아.. 여기 모이삼 저 영화 좀 흠좀무인듯 ㅋㅋ"); 
engine.buildInvertedIndex();

List<SearchResult> results = engine.search("영화 흠좀무");
for (SearchResult result : results) {
    System.out.println("문서 ID: " + result.getDocId() + ", 스코어: " + result.getScore());
}
```

**[보너스]**

* PageRank를 응용한 문서 중요도 랭킹 산출

**[📄 관련 논문 및 연구]**

* **근본 논문:** *인터넷 매체 언어의 국어 파괴 현상의 고찰을 통한 표준어 자동 번역 기술에 대한 연구* (박장혁, 정재훈 등, 2016), *The Anatomy of a Large-Scale Hypertextual Web Search Engine* (Brin & Page, 1998)
* **활용 포인트:** 교수님이 과거 Java를 사용하여 문장을 이모티콘/기호 등으로 분리하고 형태소 태깅(Parsing)을 거쳐 REST API 형태로 서비스한 연구 로직을 검색엔진의 Tokenizer/Parser 설계에 완벽하게 오마주할 수 있습니다. '문장 입력 -> 토큰화 -> 불용어 제거 -> 역색인 저장 -> TF-IDF 계산'으로 이어지는 데이터 파이프라인을 **Activity 다이어그램**으로, Crawler, Parser, Indexer 등의 객체 역할 분담을 **Class 다이어그램**으로 명확히 도출하기 좋습니다.

--------------------------------------------------------------------------------

#### 22. Java 분산 컴퓨팅 프레임워크 기반 노드 모니터링 시스템 ⭐⭐⭐⭐⭐

> 교수님의 네트워크/통신 시스템 전공을 저격하는 소켓 통신 기반 클라이언트-서버 아키텍처

**[구현]**

* Socket Programming (TCP/IP)
* Master Node (서버, Task 분배 및 상태 관리)
* Worker Node (클라이언트/JVM, 데이터 수집 및 작업 수행)
* MapReduce (작업 분산 및 결과 수집)
* Thread Pool 관리
* 분산 노드 상태 동기화

**[예시]**

```java
MasterNode master = new MasterNode(8080);
master.start();

WorkerNode worker1 = new WorkerNode("127.0.0.1", 8080);
worker1.connect(); // Wi-Fi 및 소켓 통신을 이용한 노드 연결

Task task = new MapReduceTask(data);
master.dispatchTask(worker1, task); // Task 분배
```

**[보너스]**

* Worker 노드의 상태 데이터 실시간 수집 및 모니터링 대시보드

**[📄 관련 논문 및 연구]**

* **근본 논문:** *차량네트워크와 Wi-Fi통신을 이용한 안드로이드 차량관리 시스템 구현* (정재훈 등, 2013), ITIL 기반 클라우드-클라이언트 시스템 구조도
* **활용 포인트:** '정보통신시스템'을 전공하시고, Wi-Fi 통신 시스템 및 Cloud-Client 기반 업무 시스템 등 클라이언트-서버 구조 설계에 능통하신 교수님의 관심사를 완벽히 반영합니다. Master 노드와 Worker 노드 간의 네트워크 토폴로지를 화려한 **시스템 구조도(System Architecture)**로 뽑아낼 수 있으며, Master가 Task를 분배하고 Worker가 결과를 수집하여 반환하는 과정을 **Sequence 다이어그램**으로 명확히 증명할 수 있습니다.