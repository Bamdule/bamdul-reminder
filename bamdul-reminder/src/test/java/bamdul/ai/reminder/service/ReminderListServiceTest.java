package bamdul.ai.reminder.service;

import bamdul.ai.reminder.domain.ReminderList;
import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;
import bamdul.ai.reminder.exception.ResourceNotFoundException;
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
    private ReminderListService service;  // 인터페이스로 주입

    @Autowired
    private ReminderListRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTest {

        @Test
        @DisplayName("모든 목록을 sortOrder 순으로 조회한다")
        void findAll() {
            repository.save(ReminderList.builder().name("개인").sortOrder(1).build());
            repository.save(ReminderList.builder().name("업무").sortOrder(0).build());

            var result = service.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("업무");
            assertThat(result.get(1).name()).isEqualTo("개인");
        }

        @Test
        @DisplayName("목록이 없으면 빈 리스트를 반환한다")
        void findAllEmpty() {
            var result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 목록을 조회한다")
        void findById() {
            var saved = repository.save(ReminderList.builder().name("업무").color("#FF6B6B").build());

            var result = service.findById(saved.getId());

            assertThat(result.name()).isEqualTo("업무");
            assertThat(result.color()).isEqualTo("#FF6B6B");
            assertThat(result.id()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외가 발생한다")
        void findByIdNotFound() {
            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("새 목록을 생성한다")
        void create() {
            var command = new CreateReminderListCommand("업무", "#FF6B6B", "briefcase", 0);

            var result = service.create(command);

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
            var saved = repository.save(ReminderList.builder().name("업무").color("#FF6B6B").icon("briefcase").build());

            var command = new UpdateReminderListCommand("개인", "#4A90D9", "person");
            var result = service.update(saved.getId(), command);

            assertThat(result.name()).isEqualTo("개인");
            assertThat(result.color()).isEqualTo("#4A90D9");
            assertThat(result.icon()).isEqualTo("person");
        }

        @Test
        @DisplayName("존재하지 않는 목록 수정 시 예외가 발생한다")
        void updateNotFound() {
            var command = new UpdateReminderListCommand("개인", "#4A90D9", "person");

            assertThatThrownBy(() -> service.update(999L, command))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("목록을 삭제한다")
        void delete() {
            var saved = repository.save(ReminderList.builder().name("업무").build());

            service.delete(saved.getId());

            assertThat(repository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 목록 삭제 시 예외가 발생한다")
        void deleteNotFound() {
            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reorder")
    class ReorderTest {

        @Test
        @DisplayName("ID 순서대로 sortOrder를 재설정한다")
        void reorder() {
            var list1 = repository.save(ReminderList.builder().name("업무").sortOrder(0).build());
            var list2 = repository.save(ReminderList.builder().name("개인").sortOrder(1).build());
            var list3 = repository.save(ReminderList.builder().name("쇼핑").sortOrder(2).build());

            service.reorder(new ReorderCommand(List.of(list3.getId(), list1.getId(), list2.getId())));

            List<ReminderListResult> result = service.findAll();
            assertThat(result.get(0).name()).isEqualTo("쇼핑");
            assertThat(result.get(1).name()).isEqualTo("업무");
            assertThat(result.get(2).name()).isEqualTo("개인");
        }
    }
}
