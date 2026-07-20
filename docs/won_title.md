# 1. 순수 Java 신경망(MLP) 라이브러리 ⭐⭐⭐⭐⭐

> TensorFlow 없이 직접 만드는 AI 라이브러리

### 구현

* Matrix 클래스
* Tensor 연산
* Dense Layer
* Activation
* Loss
* Optimizer(SGD, Adam)
* Backpropagation
* Mini Batch
* 모델 저장/불러오기

예시

```java
Sequential model = new Sequential();

model.add(new Dense(784,256));
model.add(new ReLU());
model.add(new Dense(256,10));
model.add(new Softmax());

model.fit(trainX, trainY);
```

보너스

* MNIST 숫자 인식

---

# 2. Java 기반 Git 구현체 ⭐⭐⭐⭐⭐

Git clone이 아니라

**Git을 직접 구현**

지원

```
init

add

commit

branch

merge

checkout

status

log
```

내부적으로

* SHA-1
* Blob
* Tree
* Commit Object

까지 구현

거의 운영체제 수준.

---

# 3. Java JVM(Bytecode) 인터프리터

Java 프로그램을 실행하는 작은 JVM 제작

구현

* Bytecode Parser
* Stack Frame
* Operand Stack
* Constant Pool

엄청 어렵지만

남들은 절대 안 한다.

---

# 4. Java Compiler

Java 비슷한 언어 제작

```
int a=3;

print(a+5);
```

↓

토큰화

↓

Parser

↓

AST

↓

Bytecode 생성

↓

실행

Compiler 과목 수준

---

# 5. 순수 Java DBMS

SQLite 비슷한 DB

지원

```
CREATE TABLE

INSERT

DELETE

UPDATE

SELECT

WHERE

ORDER BY

JOIN
```

직접 구현

* B+Tree
* Index
* Buffer
* Transaction

---

# 6. Java 검색엔진

Google 축소판

구현

* 크롤러
* 역색인
* TF-IDF
* PageRank
* 검색

---

# 7. Java Redis 구현

메모리 DB

지원

```
SET

GET

EXPIRE

DEL

LRANGE

HSET
```

구현

* HashMap
* LRU
* Snapshot
* Persistence

---

# 8. Java Kafka 구현

메시지 큐

Producer

↓

Broker

↓

Consumer

지원

* Topic
* Offset
* Partition

---

# 9. Java Docker 축소판

Process 관리

* Namespace 흉내
* Resource 제한
* Command 실행

OS 느낌

---

# 10. Java Neural Network + CNN

MLP가 아니라

CNN 직접 구현

* Conv
* Pooling
* Flatten
* Softmax

행렬연산 전부 직접.

---

# 11. Java 강화학습 엔진

GridWorld

Q-learning

SARSA

DQN까지 가능하면 미쳤다.

---

# 12. Java 그래프 라이브러리

NetworkX 같은 거

지원

```
DFS

BFS

Dijkstra

Bellman

A*

Prim

Kruskal

Floyd

Topological Sort
```

---

# 13. Java Ray Tracer

3D 렌더러

지원

* Reflection
* Shadow
* Light
* Camera
* Sphere
* Plane

OpenGL 안 씀

전부 수학.

---

# 14. Java 물리엔진

2D Physics

지원

* Collision
* Rigid Body
* Gravity
* Friction
* Spring

게임엔진 핵심.

---

# 15. Java Blockchain

지원

```
Wallet

Mining

Transaction

Merkle Tree

PoW

Block

Chain Validation
```

---

# 16. Java 운영체제 스케줄러 시뮬레이터

CPU Scheduling

지원

* FCFS
* SJF
* RR
* Priority
* Multilevel Queue

간트차트 출력

---

# 17. Java Virtual File System

가짜 운영체제 파일시스템

지원

```
mkdir

touch

cd

cp

mv

chmod

tree
```

내부

* inode
* directory
* block

---

# 18. Java 자동미분(AutoDiff) 라이브러리

PyTorch 핵심

```
Tensor x

↓

Operation Graph

↓

Backward()

↓

Gradient 계산
```

그 위에

신경망도 구현 가능

---

# 19. Java 분산 컴퓨팅 프레임워크

작은 Spark

Node

↓

Master

↓

Task 분배

↓

결과 수집

Socket Programming 활용

---

# 20. Java 자체 게임엔진

Unity 흉내

지원

* Scene
* Entity
* Component
* Physics
* Animation
* Rendering

Java2D만 사용

