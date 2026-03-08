package bamdul.ai.reminder.group.controller;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.group.domain.GroupPermission;
import bamdul.ai.reminder.group.domain.ReminderGroup;
import bamdul.ai.reminder.group.repository.GroupMemberRepository;
import bamdul.ai.reminder.group.repository.ReminderGroupRepository;
import bamdul.ai.reminder.group.service.port.in.GroupService;
import bamdul.ai.reminder.group.service.dto.CreateGroupCommand;
import bamdul.ai.reminder.group.service.dto.InviteMemberCommand;
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
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReminderGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private ReminderListRepository listRepository;

    @Autowired
    private GroupService groupService;

    private Member owner;
    private Member other;

    @BeforeEach
    void setUp() {
        listRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        memberRepository.deleteAll();
        owner = memberRepository.save(Member.builder()
                .email("owner@example.com").password("encoded").name("소유자").build());
        other = memberRepository.save(Member.builder()
                .email("other@example.com").password("encoded").name("다른 사용자").build());
    }

    private RequestPostProcessor authenticated(Member member) {
        return authentication(new UsernamePasswordAuthenticationToken(member.getId(), null, List.of()));
    }

    @Nested
    @DisplayName("POST /api/groups")
    class CreateTest {

        @Test
        @DisplayName("그룹을 생성하고 201을 반환한다")
        void create() throws Exception {
            mockMvc.perform(post("/api/groups")
                            .with(authenticated(owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "팀 프로젝트"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name", is("팀 프로젝트")))
                    .andExpect(jsonPath("$.ownerId", is(owner.getId().intValue())));
        }
    }

    @Nested
    @DisplayName("GET /api/groups")
    class FindAllTest {

        @Test
        @DisplayName("소유 및 소속 그룹을 조회한다")
        void findAll() throws Exception {
            groupService.create(new CreateGroupCommand("내 그룹"), owner.getId());

            mockMvc.perform(get("/api/groups").with(authenticated(owner)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("PUT /api/groups/{id}")
    class UpdateTest {

        @Test
        @DisplayName("그룹 이름을 수정한다")
        void update() throws Exception {
            var group = groupService.create(new CreateGroupCommand("원래 이름"), owner.getId());

            mockMvc.perform(put("/api/groups/{id}", group.id())
                            .with(authenticated(owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "새 이름"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("새 이름")));
        }

        @Test
        @DisplayName("소유자가 아니면 403을 반환한다")
        void updateForbidden() throws Exception {
            var group = groupService.create(new CreateGroupCommand("그룹"), owner.getId());

            mockMvc.perform(put("/api/groups/{id}", group.id())
                            .with(authenticated(other))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "새 이름"}
                                    """))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/groups/{id}")
    class DeleteTest {

        @Test
        @DisplayName("그룹을 삭제하고 204를 반환한다")
        void deleteGroup() throws Exception {
            var group = groupService.create(new CreateGroupCommand("삭제할 그룹"), owner.getId());

            mockMvc.perform(delete("/api/groups/{id}", group.id())
                            .with(authenticated(owner)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("POST /api/groups/{id}/members")
    class InviteMemberTest {

        @Test
        @DisplayName("그룹원을 초대하고 201을 반환한다")
        void invite() throws Exception {
            var group = groupService.create(new CreateGroupCommand("그룹"), owner.getId());

            mockMvc.perform(post("/api/groups/{id}/members", group.id())
                            .with(authenticated(owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "other@example.com", "permission": "READ_WRITE"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.memberId", is(other.getId().intValue())))
                    .andExpect(jsonPath("$.permission", is("READ_WRITE")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/groups/{id}/members/{memberId}")
    class RemoveMemberTest {

        @Test
        @DisplayName("그룹원을 강퇴하고 204를 반환한다")
        void remove() throws Exception {
            var group = groupService.create(new CreateGroupCommand("그룹"), owner.getId());
            groupService.inviteMember(group.id(), new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId());

            mockMvc.perform(delete("/api/groups/{id}/members/{memberId}", group.id(), other.getId())
                            .with(authenticated(owner)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PATCH /api/groups/{id}/members/{memberId}/permission")
    class UpdatePermissionTest {

        @Test
        @DisplayName("그룹원 권한을 변경한다")
        void updatePermission() throws Exception {
            var group = groupService.create(new CreateGroupCommand("그룹"), owner.getId());
            groupService.inviteMember(group.id(), new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId());

            mockMvc.perform(patch("/api/groups/{id}/members/{memberId}/permission", group.id(), other.getId())
                            .with(authenticated(owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"permission": "READ_WRITE"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.permission", is("READ_WRITE")));
        }
    }
}
