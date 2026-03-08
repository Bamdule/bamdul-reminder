# CLAUDE.md - 코딩 관례

## 일반 원칙
- 기능 추가/수정 시 반드시 테스트 코드를 함께 작성한다
- 테스트는 먼저 실패를 확인한 후 구현하는 것을 권장한다

### 패키지 구조
- `domain/` - 엔티티, enum (entity 패키지 사용하지 않음)
- `repository/` - Spring Data JPA Repository
- `service/port/in/` - Service 인터페이스
- `service/` - Service 구현 클래스 (`Default` 접두사, 예: `DefaultReminderListService`)
- `service/dto/` - 서비스 입출력 객체 (Command: 요청, Result: 응답), `@Valid`/`@NotBlank` 등 검증 어노테이션 허용
- `controller/` - REST API 컨트롤러, 기본적으로 `service/dto/`를 직접 사용
- `controller/dto/` - API 스펙이 서비스 DTO와 다를 경우에만 별도 생성 (Request/Response)

### 테스트
- domain 엔티티 테스트는 순수 unit test (JPA/Spring Context 의존 없이)
- Service 테스트는 `@SpringBootTest` + `@Transactional` 통합 테스트 (Mock 사용하지 않음)
- `@Nested`와 `@DisplayName`으로 테스트 구조화
- AssertJ 사용 (`assertThat`)
- 테스트 클래스 위치는 프로덕션 코드와 동일한 패키지 구조

## Git
- 의미 단위로 커밋 분리

## 참고 문서
- prd.md : 제품 요구사항 (무엇을, 왜 - 기능, 사용자 시나리오, UI/UX)
- spec.md : 개발 명세 (어떻게 - 기술 스택, 데이터 모델, API, 프로젝트 구조)
- tasks.md : 작업 체크리스트 (실행 - Phase별 세부 태스크, 진행 상태 추적)