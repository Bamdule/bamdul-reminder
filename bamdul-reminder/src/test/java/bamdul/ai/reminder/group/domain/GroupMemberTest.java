package bamdul.ai.reminder.group.domain;

import bamdul.ai.reminder.auth.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupMemberTest {

    private Member createMember(String email) {
        return Member.builder().email(email).password("encoded").name("테스트").build();
    }

    @Nested
    @DisplayName("생성")
    class CreateTest {

        @Test
        @DisplayName("그룹원을 생성한다")
        void create() {
            var owner = createMember("owner@example.com");
            var member = createMember("member@example.com");
            var group = ReminderGroup.builder().name("그룹").owner(owner).build();

            var gm = GroupMember.builder()
                    .group(group).member(member).permission(GroupPermission.READ).build();

            assertThat(gm.getGroup()).isEqualTo(group);
            assertThat(gm.getMember()).isEqualTo(member);
            assertThat(gm.getPermission()).isEqualTo(GroupPermission.READ);
            assertThat(gm.getJoinedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("권한")
    class PermissionTest {

        @Test
        @DisplayName("READ 권한은 쓰기 불가")
        void readCannotWrite() {
            var owner = createMember("owner@example.com");
            var member = createMember("member@example.com");
            var group = ReminderGroup.builder().name("그룹").owner(owner).build();
            var gm = GroupMember.builder()
                    .group(group).member(member).permission(GroupPermission.READ).build();

            assertThat(gm.canWrite()).isFalse();
        }

        @Test
        @DisplayName("READ_WRITE 권한은 쓰기 가능")
        void readWriteCanWrite() {
            var owner = createMember("owner@example.com");
            var member = createMember("member@example.com");
            var group = ReminderGroup.builder().name("그룹").owner(owner).build();
            var gm = GroupMember.builder()
                    .group(group).member(member).permission(GroupPermission.READ_WRITE).build();

            assertThat(gm.canWrite()).isTrue();
        }

        @Test
        @DisplayName("권한을 변경한다")
        void updatePermission() {
            var owner = createMember("owner@example.com");
            var member = createMember("member@example.com");
            var group = ReminderGroup.builder().name("그룹").owner(owner).build();
            var gm = GroupMember.builder()
                    .group(group).member(member).permission(GroupPermission.READ).build();

            gm.updatePermission(GroupPermission.READ_WRITE);

            assertThat(gm.getPermission()).isEqualTo(GroupPermission.READ_WRITE);
            assertThat(gm.canWrite()).isTrue();
        }
    }
}
