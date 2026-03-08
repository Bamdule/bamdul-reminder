package bamdul.ai.reminder.group.service;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.global.exception.ResourceNotFoundException;
import bamdul.ai.reminder.group.domain.GroupMember;
import bamdul.ai.reminder.group.domain.ReminderGroup;
import bamdul.ai.reminder.group.exception.GroupAccessDeniedException;
import bamdul.ai.reminder.group.repository.GroupMemberRepository;
import bamdul.ai.reminder.group.repository.ReminderGroupRepository;
import bamdul.ai.reminder.group.service.dto.CreateGroupCommand;
import bamdul.ai.reminder.group.service.dto.GroupMemberResult;
import bamdul.ai.reminder.group.service.dto.GroupResult;
import bamdul.ai.reminder.group.service.dto.InviteMemberCommand;
import bamdul.ai.reminder.group.service.dto.UpdateGroupCommand;
import bamdul.ai.reminder.group.service.dto.UpdatePermissionCommand;
import bamdul.ai.reminder.group.service.port.in.GroupService;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import bamdul.ai.reminder.reminderlist.service.dto.ReminderListResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultGroupService implements GroupService {

    private final ReminderGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final ReminderListRepository reminderListRepository;

    @Override
    @Transactional
    public GroupResult create(CreateGroupCommand command, Long memberId) {
        Member owner = getMember(memberId);
        ReminderGroup group = groupRepository.save(ReminderGroup.builder()
                .name(command.name())
                .owner(owner)
                .build());
        return GroupResult.from(group);
    }

    @Override
    public List<GroupResult> findAll(Long memberId) {
        // Groups the member owns
        List<ReminderGroup> owned = groupRepository.findAll().stream()
                .filter(g -> g.isOwner(memberId))
                .toList();
        // Groups the member belongs to
        List<ReminderGroup> memberOf = groupMemberRepository.findAllByMemberId(memberId).stream()
                .map(GroupMember::getGroup)
                .toList();
        return Stream.concat(owned.stream(), memberOf.stream())
                .distinct()
                .map(GroupResult::from)
                .toList();
    }

    @Override
    public GroupResult findById(Long id, Long memberId) {
        ReminderGroup group = getGroupWithAccess(id, memberId);
        return GroupResult.from(group);
    }

    @Override
    @Transactional
    public GroupResult update(Long id, UpdateGroupCommand command, Long memberId) {
        ReminderGroup group = getGroupAsOwner(id, memberId);
        group.update(command.name());
        return GroupResult.from(group);
    }

    @Override
    @Transactional
    public void delete(Long id, Long memberId) {
        ReminderGroup group = getGroupAsOwner(id, memberId);
        groupMemberRepository.findAllByGroupId(id).forEach(groupMemberRepository::delete);
        groupRepository.delete(group);
    }

    @Override
    @Transactional
    public GroupMemberResult inviteMember(Long groupId, InviteMemberCommand command, Long memberId) {
        getGroupAsOwner(groupId, memberId);
        ReminderGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderGroup", groupId));
        Member target = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new ResourceNotFoundException("Member with email " + command.email()));
        if (group.isOwner(target.getId())) {
            throw new GroupAccessDeniedException("Cannot invite the group owner as a member");
        }
        if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, target.getId())) {
            throw new GroupAccessDeniedException("Member is already in the group");
        }
        GroupMember gm = groupMemberRepository.save(GroupMember.builder()
                .group(group)
                .member(target)
                .permission(command.permission())
                .build());
        return GroupMemberResult.from(gm);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long targetMemberId, Long memberId) {
        getGroupAsOwner(groupId, memberId);
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(groupId, targetMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", targetMemberId));
        groupMemberRepository.delete(gm);
    }

    @Override
    @Transactional
    public GroupMemberResult updatePermission(Long groupId, Long targetMemberId, UpdatePermissionCommand command, Long memberId) {
        getGroupAsOwner(groupId, memberId);
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(groupId, targetMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", targetMemberId));
        gm.updatePermission(command.permission());
        return GroupMemberResult.from(gm);
    }

    @Override
    public List<GroupMemberResult> findMembers(Long groupId, Long memberId) {
        getGroupWithAccess(groupId, memberId);
        return groupMemberRepository.findAllByGroupId(groupId).stream()
                .map(GroupMemberResult::from)
                .toList();
    }

    @Override
    public List<ReminderListResult> findGroupLists(Long groupId, Long memberId) {
        getGroupWithAccess(groupId, memberId);
        return reminderListRepository.findAllByGroupIdOrderBySortOrderAsc(groupId).stream()
                .map(ReminderListResult::from)
                .toList();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
    }

    private ReminderGroup getGroupAsOwner(Long groupId, Long memberId) {
        ReminderGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderGroup", groupId));
        if (!group.isOwner(memberId)) {
            throw new GroupAccessDeniedException("Only the group owner can perform this action");
        }
        return group;
    }

    private ReminderGroup getGroupWithAccess(Long groupId, Long memberId) {
        ReminderGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderGroup", groupId));
        if (!group.isOwner(memberId) && !groupMemberRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
            throw new GroupAccessDeniedException("You do not have access to this group");
        }
        return group;
    }
}
