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

### 2.1 ReminderList (목록)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| name | String | 목록 이름 |
| color | String | HEX 색상 코드 (예: #FF6B6B) |
| icon | String | 아이콘 이름 |
| sortOrder | Integer | 정렬 순서 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### 2.2 Reminder (리마인더)

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

### 3.1 목록 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists | 전체 목록 조회 |
| POST | /lists | 목록 생성 |
| PUT | /lists/{id} | 목록 수정 |
| DELETE | /lists/{id} | 목록 삭제 |
| PATCH | /lists/reorder | 목록 순서 변경 |

### 3.2 리마인더 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists/{listId}/reminders | 목록별 리마인더 조회 |
| POST | /lists/{listId}/reminders | 리마인더 생성 |
| PUT | /reminders/{id} | 리마인더 수정 |
| DELETE | /reminders/{id} | 리마인더 삭제 |
| PATCH | /reminders/{id}/toggle | 완료 토글 |
| PATCH | /reminders/reorder | 리마인더 순서 변경 |

### 3.3 스마트 목록 API

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
├── domain/
│   ├── Reminder.java
│   ├── ReminderList.java
│   └── Priority.java
├── repository/
│   ├── ReminderRepository.java
│   └── ReminderListRepository.java
├── service/
│   ├── ReminderService.java
│   └── ReminderListService.java
├── controller/
│   ├── ReminderController.java
│   └── ReminderListController.java
├── dto/
│   ├── ReminderDto.java
│   └── ReminderListDto.java
└── BamdulReminderApplication.java
```

### 4.2 Frontend

```
frontend/
├── src/
│   ├── app/
│   │   ├── layout.tsx
│   │   ├── page.tsx              (대시보드)
│   │   ├── lists/[id]/page.tsx   (목록별 리마인더)
│   │   ├── today/page.tsx
│   │   ├── scheduled/page.tsx
│   │   ├── all/page.tsx
│   │   ├── flagged/page.tsx
│   │   └── completed/page.tsx
│   ├── components/
│   ├── lib/                      (API 클라이언트)
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
