# Bamdul Reminder - Tasks

## Phase 1: Backend 기본 구조 + 리마인더 CRUD

### 1.1 프로젝트 설정
- [ ] `application.properties` H2 DB 설정 (콘솔 활성화, DDL auto, 데이터소스 URL)
- [ ] CORS 설정 클래스 생성 (`localhost:3000` 허용)

### 1.2 Entity
- [ ] `ReminderList` 엔티티 (id, name, color, icon, sortOrder, createdAt, updatedAt)
- [ ] `Reminder` 엔티티 (id, title, notes, dueDate, completed, completedAt, priority, flagged, sortOrder, list FK, parent FK, createdAt, updatedAt)
- [ ] `Priority` enum (NONE, LOW, MEDIUM, HIGH)

### 1.3 Repository
- [ ] `ReminderListRepository` (JpaRepository)
- [ ] `ReminderRepository` (JpaRepository + 커스텀 쿼리 메서드)

### 1.4 DTO
- [ ] `ReminderListDto` (요청/응답용)
- [ ] `ReminderDto` (요청/응답용)

### 1.5 Service
- [ ] `ReminderListService` - 목록 CRUD 비즈니스 로직
- [ ] `ReminderService` - 리마인더 CRUD + 완료 토글 비즈니스 로직

### 1.6 Controller
- [ ] `ReminderListController` - `GET/POST/PUT/DELETE /api/lists`
- [ ] `ReminderController` - `GET/POST /api/lists/{listId}/reminders`, `PUT/DELETE /api/reminders/{id}`, `PATCH /api/reminders/{id}/toggle`

### 1.7 초기 데이터
- [ ] `data.sql` 작성 (샘플 목록 2~3개 + 리마인더 5~10개)

### 1.8 검증
- [ ] curl로 목록 CRUD API 테스트
- [ ] curl로 리마인더 CRUD API 테스트
- [ ] H2 콘솔(`/h2-console`)에서 데이터 확인

---

## Phase 2: Frontend 기본 구조 + 목록/리마인더 표시

### 2.1 프로젝트 초기화
- [ ] Next.js 프로젝트 생성 (`frontend/` 디렉토리, App Router, TypeScript, Tailwind CSS)
- [ ] 기본 디렉토리 구조 생성 (`components/`, `lib/`, `types/`)

### 2.2 타입 정의
- [ ] `types/reminder.ts` - Reminder, ReminderList, Priority 타입 정의

### 2.3 API 클라이언트
- [ ] `lib/api.ts` - fetch 래퍼 (baseURL, JSON 헤더, 에러 핸들링)
- [ ] 목록 API 함수 (getLists, createList, updateList, deleteList)
- [ ] 리마인더 API 함수 (getReminders, createReminder, updateReminder, deleteReminder, toggleReminder)

### 2.4 레이아웃
- [ ] `app/layout.tsx` - 루트 레이아웃 (2-패널: Sidebar + Main Content)
- [ ] Sidebar 컴포넌트 - 사용자 목록 표시 + 리마인더 개수 뱃지
- [ ] 목록 선택 상태 관리 (URL 기반 또는 Context)

### 2.5 리마인더 목록 표시
- [ ] `app/lists/[id]/page.tsx` - 특정 목록의 리마인더 표시 페이지
- [ ] ReminderItem 컴포넌트 - 체크박스, 제목, 우선순위 표시, 마감일 표시

### 2.6 검증
- [ ] 사이드바 목록 클릭 → 메인 영역에 리마인더 표시 확인
- [ ] 백엔드 API 연동 정상 동작 확인

---

## Phase 3: 리마인더 생성/수정/삭제 + 완료 토글

### 3.1 리마인더 추가
- [ ] "+ 리마인더 추가" 버튼 UI
- [ ] 인라인 입력 폼 (제목 입력 → Enter로 생성)

### 3.2 리마인더 편집
- [ ] 리마인더 항목 클릭 시 인라인 편집 모드 전환
- [ ] 제목 편집
- [ ] 메모 편집
- [ ] 마감일 선택 (date picker)
- [ ] 우선순위 선택 (NONE/LOW/MEDIUM/HIGH)
- [ ] 플래그 토글

### 3.3 리마인더 삭제
- [ ] 삭제 버튼 UI (호버 시 표시)
- [ ] 삭제 확인 처리

### 3.4 완료 토글
- [ ] 체크박스 클릭 → API 호출 (`PATCH /toggle`)
- [ ] 완료 애니메이션 (체크 표시 후 페이드아웃)

### 3.5 목록 관리 UI
- [ ] 사이드바 하단 "+ 목록 추가" 버튼
- [ ] 목록 생성 모달/인라인 폼 (이름, 색상 선택)
- [ ] 목록 수정 (이름, 색상, 아이콘 변경)
- [ ] 목록 삭제 (확인 다이얼로그 포함)

### 3.6 검증
- [ ] 리마인더 생성 → 목록에 즉시 반영
- [ ] 인라인 편집 후 저장 → 백엔드 반영 확인
- [ ] 체크박스 토글 → 완료/미완료 전환 + 애니메이션

---

## Phase 4: 스마트 목록 (Smart Lists)

### 4.1 Backend - 스마트 목록 API
- [ ] `GET /api/reminders/today` - 마감일이 오늘인 리마인더
- [ ] `GET /api/reminders/scheduled` - 마감일이 설정된 미완료 리마인더
- [ ] `GET /api/reminders/all` - 모든 미완료 리마인더
- [ ] `GET /api/reminders/flagged` - 플래그가 설정된 리마인더
- [ ] `GET /api/reminders/completed` - 완료된 리마인더
- [ ] 각 스마트 목록 카운트 API (뱃지용)

### 4.2 Frontend - 사이드바 스마트 목록
- [ ] 사이드바 상단에 스마트 목록 5종 고정 표시
- [ ] 아이콘/색상 적용 (Today=파랑, Scheduled=빨강, All=검정, Flagged=주황, Completed=회색)
- [ ] 각 스마트 목록 리마인더 카운트 뱃지

### 4.3 Frontend - 스마트 목록 페이지
- [ ] `app/today/page.tsx` - 오늘 리마인더
- [ ] `app/scheduled/page.tsx` - 예정된 리마인더
- [ ] `app/all/page.tsx` - 전체 미완료 리마인더
- [ ] `app/flagged/page.tsx` - 플래그 리마인더
- [ ] `app/completed/page.tsx` - 완료된 리마인더

### 4.4 Frontend - 대시보드
- [ ] `app/page.tsx` - 메인 대시보드에 스마트 목록 카운트 요약 카드

### 4.5 검증
- [ ] 스마트 목록 클릭 → 올바른 필터 조건의 리마인더만 표시
- [ ] 리마인더 변경 후 스마트 목록 카운트 즉시 반영

---

## Phase 5: 하위 리마인더 (Subtasks) + 정렬

### 5.1 Backend - 하위 리마인더
- [ ] 부모-자식 관계 기반 계층 데이터 반환 (Reminder.parent FK 활용)
- [ ] 하위 리마인더 생성 API (parentId 파라미터)

### 5.2 Backend - 정렬 API
- [ ] `PATCH /api/reminders/reorder` - 리마인더 순서 변경
- [ ] `PATCH /api/lists/reorder` - 목록 순서 변경

### 5.3 Frontend - 하위 리마인더 UI
- [ ] 리마인더 내 "+ 하위 작업" 버튼
- [ ] 들여쓰기로 계층 표현
- [ ] 부모 완료 시 하위 리마인더 일괄 처리 UX

### 5.4 Frontend - 드래그 앤 드롭
- [ ] 리마인더 목록 내 드래그 앤 드롭 정렬
- [ ] 사이드바 목록 드래그 앤 드롭 정렬

### 5.5 검증
- [ ] 하위 리마인더 생성/표시/완료 정상 동작
- [ ] 드래그 앤 드롭 순서 변경 → 새로고침 후에도 유지

---

## Phase 6: UI 폴리싱 + 추가 기능

### 6.1 반응형 디자인
- [ ] 모바일 사이드바 햄버거 메뉴 토글
- [ ] 태블릿/데스크탑 반응형 레이아웃 조정

### 6.2 다크 모드
- [ ] 다크 모드 테마 색상 정의
- [ ] 시스템 설정 연동 + 수동 토글 버튼

### 6.3 검색
- [ ] 검색 UI (사이드바 상단 또는 툴바)
- [ ] 리마인더 제목/메모 기반 검색 API + 프론트엔드 연동

### 6.4 키보드 단축키
- [ ] Enter - 빠른 리마인더 추가
- [ ] Esc - 편집 취소
- [ ] 기타 단축키 (Delete, Cmd+N 등)

### 6.5 로딩/에러 상태
- [ ] 스켈레톤 로딩 UI
- [ ] 토스트 알림 (성공/에러 메시지)

### 6.6 애니메이션
- [ ] 목록 전환 트랜지션
- [ ] 항목 추가/삭제 애니메이션

### 6.7 검증
- [ ] 모바일/태블릿/데스크탑 각 화면 크기 정상 동작
- [ ] 다크 모드 전환 정상 동작
- [ ] 검색 결과 즉시 필터링
