# Bamdul Reminder - Tasks

> 각 항목의 기능 상세는 prd.md, 기술 설계는 spec.md 참조

## Phase 1: Backend 기본 구조 + 리마인더 CRUD

### 1.1 프로젝트 설정
- [ ] `application.properties` H2 DB 설정 (콘솔 활성화, DDL auto, 데이터소스 URL)
- [ ] CORS 설정 클래스 생성 (`localhost:3000` 허용)

### 1.2 Domain
- [x] `ReminderList` 엔티티
- [ ] `Reminder` 엔티티
- [ ] `Priority` enum (NONE, LOW, MEDIUM, HIGH)

### 1.3 Repository
- [ ] `ReminderListRepository`
- [ ] `ReminderRepository` (+ 커스텀 쿼리 메서드)

### 1.4 DTO
- [ ] `ReminderListDto` (요청/응답용)
- [ ] `ReminderDto` (요청/응답용)

### 1.5 Service
- [ ] `ReminderListService` - 목록 CRUD
- [ ] `ReminderService` - 리마인더 CRUD + 완료 토글

### 1.6 Controller
- [ ] `ReminderListController` - `GET/POST/PUT/DELETE /api/lists`
- [ ] `ReminderController` - `GET/POST /api/lists/{listId}/reminders`, `PUT/DELETE /api/reminders/{id}`, `PATCH /api/reminders/{id}/toggle`

### 1.7 초기 데이터
- [ ] `data.sql` 작성 (샘플 목록 2~3개 + 리마인더 5~10개)

### 1.8 검증
- [ ] curl로 목록 CRUD API 테스트
- [ ] curl로 리마인더 CRUD API 테스트
- [ ] H2 콘솔에서 데이터 확인

---

## Phase 2: Frontend 기본 구조 + 목록/리마인더 표시

### 2.1 프로젝트 초기화
- [ ] Next.js 프로젝트 생성 (`frontend/`, App Router, TypeScript, Tailwind CSS)
- [ ] 기본 디렉토리 구조 생성 (`components/`, `lib/`, `types/`)

### 2.2 타입 정의
- [ ] `types/reminder.ts` - Reminder, ReminderList, Priority 타입

### 2.3 API 클라이언트
- [ ] `lib/api.ts` - fetch 래퍼 (baseURL, JSON 헤더, 에러 핸들링)
- [ ] 목록 API 함수 (getLists, createList, updateList, deleteList)
- [ ] 리마인더 API 함수 (getReminders, createReminder, updateReminder, deleteReminder, toggleReminder)

### 2.4 레이아웃
- [ ] `app/layout.tsx` - 루트 레이아웃 (2-패널: Sidebar + Main Content)
- [ ] Sidebar 컴포넌트 - 사용자 목록 + 리마인더 개수 뱃지
- [ ] 목록 선택 상태 관리

### 2.5 리마인더 목록 표시
- [ ] `app/lists/[id]/page.tsx` - 목록별 리마인더 표시
- [ ] ReminderItem 컴포넌트 - 체크박스, 제목, 우선순위, 마감일

### 2.6 검증
- [ ] 사이드바 목록 클릭 → 메인 영역에 리마인더 표시
- [ ] 백엔드 API 연동 정상 동작

---

## Phase 3: 리마인더 생성/수정/삭제 + 완료 토글

### 3.1 리마인더 추가
- [ ] "+ 리마인더 추가" 버튼
- [ ] 인라인 입력 폼 (Enter로 생성)

### 3.2 리마인더 편집
- [ ] 항목 클릭 시 인라인 편집 모드
- [ ] 제목, 메모, 마감일(date picker), 우선순위, 플래그 편집

### 3.3 리마인더 삭제
- [ ] 삭제 버튼 (호버 시 표시)
- [ ] 삭제 확인 처리

### 3.4 완료 토글
- [ ] 체크박스 클릭 → API 호출
- [ ] 완료 애니메이션 (체크 후 페이드아웃)

### 3.5 목록 관리 UI
- [ ] "+ 목록 추가" 버튼 + 생성 폼 (이름, 색상)
- [ ] 목록 수정 (이름, 색상, 아이콘)
- [ ] 목록 삭제 (확인 다이얼로그)

### 3.6 검증
- [ ] 리마인더 생성 → 목록에 즉시 반영
- [ ] 인라인 편집 → 백엔드 반영
- [ ] 체크박스 토글 → 완료/미완료 전환 + 애니메이션

---

## Phase 4: 스마트 목록

### 4.1 Backend
- [ ] `GET /api/reminders/today`
- [ ] `GET /api/reminders/scheduled`
- [ ] `GET /api/reminders/all`
- [ ] `GET /api/reminders/flagged`
- [ ] `GET /api/reminders/completed`
- [ ] 각 스마트 목록 카운트 API

### 4.2 Frontend - 사이드바
- [ ] 스마트 목록 5종 고정 표시
- [ ] 아이콘/색상 적용
- [ ] 카운트 뱃지

### 4.3 Frontend - 페이지
- [ ] `/today`, `/scheduled`, `/all`, `/flagged`, `/completed` 페이지
- [ ] 메인 대시보드 카운트 요약 카드

### 4.4 검증
- [ ] 스마트 목록 클릭 → 올바른 필터 결과
- [ ] 리마인더 변경 후 카운트 즉시 반영

---

## Phase 5: 하위 리마인더 + 정렬

### 5.1 Backend
- [ ] 부모-자식 계층 데이터 반환
- [ ] 하위 리마인더 생성 API (parentId)
- [ ] `PATCH /api/reminders/reorder`
- [ ] `PATCH /api/lists/reorder`

### 5.2 Frontend
- [ ] "+ 하위 작업" 버튼 + 들여쓰기 계층 표현
- [ ] 부모 완료 시 하위 일괄 처리 UX
- [ ] 리마인더 드래그 앤 드롭 정렬
- [ ] 사이드바 목록 드래그 앤 드롭 정렬

### 5.3 검증
- [ ] 하위 리마인더 생성/표시/완료
- [ ] 드래그 정렬 → 새로고침 후 유지

---

## Phase 6: UI 폴리싱 + 추가 기능

### 6.1 반응형 디자인
- [ ] 모바일 사이드바 햄버거 메뉴 토글
- [ ] 태블릿/데스크탑 레이아웃 조정

### 6.2 다크 모드
- [ ] 다크 모드 테마 색상 + 토글 버튼

### 6.3 검색
- [ ] 검색 UI + 리마인더 제목/메모 검색 연동

### 6.4 키보드 단축키
- [ ] Enter(빠른 추가), Esc(편집 취소) 등

### 6.5 로딩/에러 상태
- [ ] 스켈레톤 로딩 + 토스트 알림

### 6.6 애니메이션
- [ ] 목록 전환 + 항목 추가/삭제 트랜지션

### 6.7 검증
- [ ] 반응형 각 화면 크기 정상 동작
- [ ] 다크 모드 전환
- [ ] 검색 결과 즉시 필터링
