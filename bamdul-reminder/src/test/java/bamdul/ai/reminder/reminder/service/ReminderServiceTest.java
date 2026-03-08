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

import java.time.LocalDate;
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

    @Nested
    @DisplayName("findAllByListId - 계층 구조")
    class FindAllByListIdWithChildrenTest {

        @Test
        @DisplayName("부모 리마인더의 하위 리마인더를 함께 반환한다")
        void findAllWithChildren() {
            var parent = saveReminder("부모", 0);
            reminderRepository.save(Reminder.builder()
                    .list(list).parent(parent).title("하위 1").sortOrder(0).build());
            reminderRepository.save(Reminder.builder()
                    .list(list).parent(parent).title("하위 2").sortOrder(1).build());

            var result = service.findAllByListId(list.getId(), member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).children()).hasSize(2);
            assertThat(result.get(0).children().get(0).title()).isEqualTo("하위 1");
            assertThat(result.get(0).children().get(1).title()).isEqualTo("하위 2");
        }
    }

    @Nested
    @DisplayName("findToday")
    class FindTodayTest {

        @Test
        @DisplayName("오늘 마감인 미완료 리마인더를 조회한다")
        void findToday() {
            LocalDateTime todayMorning = LocalDate.now().atTime(9, 0);
            LocalDateTime tomorrow = LocalDate.now().plusDays(1).atTime(9, 0);
            reminderRepository.save(Reminder.builder().list(list).title("오늘 할 일").dueDate(todayMorning).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("내일 할 일").dueDate(tomorrow).sortOrder(1).build());

            var result = service.findToday(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).title()).isEqualTo("오늘 할 일");
        }

        @Test
        @DisplayName("완료된 리마인더는 제외한다")
        void excludeCompleted() {
            var reminder = reminderRepository.save(Reminder.builder()
                    .list(list).title("완료됨").dueDate(LocalDate.now().atTime(9, 0)).sortOrder(0).build());
            service.toggleComplete(reminder.getId(), member.getId());

            var result = service.findToday(member.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findScheduled")
    class FindScheduledTest {

        @Test
        @DisplayName("마감일이 있는 미완료 리마인더를 조회한다")
        void findScheduled() {
            reminderRepository.save(Reminder.builder().list(list).title("예정됨").dueDate(LocalDateTime.of(2026, 6, 1, 9, 0)).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("마감일 없음").sortOrder(1).build());

            var result = service.findScheduled(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).title()).isEqualTo("예정됨");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTest {

        @Test
        @DisplayName("미완료 리마인더를 모두 조회한다")
        void findAll() {
            saveReminder("할 일 1", 0);
            saveReminder("할 일 2", 1);
            var completed = saveReminder("완료됨", 2);
            service.toggleComplete(completed.getId(), member.getId());

            var result = service.findAll(member.getId());

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findFlagged")
    class FindFlaggedTest {

        @Test
        @DisplayName("깃발 표시된 미완료 리마인더를 조회한다")
        void findFlagged() {
            reminderRepository.save(Reminder.builder().list(list).title("깃발").flagged(true).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("일반").sortOrder(1).build());

            var result = service.findFlagged(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).title()).isEqualTo("깃발");
        }
    }

    @Nested
    @DisplayName("findCompleted")
    class FindCompletedTest {

        @Test
        @DisplayName("완료된 리마인더를 조회한다")
        void findCompleted() {
            saveReminder("미완료", 0);
            var completed = saveReminder("완료됨", 1);
            service.toggleComplete(completed.getId(), member.getId());

            var result = service.findCompleted(member.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).title()).isEqualTo("완료됨");
        }
    }

    @Nested
    @DisplayName("countSmartLists")
    class CountSmartListsTest {

        @Test
        @DisplayName("각 스마트 목록의 카운트를 반환한다")
        void countSmartLists() {
            reminderRepository.save(Reminder.builder().list(list).title("오늘").dueDate(LocalDate.now().atTime(9, 0)).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("예정").dueDate(LocalDateTime.of(2026, 12, 1, 9, 0)).sortOrder(1).build());
            reminderRepository.save(Reminder.builder().list(list).title("깃발").flagged(true).sortOrder(2).build());
            var completed = saveReminder("완료 예정", 3);
            service.toggleComplete(completed.getId(), member.getId());

            var result = service.countSmartLists(member.getId());

            assertThat(result.today()).isEqualTo(1);
            assertThat(result.scheduled()).isEqualTo(2);
            assertThat(result.all()).isEqualTo(3);
            assertThat(result.flagged()).isEqualTo(1);
            assertThat(result.completed()).isEqualTo(1);
        }
    }
}
