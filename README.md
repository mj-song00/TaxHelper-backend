
# 1. 프로젝트 소개 
TaxHelper는 국가법령정보센터의 법령 및 판례 데이터를 수집·구조화하고, 질문과 관련된 근거를 검색한 뒤 해당 근거를 바탕으로 답변을 생성하는 RAG 기반 세법 질의응답 서비스입니다.

이 저장소는 법령·판례 데이터 수집, 관계형 데이터 저장, 사용자 인증 및 검색 API 제공을 담당하는 Spring Boot 백엔드입니다.

----
# 2. 시스템 구성 요소
```text
사용자 질문
    ↓
FastAPI AI Service
- 질문 분석 및 검색 조건 생성
- 검색 결과 재정렬 및 컨텍스트 구성
    ↓
Spring Boot Backend
- 법령·판례 검색
- 작업 정보와 검색 근거 저장
    ↓
RabbitMQ
- job_id 기반 LLM 작업 대기
    ↓
FastAPI Worker
- 작업 상태를 PROCESSING으로 변경
- Ollama 기반 답변 생성
    ↓
Spring Boot Backend, PostgreSQL
- 답변과 완료 상태 저장
    ↓
사용자
- 작업 상태 및 결과 조회
```

# 3. 주요 기능

- 국가법령정보센터 Open API를 이용한 법령 목록 및 상세 데이터 수집
- 법령·조문·항·호·목 계층형 데이터 저장
- 부칙 및 개정문 데이터 저장
- 판례 목록 및 상세 데이터 수집
- 법령 검색 API 제공
- 판례 검색을 위한 데이터 관리
- FastAPI AI Service에 검색 근거 제공
- 질의응답 작업과 검색 근거 저장
- `PREPARING →  WAITING → PROCESSING → COMPLETED/FAILED`작업 상태 관리
  
## 개발 예정
- 검색결과 평가 기능
- Worker 장애 시 재처리와 중복 처리 방지 검증
- 서비스 배포 

## 개발 상태
> 핵심 기능 구현과 동시 요청 처리 구조 개선을 완료했으며, 추가 기능 개발은 보류 중입니다.

# 4. 실행 방법
## 구성 요소 
- PostgreSQL
- Redis
- RabbitMQ
- Spring Boot Backend
- FastAPI AI Service
- Ollama 0.30.7
- LLM Model: `qwen3:4b`(`Q4_K_M`)

> Spring Boot Backend는 단독으로 회원가입, 로그인, 법령,판례 수집 및 검색 API를 실행할 수 있습니다.
사용자 질문 분석부터 LLM답변 생성까지 전체 질의응답 기능을 실행하려면 FastAPI AI Service와 RabbitMQ, Worker, Ollama가 추가로 필요합니다.

## 환경 변수 
프로젝트 루트의 `.env` 파일에 다음 환경 변수를 설정합니다. 
실제 인증 정보와 비밀번호가 포함된 `.env` 파일은 Git 저장소에 커밋하지 않습니다.
```
API_URL=http://www.law.go.kr/DRF/lawSearch.do?OC=your_OC_key
DETAIL_URL=http://www.law.go.kr/DRF/lawService.do?OC=your_OC_key

POSTGRES_HOST=localhost
POSTGRES_DB=taxhelper
POSTGRES_NAME=your_postgres_username
POSTGRES_PASSWORD=your_postgres_password

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

JWT_EXPIRATION_TIME=1800
JWT_REFRESH_EXPIRATION_TIME=86400
JWT_SECRET_KEY=your_jwt_secret_key

```
## 실행 순서
1. PostgreSQL 실행
2. Redis 실행
3. RabbitMQ 실행
4. Spring Boot Backend 실행
5. Ollama 실행 및 모델 준비
6. FastAPI AI Service 실행
7. FastAPI Worker 실행

**관련 저장소**
- Spring Boot Backend: 현재 저장소
- [FastAPI AI Service](https://github.com/mj-song00/taxhelper-ai-service)

### Spring Boot 실행
./gradlew bootRun

# 5. 기술 스택
### Backend
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Web
- JWT

### Database
- PostgreSQL
- Redis
  - 블랙리스트 관리

### External API
- 국가법령정보센터 Open API

### Messaging
- RabbitMQ
    - LLM 작업 큐 : `taxhelper.llm.jobs`
    - Worker 1개, `perfetch_count=1`로 작업 순차 처리

### Development Environment
- Docker
  - PostgreSQL 로컬 컨테이너 실행

# 6. ERD
> 국가법령정보센터 Open API가 제공하는 중첩형 법령 데이터를 조회 및 검색하기 쉬운 관계형 구조로 관리하기 위해 법령·조문·항·호·목을 각각의 엔티티로 분리했으며, 부칙과 개정문도 별도 구조로 저장했습니다.

# 7. API 목록 
<img width="1466" height="819" alt="스크린샷 2026-07-22 오전 10 43 56" src="https://github.com/user-attachments/assets/ace01ce0-9a54-454f-a7cc-1a0fa99afd97" />
<img width="1461" height="753" alt="스크린샷 2026-07-22 오전 10 44 16" src="https://github.com/user-attachments/assets/820dbe43-c71a-4b6c-b821-a4ada3b61732" />

# 8. 기술적 고민과 의사 결정
### 로컬 LLM 작업의 비동기 처리
Ollama는 로컬 환경에서 여러 요청을 동시에 실행할 때 각 요청의 처리 시간이 증가했습니다.
세마포어로 동시 실행 수를 1개로 제한했지만, 후속 요청은 HTTP 연결을 유지한채 세마포어와 Ollama 처리를 모두 기다려야 했습니다.

Worker수와 `frefetch_count`를 각각 1로 유지하고 RabbitMQ가 대기 요청을 보관하도록 구성했습니다.
HTTP 요청은 검색과 작업 등록까지만 수행하고, Worker가 job_id를 받아 Ollama를 호출한 뒤 답변과 상태를 PostgreSQL에 저장합니다.
이를 통해 LLM 처리량을 무리하게 높이지 않으면서 HTTP 타임아웃과 LLM

# 9. 트러블 슈팅 
> Spring Boot 검색 API부터 FastAPI·Ollama 답변 생성까지 전체 RAG 파이프라인을 기준으로 분석했습니다.
## 1. 최초 질의 응답 지연 개선

### 문제

애플리케이션 실행 후 최초 질문의 응답 시간이 약 188초까지 소요되었습니다. 전체 처리 시간만 기록하고 있어 검색과 LLM 처리 중 어느 구간이 병목인지 구분하기 어려웠습니다.
<img width="1124" height="59" alt="image" src="https://github.com/user-attachments/assets/bfae2fb7-e736-4b88-932f-3795cfb5b5d1" />

### 원인 분석

구간별 로그를 추가해 다음 병목을 확인했습니다.

- 인덱스가 적용되지 않은 법령·청크 검색
- 과도한 검색 후보 조회 및 재정렬
- 요청마다 생성되는 HTTP Client
- 최초 요청 시 Ollama 모델을 로드하는 콜드스타트
- 약 5.46 tokens/sec의 생성 속도와 긴 컨텍스트
- 내부적으로 토큰을 모두 생성한 후 응답하는 구조

측정 결과 Spring 검색은 약 0.57초였으며, 주요 지연은 Ollama의 최초 모델 로드와 답변 생성 구간에서 발생했습니다.

### 개선

**검색 구간**
- PostgreSQL 일반 인덱스 및 `pg_trgm` GIN 인덱스 적용
- 검색 후보 수를 100개에서 30개로 축소
- 독립적인 추가 검색 병렬화 및 TTL 캐시 적용

**LLM 구간**
- FastAPI 시작 시 Ollama 워밍업 적용
- `keep_alive=30m`로 모델 상주 시간 연장
- 8B 모델을 4B 양자화 모델로 변경
- 실제 스트리밍 응답 적용

**관측 구간**
- HTTP Client 및 연결 풀 재사용
- 검색, 모델 로드, 첫 토큰, 답변 생성 시간 분리 측정

### 결과

- 전체 응답 시간: 약 **188초 → 60초**
- 응답 시간 약 **68% 단축**
- Spring 법령 검색 구간 : 약 **0.57초**
- 동일 검색 캐시 조회: 약 **0.04초**
- 최초 모델 로드 비용: 약 **15~20초**로 식별

검색 구간보다 Ollama 모델 로드와 답변 생성이 주요 병목임을 확인했습니다. 또한 구간별 로그를 통해 이후 성능 저하 발생 시 검색, 모델 로드, 프롬프트 처리 및 토큰 생성 구간을 구분할 수 있게 되었습니다.

성능 개선 과정에서 검색 후보 축소에 따른 정확도 저하 사례가 확인되어, 키워드 출현 빈도뿐 아니라 법률 개념 커버리지와 핵심 개념 밀집도를 검색 점수에 반영했습니다.

<img width="1113" height="61" alt="image" src="https://github.com/user-attachments/assets/7da5c354-db3f-454c-a0e1-b494ec2f5c50" />
> 동일한 로컬 개발 환경에서 측정했으며, 질문과 서버·모델 상태에 따라 응답 시간은 달라질 수 있습니다.

## 2. 동시 요청 타임아웃을 비동기 작업 구조로 개선

#### 문제
로컬 Ollama가 한 번에 여러 요청을 처리할 때 각 요청의 생성 시간이 증가했습니다.
이를 방지하기 위해 세마포어로 LLM 동시 실행 수를 1개로 제한했지만, 후속 요청은 HTTP 연결을 유지한 채 앞선 작업이 끝날 때까지 기다려야 했습니다.

2VU 테스트에서 푸속 요청은 세마포어 대기사간 약 125.7초와 Ollama 처리 약 125.7초가 더해져 LLM 구간에 약 251.4초가 소요되었습니다.
이는 Spring Boot가 FastAPI 응답을 기다리는 180초 read timeout을 초과하여, k6의 요청 제한 시간을 6분으로 늘려도 Spring 구간에서 요청이 실패하였습니다.

#### 원인 
세마포어는 Ollama에 동시 진업하는 요청 수만 제한할 뿐, HTTP요청과 LLM 작업을 분리하지 않습니다.
따라서 동시 요청이 증가하면 대기시간까지 HTTP 연결이 유지되고, 앞선 요청의 처리 시간이 후속 요청의 타임아웃으로 이어졌습니다. 

#### 개선
- HTTP요청은 질문 분석, 검색, 컨텍스트 구성 및 작업 등록까지만 수행하도록 변경
- 생성된 `job_id`를 RabbitMQ의 `taxhelper.llm.jobs`큐에 발행
- 별도 Worker가 작업을 가져와 기존 LLM 서비스와 Ollama 호출
- 작업 상태를 `PREPARING → WAITING → PROCESSING → COMPLETED/FAILED`로 관리
- 답변 처리 시간을 PostgreSQL에 저장한 뒤 RabbitMQ 메시지를 수동 ack
- 로컬 Ollama의 처리 특성을 고려해 Worker 1개, `prefetch_count=1`, 세마포어 1 유지

#### 결과
2VU 테스트에서 작업 등록 요청과 실제 LLM 처리가 분리되었습니다.

- 작업 등록시간: 평균 1.105초, p95 1.562초
- 작업 완료율: 100%
- 작업 실패율: 0%
- 큐 대기시간을 포함한 전체 처리시간 최댓값: 311.171초
- Worker가 처리한 두 작업의 Ollama 소요시간 : 각 156초, 153초

전체 LLM 처리시간이 단축된 것은 아니지만, 180초를 넘는 작업도 HTTP 타임아웃 없이 큐에서 대기한 뒤 완료되었습니다.
두 작업 모두 PostgreSQL에 `COMPLETE` 상태와 답변이 저장되고 ack된것을 확인하였습니다.

현재 로컬 환경에서는 Worker를 1개로 운영하므로 요청 수가 증가하면 큐 대기시간도 증가합니다. 
RabbitMQ 재시도 큐, DLQ, Worker 장애 시 재전달 및 중복 처리 방지는 후속 개선 항목으로 남겨두었습니다.

> 동일한 로컬 개발 환경에서 2VU로 측정했으며, 질문과 서버·모델 상태에 따라 처리시간은 달라질 수 있습니다.

## 3. 배열과 단일 객체가 혼재하는 API 응답 처리

#### 문제
동일한 API 필드가 데이터 개수에 따라 배열 또는 단일 객체 형태로 반환되어 역직렬화 오류가 발생했습니다.
<img width="2048" height="130" alt="image" src="https://github.com/user-attachments/assets/18a50013-3cef-4d19-95d1-9dfb452e0889" />

#### 원인
응답 데이터가 여러 건일 때는 JSON 배열로 반환되지만, 한 건일 때는 단일 객체로 반환되는 비일관적인 응답 구조 때문이었습니다.
<img width="853" height="327" alt="image" src="https://github.com/user-attachments/assets/88923b9c-8f12-41c9-87cc-96479599f30a" />
<img width="885" height="308" alt="image" src="https://github.com/user-attachments/assets/c49c956b-a0cd-4e0d-af51-4ea62343d8f5" />

#### 해결
응답 타입을 하나의 형태로 고정해서 처리하지 않고, 배열과 단일 객체를 모두 처리할 수 있도록 변환 로직을 추가했습니다.
<img width="645" height="465" alt="image" src="https://github.com/user-attachments/assets/83947e08-78f2-44a2-a6ea-87fa1674a8c9" />

#### 결과
데이터 개수와 관계없이 동일한 저장 로직으로 처리할 수 있도록 API 응답 처리를 안정화했습니다.
