package bamdul.ai.reminder.group.service.port.in;

import bamdul.ai.reminder.group.service.dto.CreateGroupCommand;
import bamdul.ai.reminder.group.service.dto.GroupMemberResult;
import bamdul.ai.reminder.group.service.dto.GroupResult;
import bamdul.ai.reminder.group.service.dto.InviteMemberCommand;
import bamdul.ai.reminder.group.service.dto.UpdateGroupCommand;
import bamdul.ai.reminder.group.service.dto.UpdatePermissionCommand;
import bamdul.ai.reminder.reminderlist.service.dto.ReminderListResult;

import java.util.List;

public interface GroupService {

    GroupResult create(CreateGroupCommand command, Long memberId);

    List<GroupResult> findAll(Long memberId);

    GroupResult findById(Long id, Long memberId);

    GroupResult update(Long id, UpdateGroupCommand command, Long memberId);

    void delete(Long id, Long memberId);

    // Group members
    GroupMemberResult inviteMember(Long groupId, InviteMemberCommand command, Long memberId);

    void removeMember(Long groupId, Long targetMemberId, Long memberId);

    GroupMemberResult updatePermission(Long groupId, Long targetMemberId, UpdatePermissionCommand command, Long memberId);

    List<GroupMemberResult> findMembers(Long groupId, Long memberId);

    // Group lists
    List<ReminderListResult> findGroupLists(Long groupId, Long memberId);
}
