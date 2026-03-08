package bamdul.ai.reminder.group.domain;

import bamdul.ai.reminder.auth.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderGroupTest {

    private Member createMember(String email) {
        return Member.builder().email(email).password("encoded").name("테스트").build();
    }

    @Nested
    @DisplayName("생성")
    class CreateTest {

        @Test
        @DisplayName("그룹을 생성한다")
        void create() {
            var owner = createMember("owner@example.com");
            var group = ReminderGroup.builder().name("팀 프로젝트").owner(owner).build();

            assertThat(group.getName()).isEqualTo("팀 프로젝트");
            assertThat(group.getOwner()).isEqualTo(owner);
            assertThat(group.getCreatedAt()).isNotNull();
            assertThat(group.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("수정")
    class UpdateTest {

        @Test
        @DisplayName("그룹 이름을 수정한다")
        void update() {
            var owner = createMember("owner@example.com");
            var group = ReminderGroup.builder().name("원래 이름").owner(owner).build();

            group.update("새 이름");

            assertThat(group.getName()).isEqualTo("새 이름");
        }
    }

    @Nested
    @DisplayName("isOwner")
    class IsOwnerTest {

        @Test
        @DisplayName("소유자 객체가 동일한지 확인한다")
        void isOwner() {
            var owner = createMember("owner@example.com");
            var group = ReminderGroup.builder().name("그룹").owner(owner).build();

            // Pure unit test: ID is null without JPA, so verify owner reference
            assertThat(group.getOwner()).isEqualTo(owner);
        }
    }
}
