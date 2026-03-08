package bamdul.ai.reminder.reminder.controller;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.reminder.domain.Reminder;
import bamdul.ai.reminder.reminder.repository.ReminderRepository;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
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
}
