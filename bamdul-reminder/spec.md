# Bamdul Reminder - Spec (개발 명세서)

## 1. 개요

Apple Reminder 앱의 핵심 기능을 웹 버전으로 구현한다.

- **목표**: 리마인더 생성, 관리, 목록 분류 등 Apple Reminder의 핵심 UX를 웹에서 재현

---

## 2. 기술 스택

### 2.1 Backend

| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Spring Boot 4.0.3 | Java 21+ |
| ORM | Spring Data JPA | Hibernate 기반 |
| Database | H2 (인메모리) | 개발 편의, 재시작 시 초기화 |
| Build Tool | Gradle (Kotlin DSL) | build.gradle.kts |
| Port | 8081 | application.properties에서 설정 |

### 2.2 Frontend

| 항목 | 기술 | 비고 |
|------|------|------|
| Framework | Next.js (latest) | App Router 사용 |
| Language | TypeScript | 타입 안전성 |
| Styling | Tailwind CSS | 유틸리티 기반 CSS |
| State Management | React Context 또는 Zustand | 전역 상태 관리 |
| HTTP Client | fetch API | Next.js 내장 |

### 2.3 통신

- CORS 설정: frontend(`localhost:3000`) → backend(`localhost:8081`)
- API 응답 형식: JSON
- Base URL: `http://localhost:8081/api`
- 에러 응답: 표준 HTTP 상태 코드 + 메시지

---

## 3. 데이터 모델

### 3.1 ReminderList (목록)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long (PK) | 자동 생성 |
| name | String | 목록 이름 |
| color | String | HEX 색상 코드 (예: #FF6B6B) |
| icon | String | 아이콘 이름 |
| sortOrder | Integer | 정렬 순서 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### 3.2 Reminder (리마인더)

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

## 4. API 설계

### 4.1 목록 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists | 전체 목록 조회 |
| POST | /lists | 목록 생성 |
| PUT | /lists/{id} | 목록 수정 |
| DELETE | /lists/{id} | 목록 삭제 |
| PATCH | /lists/reorder | 목록 순서 변경 |

### 4.2 리마인더 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /lists/{listId}/reminders | 목록별 리마인더 조회 |
| POST | /lists/{listId}/reminders | 리마인더 생성 |
| PUT | /reminders/{id} | 리마인더 수정 |
| DELETE | /reminders/{id} | 리마인더 삭제 |
| PATCH | /reminders/{id}/toggle | 완료 토글 |
| PATCH | /reminders/reorder | 리마인더 순서 변경 |

### 4.3 스마트 목록 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | /reminders/today | 오늘 리마인더 |
| GET | /reminders/scheduled | 예정된 리마인더 |
| GET | /reminders/all | 전체 미완료 리마인더 |
| GET | /reminders/flagged | 플래그 리마인더 |
| GET | /reminders/completed | 완료된 리마인더 |

---

## 5. UI/UX 요구사항

### 5.1 레이아웃

Apple Reminder 스타일 2-패널 레이아웃:

```
┌──────────────────────────────────────────┐
│  Bamdul Reminder                         │
├────────────┬─────────────────────────────┤
│            │                             │
│  Sidebar   │   Main Content              │
│            │                             │
│  ┌──────┐  │   리마인더 목록 제목          │
│  │Today │  │   ─────────────────────     │
│  │Sched.│  │   ☐ 리마인더 항목 1          │
│  │All   │  │   ☐ 리마인더 항목 2          │
│  │Flag  │  │     ☐ 하위 리마인더          │
│  │Done  │  │   ☐ 리마인더 항목 3          │
│  ├──────┤  │                             │
│  │내 목록│  │   + 리마인더 추가            │
│  │ List1│  │                             │
│  │ List2│  │                             │
│  └──────┘  │                             │
│            │                             │
│ + 목록추가  │                             │
├────────────┴─────────────────────────────┤
│  Summary counts (Today: 3, Scheduled: 5) │
└──────────────────────────────────────────┘
```

### 5.2 디자인 원칙

- Apple Reminder 스타일의 깔끔한 디자인 (밝은 테마)
- 스마트 목록 아이콘/색상: Today=파랑, Scheduled=빨강, All=검정, Flagged=주황, Completed=회색
- 반응형 디자인 (모바일에서 사이드바 토글)

### 5.3 주요 페이지/컴포넌트

| 경로 | 설명 |
|------|------|
| `/` | 메인 대시보드 (스마트 목록 카운트 요약) |
| `/lists/[id]` | 특정 목록의 리마인더 표시 |
| `/today` | 오늘 리마인더 |
| `/scheduled` | 예정된 리마인더 |
| `/all` | 전체 리마인더 |
| `/flagged` | 플래그 리마인더 |
| `/completed` | 완료된 리마인더 |

---

## 6. 프로젝트 구조

```
bamdul-reminder/
├── src/main/java/bamdul/ai/reminder/    (Spring Boot Backend)
│   ├── entity/
│   │   ├── Reminder.java
│   │   └── ReminderList.java
│   ├── repository/
│   │   ├── ReminderRepository.java
│   │   └── ReminderListRepository.java
│   ├── service/
│   │   ├── ReminderService.java
│   │   └── ReminderListService.java
│   ├── controller/
│   │   ├── ReminderController.java
│   │   └── ReminderListController.java
│   ├── dto/
│   │   ├── ReminderDto.java
│   │   └── ReminderListDto.java
│   └── BamdulReminderApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── data.sql                          (초기 샘플 데이터)
├── frontend/                             (Next.js 프로젝트)
│   ├── src/
│   │   ├── app/                          (App Router)
│   │   ├── components/
│   │   ├── lib/                          (API 클라이언트 등)
│   │   └── types/
│   ├── package.json
│   ├── tailwind.config.ts
│   └── tsconfig.json
└── build.gradle.kts
```

---

## 7. 개발 계획 (Phase별)

### Phase 1: Backend 기본 구조 + 리마인더 CRUD

> 목표: Spring Boot 백엔드에서 리마인더를 생성/조회/수정/삭제할 수 있는 REST API 완성

**Backend 작업:**
- [ ] Entity 생성: `Reminder`, `ReminderList`  (JPA `@Entity`, `@Id`, `@GeneratedValue`)
- [ ] Repository 생성: `ReminderRepository`, `ReminderListRepository` (Spring Data JPA `JpaRepository`)
- [ ] DTO 생성: `ReminderDto`, `ReminderListDto` (요청/응답 객체)
- [ ] Service 생성: `ReminderService`, `ReminderListService` (비즈니스 로직)
- [ ] Controller 생성: 목록 CRUD API (`/api/lists`), 리마인더 CRUD API (`/api/lists/{listId}/reminders`)
- [ ] 완료 토글 API: `PATCH /api/reminders/{id}/toggle`
- [ ] CORS 설정: `localhost:3000` 허용
- [ ] H2 콘솔 활성화 + application.properties 설정
- [ ] `data.sql`로 초기 샘플 데이터 삽입

**검증:**
- curl 또는 Postman으로 모든 CRUD API 정상 동작 확인
- H2 콘솔(`/h2-console`)에서 데이터 확인

---

### Phase 2: Frontend 기본 구조 + 목록/리마인더 표시

> 목표: Next.js 프론트엔드에서 백엔드 데이터를 불러와 2-패널 레이아웃으로 표시

**Frontend 작업:**
- [ ] Next.js 프로젝트 초기화 (App Router, TypeScript, Tailwind CSS)
- [ ] TypeScript 타입 정의 (`types/` 디렉토리)
- [ ] API 클라이언트 모듈 생성 (`lib/api.ts` - fetch 래퍼)
- [ ] 2-패널 레이아웃 구현: Sidebar + Main Content
- [ ] Sidebar 컴포넌트: 사용자 목록 표시 + 리마인더 개수 뱃지
- [ ] 목록 선택 시 해당 목록의 리마인더 조회/표시
- [ ] 리마인더 항목 컴포넌트: 제목, 체크박스, 우선순위 표시

**검증:**
- 사이드바에서 목록 클릭 → 메인 영역에 리마인더 목록 표시
- 백엔드 API 연동 정상 동작 확인

---

### Phase 3: 리마인더 생성/수정/삭제 + 완료 토글

> 목표: 프론트엔드에서 리마인더의 모든 CRUD 작업 및 완료 처리 가능

**Frontend 작업:**
- [ ] 리마인더 추가 UI: "+ 리마인더 추가" 버튼 → 인라인 입력 폼
- [ ] 리마인더 인라인 편집: 항목 클릭 시 제목/메모/마감일/우선순위/플래그 편집
- [ ] 리마인더 삭제: 삭제 버튼 또는 스와이프 동작
- [ ] 완료 토글: 체크박스 클릭 → 완료 애니메이션 (체크 후 페이드아웃)
- [ ] 목록 생성/수정/삭제 UI: 사이드바 하단 "+ 목록 추가" 버튼

**검증:**
- 리마인더 생성 → 목록에 즉시 반영
- 인라인 편집 후 저장 → 백엔드 반영 확인
- 체크박스 토글 → 완료/미완료 전환 + 애니메이션

---

### Phase 4: 스마트 목록 (Smart Lists)

> 목표: 오늘/예정/전체/플래그/완료 등 필터 기반 가상 목록 구현

**Backend 작업:**
- [ ] 스마트 목록 API 구현:
  - `GET /api/reminders/today` - 마감일이 오늘인 리마인더
  - `GET /api/reminders/scheduled` - 마감일이 설정된 미완료 리마인더
  - `GET /api/reminders/all` - 모든 미완료 리마인더
  - `GET /api/reminders/flagged` - 플래그가 설정된 리마인더
  - `GET /api/reminders/completed` - 완료된 리마인더

**Frontend 작업:**
- [ ] 사이드바 상단에 스마트 목록 5종 고정 표시
- [ ] 스마트 목록 아이콘/색상 적용 (Today=파랑, Scheduled=빨강, All=검정, Flagged=주황, Completed=회색)
- [ ] 각 스마트 목록별 전용 페이지 (`/today`, `/scheduled`, `/all`, `/flagged`, `/completed`)
- [ ] 메인 대시보드(`/`)에 스마트 목록 카운트 요약 표시

**검증:**
- 스마트 목록 클릭 → 올바른 필터 조건의 리마인더만 표시
- 리마인더 변경(완료, 플래그 등) 후 스마트 목록 카운트 즉시 반영

---

### Phase 5: 하위 리마인더 (Subtasks) + 정렬

> 목표: 리마인더에 하위 작업 추가 및 드래그 앤 드롭 정렬 지원

**Backend 작업:**
- [ ] 하위 리마인더 조회: 부모-자식 관계 (self-referencing `parent` FK) 기반 계층 데이터 반환
- [ ] 리마인더 순서 변경 API: `PATCH /api/reminders/reorder`
- [ ] 목록 순서 변경 API: `PATCH /api/lists/reorder`

**Frontend 작업:**
- [ ] 하위 리마인더 추가 UI: 리마인더 내 "+ 하위 작업" 버튼
- [ ] 들여쓰기로 계층 표현
- [ ] 부모 리마인더 완료 시 하위 리마인더 일괄 처리 UX
- [ ] 드래그 앤 드롭 정렬 (리마인더 목록 내, 사이드바 목록)

**검증:**
- 하위 리마인더 생성/표시/완료 정상 동작
- 드래그 앤 드롭으로 순서 변경 → 새로고침 후에도 유지

---

### Phase 6: UI 폴리싱 + 추가 기능

> 목표: 완성도 높은 UI/UX 및 부가 기능 추가

**작업 항목:**
- [ ] 반응형 디자인: 모바일에서 사이드바 햄버거 메뉴 토글
- [ ] 다크 모드 지원
- [ ] 검색 기능: 리마인더 제목/메모 검색
- [ ] 키보드 단축키 (Enter로 빠른 추가, Esc로 편집 취소 등)
- [ ] 로딩/에러 상태 UI (스켈레톤, 토스트 알림)
- [ ] 애니메이션 개선 (목록 전환, 항목 추가/삭제 트랜지션)

**검증:**
- 모바일/태블릿/데스크탑 각 화면 크기에서 정상 동작
- 다크 모드 전환 정상 동작
- 검색 결과 즉시 필터링
