package bamdul.ai.reminder.reminder.controller;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.reminder.domain.Reminder;
import bamdul.ai.reminder.reminder.repository.ReminderRepository;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import bamdul.ai.reminder.reminder.service.port.in.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ReminderListRepository listRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReminderService reminderService;

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

    private RequestPostProcessor authenticated() {
        return authentication(new UsernamePasswordAuthenticationToken(member.getId(), null, List.of()));
    }

    private Reminder saveReminder(String title, Integer sortOrder) {
        return reminderRepository.save(Reminder.builder()
                .list(list).title(title).sortOrder(sortOrder).build());
    }

    @Nested
    @DisplayName("GET /api/lists/{listId}/reminders")
    class FindAllTest {

        @Test
        @DisplayName("목록의 리마인더를 조회한다")
        void findAll() throws Exception {
            saveReminder("두 번째", 1);
            saveReminder("첫 번째", 0);

            mockMvc.perform(get("/api/lists/{listId}/reminders", list.getId())
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title", is("첫 번째")))
                    .andExpect(jsonPath("$[1].title", is("두 번째")));
        }
    }

    @Nested
    @DisplayName("POST /api/lists/{listId}/reminders")
    class CreateTest {

        @Test
        @DisplayName("새 리마인더를 생성하고 201을 반환한다")
        void create() throws Exception {
            mockMvc.perform(post("/api/lists/{listId}/reminders", list.getId())
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "회의 준비", "notes": "발표 자료", "priority": "HIGH", "flagged": true, "sortOrder": 0}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title", is("회의 준비")))
                    .andExpect(jsonPath("$.priority", is("HIGH")))
                    .andExpect(jsonPath("$.flagged", is(true)))
                    .andExpect(jsonPath("$.completed", is(false)));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/{id}")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 리마인더를 조회한다")
        void findById() throws Exception {
            var saved = saveReminder("할 일", 0);

            mockMvc.perform(get("/api/reminders/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("할 일")));
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 404를 반환한다")
        void findByIdNotFound() throws Exception {
            mockMvc.perform(get("/api/reminders/{id}", 999L)
                            .with(authenticated()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/reminders/{id}")
    class UpdateTest {

        @Test
        @DisplayName("리마인더를 수정하고 200을 반환한다")
        void update() throws Exception {
            var saved = saveReminder("원래 제목", 0);

            mockMvc.perform(put("/api/reminders/{id}", saved.getId())
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title": "새 제목", "notes": "메모", "priority": "MEDIUM", "flagged": true}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("새 제목")))
                    .andExpect(jsonPath("$.priority", is("MEDIUM")))
                    .andExpect(jsonPath("$.flagged", is(true)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/reminders/{id}")
    class DeleteTest {

        @Test
        @DisplayName("리마인더를 삭제하고 204를 반환한다")
        void deleteReminder() throws Exception {
            var saved = saveReminder("할 일", 0);

            mockMvc.perform(delete("/api/reminders/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/reminders/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/reminders/{id}/toggle")
    class ToggleTest {

        @Test
        @DisplayName("리마인더 완료를 토글한다")
        void toggle() throws Exception {
            var saved = saveReminder("할 일", 0);

            mockMvc.perform(patch("/api/reminders/{id}/toggle", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed", is(true)))
                    .andExpect(jsonPath("$.completedAt", notNullValue()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/reminders/reorder")
    class ReorderTest {

        @Test
        @DisplayName("리마인더 순서를 변경하고 204를 반환한다")
        void reorder() throws Exception {
            var r1 = saveReminder("첫 번째", 0);
            var r2 = saveReminder("두 번째", 1);
            var r3 = saveReminder("세 번째", 2);

            mockMvc.perform(patch("/api/reminders/reorder")
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"ids": [%d, %d, %d]}
                                    """.formatted(r3.getId(), r1.getId(), r2.getId())))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/lists/{listId}/reminders", list.getId())
                            .with(authenticated()))
                    .andExpect(jsonPath("$[0].title", is("세 번째")))
                    .andExpect(jsonPath("$[1].title", is("첫 번째")))
                    .andExpect(jsonPath("$[2].title", is("두 번째")));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/today")
    class FindTodayTest {

        @Test
        @DisplayName("오늘 마감인 미완료 리마인더를 조회한다")
        void findToday() throws Exception {
            reminderRepository.save(Reminder.builder().list(list).title("오늘 할 일")
                    .dueDate(LocalDate.now().atTime(9, 0)).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("내일 할 일")
                    .dueDate(LocalDate.now().plusDays(1).atTime(9, 0)).sortOrder(1).build());

            mockMvc.perform(get("/api/reminders/today").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("오늘 할 일")));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/scheduled")
    class FindScheduledTest {

        @Test
        @DisplayName("마감일이 있는 미완료 리마인더를 조회한다")
        void findScheduled() throws Exception {
            reminderRepository.save(Reminder.builder().list(list).title("예정됨")
                    .dueDate(LocalDateTime.of(2026, 6, 1, 9, 0)).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("마감일 없음").sortOrder(1).build());

            mockMvc.perform(get("/api/reminders/scheduled").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("예정됨")));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/all")
    class FindAllSmartTest {

        @Test
        @DisplayName("미완료 리마인더를 모두 조회한다")
        void findAll() throws Exception {
            saveReminder("할 일 1", 0);
            saveReminder("할 일 2", 1);
            var completed = saveReminder("완료됨", 2);
            reminderService.toggleComplete(completed.getId(), member.getId());

            mockMvc.perform(get("/api/reminders/all").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/flagged")
    class FindFlaggedTest {

        @Test
        @DisplayName("깃발 표시된 미완료 리마인더를 조회한다")
        void findFlagged() throws Exception {
            reminderRepository.save(Reminder.builder().list(list).title("깃발")
                    .flagged(true).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("일반").sortOrder(1).build());

            mockMvc.perform(get("/api/reminders/flagged").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("깃발")));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/completed")
    class FindCompletedTest {

        @Test
        @DisplayName("완료된 리마인더를 조회한다")
        void findCompleted() throws Exception {
            saveReminder("미완료", 0);
            var completed = saveReminder("완료됨", 1);
            reminderService.toggleComplete(completed.getId(), member.getId());

            mockMvc.perform(get("/api/reminders/completed").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("완료됨")));
        }
    }

    @Nested
    @DisplayName("GET /api/reminders/counts")
    class CountSmartListsTest {

        @Test
        @DisplayName("스마트 목록 카운트를 반환한다")
        void counts() throws Exception {
            reminderRepository.save(Reminder.builder().list(list).title("오늘")
                    .dueDate(LocalDate.now().atTime(9, 0)).sortOrder(0).build());
            reminderRepository.save(Reminder.builder().list(list).title("깃발")
                    .flagged(true).sortOrder(1).build());
            var completed = saveReminder("완료됨", 2);
            reminderService.toggleComplete(completed.getId(), member.getId());

            mockMvc.perform(get("/api/reminders/counts").with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.today", is(1)))
                    .andExpect(jsonPath("$.scheduled", is(1)))
                    .andExpect(jsonPath("$.all", is(2)))
                    .andExpect(jsonPath("$.flagged", is(1)))
                    .andExpect(jsonPath("$.completed", is(1)));
        }
    }
}
