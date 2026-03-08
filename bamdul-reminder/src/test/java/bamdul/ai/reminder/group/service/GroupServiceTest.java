package bamdul.ai.reminder.group.service;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.global.exception.ResourceNotFoundException;
import bamdul.ai.reminder.group.domain.GroupPermission;
import bamdul.ai.reminder.group.exception.GroupAccessDeniedException;
import bamdul.ai.reminder.group.repository.GroupMemberRepository;
import bamdul.ai.reminder.group.repository.ReminderGroupRepository;
import bamdul.ai.reminder.group.service.dto.CreateGroupCommand;
import bamdul.ai.reminder.group.service.dto.InviteMemberCommand;
import bamdul.ai.reminder.group.service.dto.UpdateGroupCommand;
import bamdul.ai.reminder.group.service.dto.UpdatePermissionCommand;
import bamdul.ai.reminder.group.service.port.in.GroupService;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GroupServiceTest {

    @Autowired
    private GroupService service;

    @Autowired
    private ReminderGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReminderListRepository listRepository;

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

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("그룹을 생성한다")
        void create() {
            var result = service.create(new CreateGroupCommand("팀 프로젝트"), owner.getId());

            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo("팀 프로젝트");
            assertThat(result.ownerId()).isEqualTo(owner.getId());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTest {

        @Test
        @DisplayName("소유 및 소속 그룹을 모두 조회한다")
        void findAll() {
            service.create(new CreateGroupCommand("내 그룹"), owner.getId());
            var otherGroup = service.create(new CreateGroupCommand("다른 그룹"), other.getId());
            service.inviteMember(otherGroup.id(), new InviteMemberCommand("owner@example.com", GroupPermission.READ), other.getId());

            var result = service.findAll(owner.getId());

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("소유자가 그룹 이름을 수정한다")
        void update() {
            var group = service.create(new CreateGroupCommand("원래 이름"), owner.getId());

            var result = service.update(group.id(), new UpdateGroupCommand("새 이름"), owner.getId());

            assertThat(result.name()).isEqualTo("새 이름");
        }

        @Test
        @DisplayName("소유자가 아닌 회원은 수정할 수 없다")
        void updateNotOwner() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());

            assertThatThrownBy(() -> service.update(group.id(), new UpdateGroupCommand("새 이름"), other.getId()))
                    .isInstanceOf(GroupAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("소유자가 그룹을 삭제한다")
        void delete() {
            var group = service.create(new CreateGroupCommand("삭제할 그룹"), owner.getId());

            service.delete(group.id(), owner.getId());

            assertThat(groupRepository.findById(group.id())).isEmpty();
        }

        @Test
        @DisplayName("소유자가 아닌 회원은 삭제할 수 없다")
        void deleteNotOwner() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());

            assertThatThrownBy(() -> service.delete(group.id(), other.getId()))
                    .isInstanceOf(GroupAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("inviteMember")
    class InviteMemberTest {

        @Test
        @DisplayName("그룹원을 초대한다")
        void invite() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());

            var result = service.inviteMember(group.id(),
                    new InviteMemberCommand("other@example.com", GroupPermission.READ_WRITE), owner.getId());

            assertThat(result.memberId()).isEqualTo(other.getId());
            assertThat(result.permission()).isEqualTo(GroupPermission.READ_WRITE);
        }

        @Test
        @DisplayName("이미 가입된 회원을 초대하면 예외가 발생한다")
        void inviteDuplicate() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());
            service.inviteMember(group.id(), new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId());

            assertThatThrownBy(() -> service.inviteMember(group.id(),
                    new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId()))
                    .isInstanceOf(GroupAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMemberTest {

        @Test
        @DisplayName("그룹원을 강퇴한다")
        void remove() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());
            service.inviteMember(group.id(), new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId());

            service.removeMember(group.id(), other.getId(), owner.getId());

            assertThat(groupMemberRepository.existsByGroupIdAndMemberId(group.id(), other.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("updatePermission")
    class UpdatePermissionTest {

        @Test
        @DisplayName("그룹원 권한을 변경한다")
        void updatePermission() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());
            service.inviteMember(group.id(), new InviteMemberCommand("other@example.com", GroupPermission.READ), owner.getId());

            var result = service.updatePermission(group.id(), other.getId(),
                    new UpdatePermissionCommand(GroupPermission.READ_WRITE), owner.getId());

            assertThat(result.permission()).isEqualTo(GroupPermission.READ_WRITE);
        }
    }

    @Nested
    @DisplayName("findGroupLists")
    class FindGroupListsTest {

        @Test
        @DisplayName("그룹 목록을 조회한다")
        void findGroupLists() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());
            var groupEntity = groupRepository.findById(group.id()).orElseThrow();
            listRepository.save(ReminderList.builder()
                    .member(owner).group(groupEntity).name("그룹 목록").build());

            var result = service.findGroupLists(group.id(), owner.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("그룹 목록");
        }

        @Test
        @DisplayName("접근 권한이 없으면 예외가 발생한다")
        void findGroupListsNoAccess() {
            var group = service.create(new CreateGroupCommand("그룹"), owner.getId());
            var thirdMember = memberRepository.save(Member.builder()
                    .email("third@example.com").password("encoded").name("제3자").build());

            assertThatThrownBy(() -> service.findGroupLists(group.id(), thirdMember.getId()))
                    .isInstanceOf(GroupAccessDeniedException.class);
        }
    }
}
