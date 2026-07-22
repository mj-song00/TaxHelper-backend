
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
- LLM Model: `qwen3:8b`

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


# 9. 트러블 슈팅 

