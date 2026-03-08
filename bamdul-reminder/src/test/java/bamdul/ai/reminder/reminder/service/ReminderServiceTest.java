package bamdul.ai.reminder.reminder.service;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.global.exception.ResourceNotFoundException;
import bamdul.ai.reminder.reminder.domain.Priority;
import bamdul.ai.reminder.reminder.domain.Reminder;
import bamdul.ai.reminder.reminder.repository.ReminderRepository;
import bamdul.ai.reminder.reminder.service.dto.CreateReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.ReorderReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.UpdateReminderCommand;
import bamdul.ai.reminder.reminder.service.port.in.ReminderService;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReminderServiceTest {

    @Autowired
    private ReminderService service;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ReminderListRepository listRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private ReminderList list;

    @BeforeEach
    void setUp() {
        reminderRepository.deleteAll();
        listRepository.deleteAll();
        memberRepository.deleteAll();
        member = memberRepository.save(Member.builder()
                .email("test@example.com").password("encoded").name("테스트").build());
        list = listRepository.save(ReminderList.builder()
                .member(member).name("업무").build());
    }

    private Reminder saveReminder(String title, Integer sortOrder) {
        return reminderRepository.save(Reminder.builder()
                .list(list).title(title).sortOrder(sortOrder).build());
    }

    @Nested
    @DisplayName("findAllByListId")
    class FindAllByListIdTest {

        @Test
        @DisplayName("목록의 리마인더를 sortOrder 순으로 조회한다")
        void findAll() {
            saveReminder("두 번째", 1);
            saveReminder("첫 번째", 0);

            var result = service.findAllByListId(list.getId(), member.getId());

            assertThat(result).hasSize(2);
            assertThat(result.get(0).title()).isEqualTo("첫 번째");
            assertThat(result.get(1).title()).isEqualTo("두 번째");
        }

        @Test
        @DisplayName("리마인더가 없으면 빈 리스트를 반환한다")
        void findAllEmpty() {
            var result = service.findAllByListId(list.getId(), member.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 회원의 목록은 조회할 수 없다")
        void findAllOtherMember() {
            Member other = memberRepository.save(Member.builder()
                    .email("other@example.com").password("encoded").name("다른 사용자").build());

            assertThatThrownBy(() -> service.findAllByListId(list.getId(), other.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 리마인더를 조회한다")
        void findById() {
            var saved = saveReminder("할 일", 0);

            var result = service.findById(saved.getId(), member.getId());

            assertThat(result.title()).isEqualTo("할 일");
            assertThat(result.id()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외가 발생한다")
        void findByIdNotFound() {
            assertThatThrownBy(() -> service.findById(999L, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("새 리마인더를 생성한다")
        void create() {
            var dueDate = LocalDateTime.of(2026, 3, 10, 9, 0);
            var command = new CreateReminderCommand("회의 준비", "발표 자료", dueDate, Priority.HIGH, true, 0, null);

            var result = service.create(list.getId(), command, member.getId());

            assertThat(result.id()).isNotNull();
            assertThat(result.title()).isEqualTo("회의 준비");
            assertThat(result.notes()).isEqualTo("발표 자료");
            assertThat(result.dueDate()).isEqualTo(dueDate);
            assertThat(result.priority()).isEqualTo(Priority.HIGH);
            assertThat(result.flagged()).isTrue();
            assertThat(result.completed()).isFalse();
        }

        @Test
        @DisplayName("부모 리마인더를 지정하여 하위 리마인더를 생성한다")
        void createWithParent() {
            var parent = saveReminder("부모 작업", 0);
            var command = new CreateReminderCommand("하위 작업", null, null, null, null, 0, parent.getId());

            var result = service.create(list.getId(), command, member.getId());

            assertThat(result.parentId()).isEqualTo(parent.getId());
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("리마인더를 수정한다")
        void update() {
            var saved = saveReminder("원래 제목", 0);
            var command = new UpdateReminderCommand("새 제목", "메모", null, Priority.MEDIUM, true);

            var result = service.update(saved.getId(), command, member.getId());

            assertThat(result.title()).isEqualTo("새 제목");
            assertThat(result.notes()).isEqualTo("메모");
            assertThat(result.priority()).isEqualTo(Priority.MEDIUM);
            assertThat(result.flagged()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 리마인더 수정 시 예외가 발생한다")
        void updateNotFound() {
            var command = new UpdateReminderCommand("새 제목", null, null, null, null);

            assertThatThrownBy(() -> service.update(999L, command, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("리마인더를 삭제한다")
        void delete() {
            var saved = saveReminder("할 일", 0);

            service.delete(saved.getId(), member.getId());

            assertThat(reminderRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 리마인더 삭제 시 예외가 발생한다")
        void deleteNotFound() {
            assertThatThrownBy(() -> service.delete(999L, member.getId()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("toggleComplete")
    class ToggleCompleteTest {

        @Test
        @DisplayName("미완료 리마인더를 완료로 전환한다")
        void toggleToComplete() {
            var saved = saveReminder("할 일", 0);

            var result = service.toggleComplete(saved.getId(), member.getId());

            assertThat(result.completed()).isTrue();
            assertThat(result.completedAt()).isNotNull();
        }

        @Test
        @DisplayName("완료 리마인더를 미완료로 전환한다")
        void toggleToIncomplete() {
            var saved = saveReminder("할 일", 0);
            service.toggleComplete(saved.getId(), member.getId());

            var result = service.toggleComplete(saved.getId(), member.getId());

            assertThat(result.completed()).isFalse();
            assertThat(result.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("reorder")
    class ReorderTest {

        @Test
        @DisplayName("ID 순서대로 sortOrder를 재설정한다")
        void reorder() {
            var r1 = saveReminder("첫 번째", 0);
            var r2 = saveReminder("두 번째", 1);
            var r3 = saveReminder("세 번째", 2);

            service.reorder(new ReorderReminderCommand(List.of(r3.getId(), r1.getId(), r2.getId())), member.getId());

            var result = service.findAllByListId(list.getId(), member.getId());
            assertThat(result.get(0).title()).isEqualTo("세 번째");
            assertThat(result.get(1).title()).isEqualTo("첫 번째");
            assertThat(result.get(2).title()).isEqualTo("두 번째");
        }
    }
}
