CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    deleted_at TIMESTAMP(6),
    email      VARCHAR(255) NOT NULL,
    nick_name  VARCHAR(255) NOT NULL,
    password   VARCHAR(255),
    user_role  VARCHAR(255) NOT NULL CHECK (user_role IN ('USER', 'ADMIN'))
);

CREATE TABLE laws--법령(기본정보 포함)
(
    law_id SERIAL PRIMARY KEY, -- 법령 key
    name_kor        VARCHAR(255) NOT NULL,     -- 법령명_한글
    name_hanja      VARCHAR(255),              -- 법령명_한자
    name_short      VARCHAR(255),              -- 법령명약칭
    decision_type   VARCHAR(50),               -- 의결구분
    proposal_type   VARCHAR(50),               -- 제안구분
    proclamation_no VARCHAR(50),               -- 공포번호
    proclamation_date DATE,                    -- 공포일자 (YYYYMMDD → DATE 변환)
    enforcement_date DATE,                     -- 시행일자 (YYYYMMDD → DATE 변환)
    phone_number    VARCHAR(50),               -- 전화번호
    language        VARCHAR(50),               -- 언어 (한글/영문 등)
    revision_type   VARCHAR(50),               -- 제개정구분
    joint_ministry  VARCHAR(255),              -- 공동부령정보
    is_proclaimed   BOOLEAN,                   -- 공포법령여부 (Y/N → boolean)
    is_korean       BOOLEAN,                   -- 한글법령여부 (Y/N → boolean)
    is_title_changed BOOLEAN,                  -- 제명변경여부 (Y/N → boolean)
    has_annex       BOOLEAN,                   -- 별표편집여부 (Y/N → boolean)
    structure_code  VARCHAR(50),               -- 편장절관 (법령 구조 코드)
)


CREATE TABLE articles -- 조문
(
    article_id       VARCHAR(20) PRIMARY KEY,   -- 조문키
    law_id           VARCHAR(20) NOT NULL,     -- 법령ID (FK)
    article_no       VARCHAR(20),              -- 조문번호
    title            VARCHAR(255),             -- 조문제목
    content          TEXT,                     -- 조문내용
    enforcement_date DATE,                     -- 조문시행일자
    is_changed       BOOLEAN,                  -- 조문변경여부
    move_before      VARCHAR(20),              -- 조문이동이전
    move_after       VARCHAR(20),              -- 조문이동이후
    reference        TEXT,                     -- 조문참고자료
    article_type     VARCHAR(50),              -- 조문여부
    branch_no        VARCHAR(20),              -- 조문가지번호

)

CREATE TABLE supplements -- 부칙
(
    supplement_id     VARCHAR(20) PRIMARY KEY,   -- 부칙키
    law_id            VARCHAR(20) NOT NULL,     -- 법령ID (FK: laws)
    proclamation_no   VARCHAR(20),              -- 부칙공포번호
    proclamation_date DATE,                     -- 부칙공포일자
    content           TEXT[]                    -- 부칙내용 (배열로 저장, 각 항목 한 줄)
)

CREATE TABLE amendments -- 개정문
(
    amendment_id   SERIAL PRIMARY KEY,       -- 내부 PK
    law_id         VARCHAR(20) NOT NULL,    -- 법령ID (FK: laws)
    title          VARCHAR(255),            -- 개정문 제목 (예: 대통령령 제23987호 등)
    content        TEXT[] ,                  -- 개정문 내용 (배열로 저장, 각 줄을 요소로)
)

CREATE TABLE ministries(  -- 소관 부처
    ministry_id     VARCHAR(20) PRIMARY KEY ,     -- 소관부처코드
    law_type_code   VARCHAR(20) NOT NULL,     -- 법종구분코드
)

CREATE TABLE departmentalUnit --부서단위
(
    dept_id         VARCHAR(20) PRIMARY KEY,               -- 부서키
    dept_name       VARCHAR(100) NOT NULL, ,              -- 부서명
    dept_phone      VARCHAR(50),               -- 부서연락처
    ministry_id VARCHAR(20) NOT NULL,       -- 소관부처 FK
)