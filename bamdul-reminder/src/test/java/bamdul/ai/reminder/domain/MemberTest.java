package bamdul.ai.reminder.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("Builder로 모든 필드를 설정하여 생성한다")
        void createWithAllFields() {
            Member member = Member.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .name("홍길동")
                    .build();

            assertThat(member.getEmail()).isEqualTo("test@example.com");
            assertThat(member.getPassword()).isEqualTo("encodedPassword");
            assertThat(member.getName()).isEqualTo("홍길동");
            assertThat(member.getId()).isNull();
        }

        @Test
        @DisplayName("생성 시 createdAt과 updatedAt이 자동 설정된다")
        void createSetsTimestamps() {
            Member member = Member.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .name("홍길동")
                    .build();

            assertThat(member.getCreatedAt()).isNotNull();
            assertThat(member.getUpdatedAt()).isNotNull();
            assertThat(member.getCreatedAt()).isEqualTo(member.getUpdatedAt());
        }
    }
}
