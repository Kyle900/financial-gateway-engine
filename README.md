# 🏦 Financial Gateway Engine (금융 대외계 & 펌뱅킹 FEP 파이프라인 통합 엔진)

> **대규모 금융 대외계 트랜잭션 수용 및 FEP(Front-End Processor) EBCDIC/ASCII 전문 마이그레이션을 아우르는 고신뢰성 금융 백엔드 통합 엔진**

## 📌 1. 프로젝트 개요 (Executive Summary)

본 프로젝트는 제1금융권 현장에서 직접 검증한 두 핵심 금융 시스템 아키텍처를 하나로 집약한 **엔터프라이즈급 금융 게이트웨이 토이 프로젝트**입니다.

1. **대외계 실시간 데몬/스케줄러 엔진** (KB국민은행 행안부 차세대 지방행정 중계 레퍼런스)

   * 분산 Daemon 프로세스(`daemon01`, `daemon02`)와 Spring Batch 스케줄링 구조를 현대화.

   * 금융 보안 규격(대칭키 암·복호화 모듈, SCP DB 암호화 호환)을 적용한 인바운드/아웃바운드 RESTful 연계.

2. **FEP(Front-End Processor) 펌뱅킹 마이그레이션 엔진** (SC제일은행 FEP 고도화 레퍼런스)

   * 레거시 C/IBM DB2 메인프레임 환경에서 운영되던 EBCDIC 바이너리 전문 처리를 **Java/Spring 기반의 유연한 Byte 파서 구조**로 전환.

   * VAN사 수발신 File Batch와 금융결제원/계정계(HOST) 온라인 이체 파이프라인(전처리 → 본처리 → 후처리 → 일일정산) 구현.

## 🏛️ 2. 시스템 아키텍처 (Architecture & Flow)

```
[ VAN사 / 외부기관 ] 
        │ (File-to-File Batch / REST API)
        ▼
┌─────────────────────────────────────────────────────────────┐
│                 Financial Gateway Engine                    │
│                                                             │
│  [1] FEP Pipeline Area                                      │
│      ┌───────────────┐     ┌───────────────┐               │
│      │ Byte Packet   │ ──> │   Pre-Process │               │
│      │ Converter     │     │ (EBCDIC->EUC-KR)              │
│      │ (EBCDIC/ASCII)│     └───────┬───────┘               │
│      └───────────────┘             │ (Insert Staging)       │
│                                    ▼                        │
│                           ┌─────────────────┐               │
│                           │   Main-Process  │               │
│                           │ (Core Transfer) │               │
│                           └────────┬────────┘               │
│                                    │                        │
│                   ┌────────────────┴──────────────┐         │
│                   ▼                               ▼         │
│           [당행이체 처리]                  [타행이체 처리]    │
│           (ASCII -> EBCDIC)               (ASCII -> ASCII)  │
│                   │                               │         │
│  [2] External Daemon Area                         │         │
│      ┌─────────────────────────┐                  │         │
│      │ Daemon API Controller   │                  │         │
│      │ (AES/Cipher Crypto SPI) │                  │         │
│      └────────────┬────────────┘                  │         │
│                   ▼                               │         │
│  [3] Settlement Batch Area                        │         │
│      ┌─────────────────────────┐                  │         │
│      │ Spring Batch Chunk/Task │                  │         │
│      │ (Dual DataSource: Meta/Biz)                │         │
│      └─────────────────────────┘                  │         │
└───────────────────┬───────────────────────────────┴─────────┘
                    │ (NDM / Socket / TCP / DB)
        ┌───────────┴───────────┐
        ▼                       ▼
  [ IBM Host (호스트) ]   [ 금융결제원 (KFTC) ]

```

## 🛠️ 3. 기술 스택 및 환경 (Tech Stack)

| **구분** | **기술 스택** | **설명** | 
| **Language & Framework** | Java 17, Spring Boot 3.x, Spring Batch 5.x | 엔터프라이즈 멀티 스레드 및 트랜잭션 제어 | 
| **Data Persistence** | MyBatis 3.5.x, HikariCP, PostgreSQL / Oracle 호환 | 원장 트랜잭션 분리 및 대용량 배치 최적화 | 
| **Data Isolation** | **Dual DataSource** (`BizDataSource`, `BatchMetaDataSource`) | 금융 표준 배치 메타데이터와 비즈니스 데이터 격리 | 
| **Network & Protocol** | Custom Byte Engine, EBCDIC-ASCII Codec, RESTful API | 메인프레임 통신 규격 및 REST 통신 지원 | 
| **Security** | AES-256 Crypto Engine | DB 패스워드 및 전송 민감정보(계좌, 키값) 암·복호화 | 
| **Build & Test** | Maven, JUnit 5, H2 In-Memory TCP Test Engine | 단위/통합 대용량 대사 테스트 자동화 | 

## 💡 4. 핵심 구현 및 기술적 해결 과제 (Key Highlights)

### ① 레거시 EBCDIC 바이너리와 가변 ASCII 패킷 디코딩 엔진 (`BytePacketConverter`)

* **문제점:** 레거시 메인프레임(IBM) 및 VAN사 전문 송수신 시 한글 EUC-KR 및 EBCDIC(CP933) 간 인코딩 불일치로 인한 데이터 왜곡 발생 위험.

* **해결책:** 고정길이 전문(Fixed-length byte stream)을 바이트 오프셋 기준으로 안전하게 슬라이싱하고 인코딩 테이블 변환을 지원하는 `BytePacketConverter` 구축. 네트워크 바이트 패딩(Space/Zero) 처리 로직 내재화.

### ② 안전한 대량 금융 정산(Settlement)을 위한 Dual DataSource 격리

* **설계 구조:** `DualDataSourceConfig`를 구현하여 Spring Batch 실행 이력을 관리하는 `batchMeta` 영역과 정산 대사 원장 DB인 `primary` 영역의 Hikari Connection Pool을 물리적으로 분리.

* **효과:** 일일 대용량 정산 작업 중 메타데이터 Lock으로 인한 업무 원장 지연(Deadlock) 가능성을 원천 차단.

### ③ 실시간 온라인 이체 분기 및 예외 롤백 파이프라인

* 타행 이체(금융결제원)와 당행 이체(Host 코어뱅킹)의 전문 규격(EBCDIC vs ASCII) 분기 처리 및 실시간 ACK 수신 검증.

* 단계별(전처리 `Pre-Process` → 본처리 `Main-Process` → 후처리 `Post-Process`) 상태 전이 트랜잭션을 엄격히 분리하여 실패 시 원인 추적 및 보상 트랜잭션 토대 마련.

### ④ 금융 보안 가이드라인을 준수한 Crypto 서비스

* 대외계 연계용 API 키 및 DB 패스워드에 대해 단방향/양방향 암호화를 모듈화(`CryptoService`). 운영/스테이징/개발 프로파일별 동적 키 인젝션 지원.

## 📂 5. 프로젝트 패키지 구조 (Package Structure)

```
com.financial.gateway
├── FinancialGatewayApplication.java       # 구동 메인 클래스
├── batch
│   ├── config/SettlementBatchConfig.java   # 정산 배치 잡/스텝 설정
│   ├── listener/BatchJobExecutionListener.java # 실행 전후 로그 및 상태 검증
│   └── tasklet/SettlementTasklet.java      # 일일 대사 및 파일 생성 Tasklet
├── daemon
│   ├── controller/ExternalDaemonController.java # 대외계 인바운드 REST 인터페이스
│   ├── dto/{DaemonRequestDto, DaemonResponseDto}.java
│   └── service/DaemonTransactionService.java    # 데몬 비즈니스 트랜잭션 서비스
├── fep
│   ├── controller/FepPipelineTestController.java # FEP 전문 파이프라인 트리거 엔드포인트
│   ├── dto/FepPacketDto.java                     # 공통 전문 구조 DTO
│   ├── parser/BytePacketConverter.java           # EBCDIC <-> ASCII <-> EUC-KR 변환기
│   └── service/
│       ├── FepFirmbankingPipelineService.java    # 전처리/본처리/후처리 오케스트레이션
│       └── HostTransferService.java              # 당행/타행 온라인 인터페이스 송수신
├── global
│   ├── config/DualDataSourceConfig.java          # 2차 DB 풀(Batch Meta vs Biz) 분리
│   ├── config/H2TcpServerConfig.java             # 로컬 테스트용 TCP DB Server
│   ├── crypto/CryptoService.java                 # 금융 데이터 암복호화 엔진
│   └── scheduler/GatewayScheduler.java           # 기관 대사 및 모니터링 스케줄러
└── mapper
    └── SettlementMapper.java                     # MyBatis 대사 쿼리 매퍼 인터페이스

```

## 🚀 6. 빌드 및 테스트 실행 방법 (Getting Started)

### 사전 요구 사항

* Java 17 이상

* Maven 3.8+

### 1) 프로젝트 클론 및 빌드

```
git clone https://github.com/your-username/financial-gateway-engine.git
cd financial-gateway-engine
mvn clean package -DskipTests=false

```

### 2) 통합 테스트 실행

대용량 정산 배치 및 FEP 변환 파이프라인의 통합 테스트를 포함하고 있습니다:

```
mvn test
# 실행 대상:
# - SettlementBatchIntegrationTest (단일 정산 배치 무결성 검증)
# - SettlementBulkBatchIntegrationTest (대용량 대사 처리 및 시간 측정)

```

### 3) 실행 (Daemon & Batch 프로파일)

```
# REST Daemon 모드 실행
java -jar target/financial-gateway-engine.jar --spring.profiles.active=daemon

# Settlement Batch 모드 실행
java -jar target/financial-gateway-engine.jar --spring.profiles.active=batch

```
