package bamdul.ai.reminder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderListTest {

    private Member createMember() {
        return Member.builder()
                .email("test@example.com")
                .password("encoded")
                .name("테스트")
                .build();
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("Builder로 모든 필드를 설정하여 생성한다")
        void createWithAllFields() {
            Member member = createMember();
            ReminderList list = ReminderList.builder()
                    .member(member)
                    .name("업무")
                    .color("#FF6B6B")
                    .icon("briefcase")
                    .sortOrder(3)
                    .build();

            assertThat(list.getName()).isEqualTo("업무");
            assertThat(list.getColor()).isEqualTo("#FF6B6B");
            assertThat(list.getIcon()).isEqualTo("briefcase");
            assertThat(list.getSortOrder()).isEqualTo(3);
            assertThat(list.getMember()).isEqualTo(member);
            assertThat(list.getId()).isNull();
        }

        @Test
        @DisplayName("sortOrder가 null이면 기본값 0으로 설정된다")
        void createWithNullSortOrder() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("개인")
                    .color("#4A90D9")
                    .icon("person")
                    .sortOrder(null)
                    .build();

            assertThat(list.getSortOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("선택 필드(color, icon) 없이 생성할 수 있다")
        void createWithRequiredFieldsOnly() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("메모")
                    .build();

            assertThat(list.getName()).isEqualTo("메모");
            assertThat(list.getColor()).isNull();
            assertThat(list.getIcon()).isNull();
            assertThat(list.getSortOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("생성 시 createdAt과 updatedAt이 자동 설정된다")
        void createSetsTimestamps() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("업무")
                    .build();

            assertThat(list.getCreatedAt()).isNotNull();
            assertThat(list.getUpdatedAt()).isNotNull();
            assertThat(list.getCreatedAt()).isEqualTo(list.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("update 테스트")
    class UpdateTest {

        @Test
        @DisplayName("이름, 색상, 아이콘을 변경한다")
        void updateFields() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("업무")
                    .color("#FF6B6B")
                    .icon("briefcase")
                    .build();

            list.update("개인", "#4A90D9", "person");

            assertThat(list.getName()).isEqualTo("개인");
            assertThat(list.getColor()).isEqualTo("#4A90D9");
            assertThat(list.getIcon()).isEqualTo("person");
        }

        @Test
        @DisplayName("update 시 updatedAt이 갱신된다")
        void updateRefreshesUpdatedAt() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("업무")
                    .build();
            var originalUpdatedAt = list.getUpdatedAt();

            list.update("개인", "#4A90D9", "person");

            assertThat(list.getCreatedAt()).isEqualTo(list.getCreatedAt());
            assertThat(list.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("sortOrder를 변경한다")
        void updateSortOrder() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("업무")
                    .sortOrder(0)
                    .build();

            list.updateSortOrder(5);

            assertThat(list.getSortOrder()).isEqualTo(5);
        }

        @Test
        @DisplayName("sortOrder 변경 시 updatedAt이 갱신된다")
        void updateSortOrderRefreshesUpdatedAt() {
            ReminderList list = ReminderList.builder()
                    .member(createMember())
                    .name("업무")
                    .build();
            var originalUpdatedAt = list.getUpdatedAt();

            list.updateSortOrder(3);

            assertThat(list.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }
}
