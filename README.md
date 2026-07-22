
# 1. 프로젝트 소개 
TaxHelper는 국가법령정보센터의 법령 및 판례 데이터를 수집·구조화하고, 질문과 관련된 근거를 검색한 뒤 해당 근거를 바탕으로 답변을 생성하는 RAG 기반 세법 질의응답 서비스입니다.

이 저장소는 법령·판례 데이터 수집, 관계형 데이터 저장, 사용자 인증 및 검색 API 제공을 담당하는 Spring Boot 백엔드입니다.

----
# 2. 시스템 구성 요소
```text
사용자 질문
    ↓
FastAPI AI Service
- 질문 분석
- 검색 조건 생성
    ↓
Spring Boot Backend
- 법령·판례 검색
- 검색 근거 반환
    ↓
FastAPI AI Service
- 검색 결과 재정렬
- Ollama 기반 답변 생성
```

# 3. 주요 기능

- 국가법령정보센터 Open API를 이용한 법령 목록 및 상세 데이터 수집
- 법령·조문·항·호·목 계층형 데이터 저장
- 부칙 및 개정문 데이터 저장
- 판례 목록 및 상세 데이터 수집
- 법령 검색 API 제공
- 판례 검색을 위한 데이터 관리
- FastAPI AI Service에 검색 근거 제공
  
## 개발 예정
- 질문 및 답변 저장
- 검색 결과 평가 기능
- 서비스 배포

## 개발 상태
현재 개발 중인 프로젝트 입니다. 

# 4. 실행 방법
## 구성 요소 
- PostgreSQL
- Redis
- Spring Boot Backend
- FastAPI AI Service
- Ollama 0.30.7
- LLM Model: `qwen3:4b`(`Q4_K_M`)

> Spring Boot Backend는 단독으로 회원가입, 로그인, 법령,판례 수집 및 검색 API를 실행할 수 있습니다.
사용자 질문 분석부터 LLM답변 생성까지 전체 질의응답 기능을 실행하려면 Fast API AI Service와 Ollama가 추가로 필요합니다.

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
2. Spring Boot Backend 실행
3. Ollama 실행 및 모델 준비
4. FastAPI AI Service 실행

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

### Development Environment
- Docker
  - PostgreSQL 로컬 컨테이너 실행

# 6. ERD
> 국가법령정보센터 Open API가 제공하는 중첩형 법령 데이터를 조회 및 검색하기 쉬운 관계형 구조로 관리하기 위해 법령·조문·항·호·목을 각각의 엔티티로 분리했으며, 부칙과 개정문도 별도 구조로 저장했습니다.

# 7. API 목록 
<img width="1466" height="819" alt="스크린샷 2026-07-22 오전 10 43 56" src="https://github.com/user-attachments/assets/ace01ce0-9a54-454f-a7cc-1a0fa99afd97" />
<img width="1461" height="753" alt="스크린샷 2026-07-22 오전 10 44 16" src="https://github.com/user-attachments/assets/820dbe43-c71a-4b6c-b821-a4ada3b61732" />

# 8. 기술적 고민과 의사 결정
### 판례 목록과 상세 데이터 수집 분리
국가법령정보센터 판례 API는 목록 조회 결과만으로는 판시사항, 판결요지, 판례 본문 등의 상세 정보를 제공하지 않습니다.<br>따라서 판례 목록을 먼저 저장한 뒤 판례일련번호를 기준으로 상세 API를 다시 호출하는 방식으로 수집 과정을 분리했습니다.<br>상세 데이터가 없거나 호출에 실패한 경우를 구분할 수 있도록 저장 상태를 관리하여, 전체 수집 작업 중 누락된 데이터를 확인할 수 있도록 구성했습니다.

# 9. 트러블 슈팅 
## 최초 검색 응답 지연 개선

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

- PostgreSQL 일반 인덱스 및 `pg_trgm` GIN 인덱스 적용
- 검색 후보 수를 100개에서 30개로 축소
- FastAPI–Spring Boot 간 HTTP Client 재사용
- 독립적인 추가 검색 병렬화
- 검색 결과 TTL 캐시 적용
- FastAPI 시작 시 Ollama 모델 Warm-up
- Ollama `keep_alive=30m` 적용
- 8B 모델을 4B 양자화 모델로 변경
- 실제 스트리밍 응답 적용
- 검색, 모델 로드, 첫 토큰 및 답변 생성 시간 분리 측정

### 결과

- 전체 응답 시간: 약 **188초 → 60초**
- 약 **68% 단축**
- Spring 검색: 약 **0.57초**
- 동일 검색 캐시 응답: 약 **0.04초**
- 최초 모델 로드 비용: 약 **15~20초**로 식별

성능 개선 과정에서 검색 후보 축소로 검색 정확도가 낮아지는 사례가 확인되어, 단순 키워드 빈도 대신 법률 개념 커버리지와 핵심 개념 밀집도를 반영하도록 검색 점수도 개선했습니다.
<img width="1113" height="61" alt="image" src="https://github.com/user-attachments/assets/7da5c354-db3f-454c-a0e1-b494ec2f5c50" />

### 배열과 단일 객체가 혼재하는 API 응답 처리

#### 문제
동일한 API 필드가 데이터 개수에 따라 배열 또는 단일 객체 형태로 반환되어 역직렬화 오류가 발생했습니다.

#### 원인
응답 데이터가 여러 건일 때는 JSON 배열로 반환되지만, 한 건일 때는 단일 객체로 반환되는 비일관적인 응답 구조 때문이었습니다.

#### 해결
응답 타입을 하나의 형태로 고정해서 처리하지 않고, 배열과 단일 객체를 모두 처리할 수 있도록 변환 로직을 추가했습니다.

#### 결과
데이터 개수와 관계없이 동일한 저장 로직으로 처리할 수 있도록 API 응답 처리를 안정화했습니다.
