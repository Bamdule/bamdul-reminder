package bamdul.ai.reminder.service;

import bamdul.ai.reminder.domain.Member;
import bamdul.ai.reminder.domain.ReminderList;
import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;
import bamdul.ai.reminder.exception.ResourceNotFoundException;
import bamdul.ai.reminder.repository.MemberRepository;
import bamdul.ai.reminder.repository.ReminderListRepository;
import bamdul.ai.reminder.service.port.in.ReminderListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReminderListServiceTest {

    @Autowired
    private ReminderListService service;

    @Autowired
    private ReminderListRepository repository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        memberRepository.deleteAll();
        member = memberRepository.save(Member.builder()
                .email("test@example.com")
                .password("encoded")
                .name("테스트")
                .build());
    }

    private ReminderList saveReminderList(String name, Integer sortOrder) {
        return repository.save(ReminderList.builder().member(member).name(name).sortOrder(sortOrder).build());
    }

    private ReminderList saveReminderList(String name, String color, String icon) {
        return repository.save(ReminderList.builder().member(member).name(name).color(color).icon(icon).build());
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTest {

        @Test
        @DisplayName("모든 목록을 sortOrder 순으로 조회한다")
        void findAll() {
            saveReminderList("개인", 1);
            saveReminderList("업무", 0);

            var result = service.findAll(member.getId());

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("업무");
            assertThat(result.get(1).name()).isEqualTo("개인");
        }

        @Test
        @DisplayName("목록이 없으면 빈 리스트를 반환한다")
        void findAllEmpty() {
            var result = service.findAll(member.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 회원의 목록은 조회되지 않는다")
        void findAllExcludesOtherMembers() {
            saveReminderList("내 목록", 0);

            Member other = memberRepository.save(Member.builder()
                    .email("other@example.com").password("encoded").name("다른 사용자").build());
            repository.save(ReminderList.builder().member(other).name("다른 목록").sortOrder(0).build());

            var result = service.findAll(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("내 목록");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 목록을 조회한다")
        void findById() {
            var saved = saveReminderList("업무", "#FF6B6B", "briefcase");

            var result = service.findById(saved.getId(), member.getId());

            assertThat(result.name()).isEqualTo("업무");
            assertThat(result.color()).isEqualTo("#FF6B6B");
            assertThat(result.id()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외가 발생한다")
        void findByIdNotFound() {
            assertThatThrownBy(() -> service.findById(999L, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("다른 회원의 목록은 조회할 수 없다")
        void findByIdOtherMember() {
            var saved = saveReminderList("업무", 0);

            Member other = memberRepository.save(Member.builder()
                    .email("other@example.com").password("encoded").name("다른 사용자").build());

            assertThatThrownBy(() -> service.findById(saved.getId(), other.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("새 목록을 생성한다")
        void create() {
            var command = new CreateReminderListCommand("업무", "#FF6B6B", "briefcase", 0);

            var result = service.create(command, member.getId());

            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo("업무");
            assertThat(result.color()).isEqualTo("#FF6B6B");
            assertThat(result.icon()).isEqualTo("briefcase");
            assertThat(result.sortOrder()).isEqualTo(0);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("목록의 이름, 색상, 아이콘을 수정한다")
        void update() {
            var saved = saveReminderList("업무", "#FF6B6B", "briefcase");

            var command = new UpdateReminderListCommand("개인", "#4A90D9", "person");
            var result = service.update(saved.getId(), command, member.getId());

            assertThat(result.name()).isEqualTo("개인");
            assertThat(result.color()).isEqualTo("#4A90D9");
            assertThat(result.icon()).isEqualTo("person");
        }

        @Test
        @DisplayName("존재하지 않는 목록 수정 시 예외가 발생한다")
        void updateNotFound() {
            var command = new UpdateReminderListCommand("개인", "#4A90D9", "person");

            assertThatThrownBy(() -> service.update(999L, command, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("목록을 삭제한다")
        void delete() {
            var saved = saveReminderList("업무", 0);

            service.delete(saved.getId(), member.getId());

            assertThat(repository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 목록 삭제 시 예외가 발생한다")
        void deleteNotFound() {
            assertThatThrownBy(() -> service.delete(999L, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reorder")
    class ReorderTest {

        @Test
        @DisplayName("ID 순서대로 sortOrder를 재설정한다")
        void reorder() {
            var list1 = saveReminderList("업무", 0);
            var list2 = saveReminderList("개인", 1);
            var list3 = saveReminderList("쇼핑", 2);

            service.reorder(new ReorderCommand(List.of(list3.getId(), list1.getId(), list2.getId())), member.getId());

            List<ReminderListResult> result = service.findAll(member.getId());
            assertThat(result.get(0).name()).isEqualTo("쇼핑");
            assertThat(result.get(1).name()).isEqualTo("업무");
            assertThat(result.get(2).name()).isEqualTo("개인");
        }
    }
}
