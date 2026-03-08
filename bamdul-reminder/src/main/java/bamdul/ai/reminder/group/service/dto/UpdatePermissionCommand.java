package bamdul.ai.reminder.group.service.dto;

import bamdul.ai.reminder.group.domain.GroupPermission;

public record UpdatePermissionCommand(
        GroupPermission permission
) {}
