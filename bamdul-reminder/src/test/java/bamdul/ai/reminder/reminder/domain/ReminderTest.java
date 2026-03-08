package bamdul.ai.reminder.reminder.domain;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderTest {

    private Member createMember() {
        return Member.builder()
                .email("test@example.com")
                .password("encoded")
                .name("테스트")
                .build();
    }

    private ReminderList createList() {
        return ReminderList.builder()
                .member(createMember())
                .name("업무")
                .build();
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("Builder로 모든 필드를 설정하여 생성한다")
        void createWithAllFields() {
            var dueDate = LocalDateTime.of(2026, 3, 10, 9, 0);
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("회의 준비")
                    .notes("발표 자료 준비")
                    .dueDate(dueDate)
                    .priority(Priority.HIGH)
                    .flagged(true)
                    .sortOrder(1)
                    .build();

            assertThat(reminder.getTitle()).isEqualTo("회의 준비");
            assertThat(reminder.getNotes()).isEqualTo("발표 자료 준비");
            assertThat(reminder.getDueDate()).isEqualTo(dueDate);
            assertThat(reminder.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(reminder.getFlagged()).isTrue();
            assertThat(reminder.getSortOrder()).isEqualTo(1);
            assertThat(reminder.getCompleted()).isFalse();
            assertThat(reminder.getCompletedAt()).isNull();
            assertThat(reminder.getId()).isNull();
        }

        @Test
        @DisplayName("필수 필드만으로 생성하면 기본값이 설정된다")
        void createWithRequiredFieldsOnly() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();

            assertThat(reminder.getTitle()).isEqualTo("할 일");
            assertThat(reminder.getNotes()).isNull();
            assertThat(reminder.getDueDate()).isNull();
            assertThat(reminder.getPriority()).isEqualTo(Priority.NONE);
            assertThat(reminder.getFlagged()).isFalse();
            assertThat(reminder.getSortOrder()).isEqualTo(0);
            assertThat(reminder.getCompleted()).isFalse();
        }

        @Test
        @DisplayName("생성 시 createdAt과 updatedAt이 자동 설정된다")
        void createSetsTimestamps() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();

            assertThat(reminder.getCreatedAt()).isNotNull();
            assertThat(reminder.getUpdatedAt()).isNotNull();
            assertThat(reminder.getCreatedAt()).isEqualTo(reminder.getUpdatedAt());
        }

        @Test
        @DisplayName("부모 리마인더를 설정하여 하위 리마인더를 생성한다")
        void createWithParent() {
            var parent = Reminder.builder()
                    .list(createList())
                    .title("부모 작업")
                    .build();

            var child = Reminder.builder()
                    .list(createList())
                    .parent(parent)
                    .title("하위 작업")
                    .build();

            assertThat(child.getParent()).isEqualTo(parent);
        }
    }

    @Nested
    @DisplayName("update 테스트")
    class UpdateTest {

        @Test
        @DisplayName("제목, 메모, 마감일, 우선순위, 플래그를 변경한다")
        void updateFields() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("원래 제목")
                    .priority(Priority.LOW)
                    .build();

            var newDueDate = LocalDateTime.of(2026, 3, 15, 18, 0);
            reminder.update("새 제목", "메모 추가", newDueDate, Priority.HIGH, true);

            assertThat(reminder.getTitle()).isEqualTo("새 제목");
            assertThat(reminder.getNotes()).isEqualTo("메모 추가");
            assertThat(reminder.getDueDate()).isEqualTo(newDueDate);
            assertThat(reminder.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(reminder.getFlagged()).isTrue();
        }

        @Test
        @DisplayName("update 시 updatedAt이 갱신된다")
        void updateRefreshesUpdatedAt() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();
            var originalUpdatedAt = reminder.getUpdatedAt();

            reminder.update("수정된 제목", null, null, null, null);

            assertThat(reminder.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("toggleComplete 테스트")
    class ToggleCompleteTest {

        @Test
        @DisplayName("미완료 → 완료로 전환하면 completedAt이 설정된다")
        void toggleToComplete() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();

            reminder.toggleComplete();

            assertThat(reminder.getCompleted()).isTrue();
            assertThat(reminder.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("완료 → 미완료로 전환하면 completedAt이 null이 된다")
        void toggleToIncomplete() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();

            reminder.toggleComplete();
            reminder.toggleComplete();

            assertThat(reminder.getCompleted()).isFalse();
            assertThat(reminder.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("toggleComplete 시 updatedAt이 갱신된다")
        void toggleRefreshesUpdatedAt() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .build();
            var originalUpdatedAt = reminder.getUpdatedAt();

            reminder.toggleComplete();

            assertThat(reminder.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("updateSortOrder 테스트")
    class UpdateSortOrderTest {

        @Test
        @DisplayName("sortOrder를 변경한다")
        void updateSortOrder() {
            Reminder reminder = Reminder.builder()
                    .list(createList())
                    .title("할 일")
                    .sortOrder(0)
                    .build();

            reminder.updateSortOrder(5);

            assertThat(reminder.getSortOrder()).isEqualTo(5);
        }
    }
}
