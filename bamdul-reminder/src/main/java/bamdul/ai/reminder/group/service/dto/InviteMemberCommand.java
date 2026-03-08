package bamdul.ai.reminder.group.service.dto;

import bamdul.ai.reminder.group.domain.GroupPermission;

public record InviteMemberCommand(
        String email,
        GroupPermission permission
) {}
