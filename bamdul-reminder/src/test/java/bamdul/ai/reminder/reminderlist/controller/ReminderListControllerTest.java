package bamdul.ai.reminder.reminderlist.controller;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReminderListControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private RequestPostProcessor authenticated() {
        return authentication(new UsernamePasswordAuthenticationToken(member.getId(), null, List.of()));
    }

    private ReminderList saveReminderList(String name, Integer sortOrder) {
        return repository.save(ReminderList.builder().member(member).name(name).sortOrder(sortOrder).build());
    }

    @Nested
    @DisplayName("인증 없이 접근")
    class UnauthenticatedTest {

        @Test
        @DisplayName("인증 없이 /api/lists 접근 시 401을 반환한다")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/lists"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/lists")
    class FindAllTest {

        @Test
        @DisplayName("전체 목록을 sortOrder 순으로 반환한다")
        void findAll() throws Exception {
            saveReminderList("개인", 1);
            saveReminderList("업무", 0);

            mockMvc.perform(get("/api/lists")
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("업무")))
                    .andExpect(jsonPath("$[1].name", is("개인")));
        }

        @Test
        @DisplayName("목록이 없으면 빈 배열을 반환한다")
        void findAllEmpty() throws Exception {
            mockMvc.perform(get("/api/lists")
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/lists/{id}")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 목록을 조회한다")
        void findById() throws Exception {
            var saved = repository.save(ReminderList.builder()
                    .member(member).name("업무").color("#FF6B6B").build());

            mockMvc.perform(get("/api/lists/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("업무")))
                    .andExpect(jsonPath("$.color", is("#FF6B6B")));
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 404를 반환한다")
        void findByIdNotFound() throws Exception {
            mockMvc.perform(get("/api/lists/{id}", 999L)
                            .with(authenticated()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/lists")
    class CreateTest {

        @Test
        @DisplayName("새 목록을 생성하고 201을 반환한다")
        void create() throws Exception {
            mockMvc.perform(post("/api/lists")
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "업무", "color": "#FF6B6B", "icon": "briefcase", "sortOrder": 0}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name", is("업무")))
                    .andExpect(jsonPath("$.color", is("#FF6B6B")))
                    .andExpect(jsonPath("$.icon", is("briefcase")))
                    .andExpect(jsonPath("$.sortOrder", is(0)));
        }
    }

    @Nested
    @DisplayName("PUT /api/lists/{id}")
    class UpdateTest {

        @Test
        @DisplayName("목록을 수정하고 200을 반환한다")
        void update() throws Exception {
            var saved = repository.save(ReminderList.builder()
                    .member(member).name("업무").color("#FF6B6B").icon("briefcase").build());

            mockMvc.perform(put("/api/lists/{id}", saved.getId())
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "개인", "color": "#4A90D9", "icon": "person"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("개인")))
                    .andExpect(jsonPath("$.color", is("#4A90D9")))
                    .andExpect(jsonPath("$.icon", is("person")));
        }

        @Test
        @DisplayName("존재하지 않는 목록 수정 시 404를 반환한다")
        void updateNotFound() throws Exception {
            mockMvc.perform(put("/api/lists/{id}", 999L)
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "개인", "color": "#4A90D9", "icon": "person"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/lists/{id}")
    class DeleteTest {

        @Test
        @DisplayName("목록을 삭제하고 204를 반환한다")
        void deleteList() throws Exception {
            var saved = repository.save(ReminderList.builder().member(member).name("업무").build());

            mockMvc.perform(delete("/api/lists/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/lists/{id}", saved.getId())
                            .with(authenticated()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 목록 삭제 시 404를 반환한다")
        void deleteNotFound() throws Exception {
            mockMvc.perform(delete("/api/lists/{id}", 999L)
                            .with(authenticated()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/lists/reorder")
    class ReorderTest {

        @Test
        @DisplayName("목록 순서를 변경하고 204를 반환한다")
        void reorder() throws Exception {
            var list1 = saveReminderList("업무", 0);
            var list2 = saveReminderList("개인", 1);
            var list3 = saveReminderList("쇼핑", 2);

            mockMvc.perform(patch("/api/lists/reorder")
                            .with(authenticated())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"ids": [%d, %d, %d]}
                                    """.formatted(list3.getId(), list1.getId(), list2.getId())))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/lists")
                            .with(authenticated()))
                    .andExpect(jsonPath("$[0].name", is("쇼핑")))
                    .andExpect(jsonPath("$[1].name", is("업무")))
                    .andExpect(jsonPath("$[2].name", is("개인")));
        }
    }
}
