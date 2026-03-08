# Bamdul Reminder - Spec (개발 명세서)

## 1. 기술 스택

### 1.1 Backend

| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Spring Boot 4.0.3 | Java 25 |
| ORM | Spring Data JPA | Hibernate 기반 |
| Database | H2 (인메모리) | 개발 편의, 재시작 시 초기화 |
| Build Tool | Gradle (Kotlin DSL) | build.gradle.kts |
| Utility | Lombok | Getter, Builder, NoArgsConstructor |
| Port | 8081 | application.properties |

### 1.2 Frontend

| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Next.js (latest) | App Router |
| Language | TypeScript | 타입 안전성 |
| Styling | Tailwind CSS | 유틸리티 기반 CSS |
| State Management | React Context 또는 Zustand | 전역 상태 관리 |
| HTTP Client | fetch API | Next.js 내장 |

### 1.3 통신

- CORS: frontend(`localhost:3000`) → backend(`localhost:8081`)
- API 응답 형식: JSON
- Base URL: `http://localhost:8081/api`
- 에러 응답: 표준 HTTP 상태 코드 + 메시지

---

## 2. 데이터 모델

### 2.1 Member (회원)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| email | String (unique) | 이메일 |
| password | String | 비밀번호 (BCrypt 암호화) |
| name | String | 이름 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### 2.2 ReminderList (목록)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| member | Member (FK) | 소속 회원 (개인 모드) |
| group | ReminderGroup (FK, nullable) | 소속 그룹 (그룹 모드, null이면 개인) |
| name | String | 목록 이름 |
| color | String | HEX 색상 코드 (예: #FF6B6B) |
| icon | String | 아이콘 이름 |
| sortOrder | Integer | 정렬 순서 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### 2.3 ReminderGroup (그룹)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| name | String | 그룹 이름 |
| owner | Member (FK) | 그룹 대표 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### 2.4 GroupMember (그룹원)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| group | ReminderGroup (FK) | 소속 그룹 |
| member | Member (FK) | 회원 |
| permission | enum (READ, READ_WRITE) | 그룹 내 권한 |
| joinedAt | LocalDateTime | 가입일시 |

> **unique 제약**: (group, member) 조합은 유일해야 한다.

### 2.5 Reminder (리마인더)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| title | String | 제목 (필수) |
| notes | String (TEXT) | 메모 |
| dueDate | LocalDateTime | 마감일시 |
| completed | Boolean | 완료 여부 |
| completedAt | LocalDateTime | 완료일시 |
| priority | enum (NONE, LOW, MEDIUM, HIGH) | 우선순위 |
| flagged | Boolean | 플래그 여부 |
| sortOrder | Integer | 목록 내 정렬 순서 |
| list | ReminderList (FK) | 소속 목록 |
| parent | Reminder (FK, nullable) | 부모 리마인더 (하위 작업용) |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

---

## 3. API 설계

### 3.1 인증 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /auth/signup | 회원가입 (email, password, name) → token + member |
| POST | /auth/login | 로그인 (email, password) → token + member |

> `/api/auth/**`는 인증 불필요. 그 외 `/api/**`는 JWT Bearer 토큰 필요.

**인증 플로우:**
1. 비로그인 사용자가 접근 시 → 로그인 페이지(`/login`)로 리다이렉트
2. 로그인/회원가입 성공 시 → JWT 토큰을 `localStorage`에 저장 → 리마인더 홈(`/`)으로 리다이렉트
3. 이후 모든 API 요청에 `Authorization: Bearer {token}` 헤더 포함
4. 토큰 만료/유효하지 않은 경우 → 401 응답 → 로그인 페이지로 리다이렉트

### 3.2 목록 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists | 전체 목록 조회 |
| POST | /lists | 목록 생성 |
| PUT | /lists/{id} | 목록 수정 |
| DELETE | /lists/{id} | 목록 삭제 |
| PATCH | /lists/reorder | 목록 순서 변경 |

### 3.3 리마인더 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists/{listId}/reminders | 목록별 리마인더 조회 |
| POST | /lists/{listId}/reminders | 리마인더 생성 |
| PUT | /reminders/{id} | 리마인더 수정 |
| DELETE | /reminders/{id} | 리마인더 삭제 |
| PATCH | /reminders/{id}/toggle | 완료 토글 |
| PATCH | /reminders/reorder | 리마인더 순서 변경 |

### 3.4 그룹 API (P2)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | /groups | 그룹 생성 |
| GET | /groups | 내가 속한 그룹 목록 조회 |
| PUT | /groups/{id} | 그룹 수정 (대표만) |
| DELETE | /groups/{id} | 그룹 삭제 (대표만) |
| POST | /groups/{id}/members | 그룹원 초대 (대표만) |
| DELETE | /groups/{id}/members/{memberId} | 그룹원 강퇴 (대표만) |
| PATCH | /groups/{id}/members/{memberId}/permission | 그룹원 권한 변경 (대표만) |
| GET | /groups/{id}/lists | 그룹 목록 조회 |

### 3.5 스마트 목록 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /reminders/today | 오늘 리마인더 |
| GET | /reminders/scheduled | 예정된 리마인더 |
| GET | /reminders/all | 전체 미완료 리마인더 |
| GET | /reminders/flagged | 플래그 리마인더 |
| GET | /reminders/completed | 완료된 리마인더 |

---

## 4. 프로젝트 구조

### 4.1 Backend

```
src/main/java/bamdul/ai/reminder/
├── auth/
│   ├── controller/        AuthController
│   ├── domain/            Member
│   ├── exception/         DuplicateEmailException
│   ├── repository/        MemberRepository
│   ├── security/          JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig
│   └── service/
│       ├── port/in/       AuthService
│       ├── dto/           SignupCommand, LoginCommand, AuthResult, MemberResult
│       └── DefaultAuthService
├── reminderlist/
│   ├── controller/        ReminderListController
│   ├── domain/            ReminderList
│   ├── repository/        ReminderListRepository
│   └── service/
│       ├── port/in/       ReminderListService
│       ├── dto/           CreateReminderListCommand, UpdateReminderListCommand,
│       │                  ReminderListResult, ReorderCommand
│       └── DefaultReminderListService
├── group/                         (P2 - 엔티티만 선설계)
│   ├── controller/        GroupController
│   ├── domain/            ReminderGroup, GroupMember, GroupPermission
│   ├── repository/        ReminderGroupRepository, GroupMemberRepository
│   └── service/
│       ├── port/in/       GroupService
│       ├── dto/           CreateGroupCommand, GroupResult, ...
│       └── DefaultGroupService
├── global/
│   └── exception/         GlobalExceptionHandler, ResourceNotFoundException
└── BamdulReminderApplication.java
```

> **패키지 규칙**: 도메인별(`{도메인}/`) 하위에 `domain/`, `repository/`, `service/`, `controller/` 등을 배치한다. Service 구현 클래스는 `Default` 접두사를 사용하고, DTO는 `service/dto/`에 Command(요청)/Result(응답)로 구분한다. 전역 공통 클래스는 `global/`에 배치한다.

### 4.2 Frontend

```
frontend/
├── src/
│   ├── app/
│   │   ├── layout.tsx
│   │   ├── page.tsx              (리마인더 홈 - 대시보드)
│   │   ├── login/page.tsx        (로그인)
│   │   ├── signup/page.tsx       (회원가입)
│   │   ├── lists/[id]/page.tsx   (목록별 리마인더)
│   │   ├── today/page.tsx
│   │   ├── scheduled/page.tsx
│   │   ├── all/page.tsx
│   │   ├── flagged/page.tsx
│   │   └── completed/page.tsx
│   ├── components/
│   ├── lib/                      (API 클라이언트, 인증 유틸)
│   └── types/
├── package.json
├── tailwind.config.ts
└── tsconfig.json
```

---

## 5. 개발 Phase 개요

> 각 Phase의 세부 작업 항목은 tasks.md 참조

| Phase | 목표 | 우선순위 |
|-------|------|----------|
| 1 | Backend 기본 구조 + 리마인더/목록 CRUD REST API | P0 |
| 2 | Frontend 기본 구조 + 2-패널 레이아웃 + 데이터 표시 | P0 |
| 3 | 리마인더 생성/수정/삭제 UI + 완료 토글 + 목록 관리 UI | P0 |
| 4 | 스마트 목록 API + 프론트엔드 페이지 + 대시보드 | P0 |
| 5 | 하위 리마인더 + 드래그 앤 드롭 정렬 | P1 |
| 6 | 반응형, 다크 모드, 검색, 키보드 단축키, 애니메이션 | P2 |
| 7 | 그룹 리마인더 (그룹 CRUD, 그룹원 초대/강퇴, 권한 관리) | P2 |
