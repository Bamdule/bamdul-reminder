package bamdul.ai.reminder.group.service.dto;

import bamdul.ai.reminder.group.domain.GroupMember;
import bamdul.ai.reminder.group.domain.GroupPermission;

import java.time.LocalDateTime;

public record GroupMemberResult(
        Long id,
        Long memberId,
        String memberName,
        String memberEmail,
        GroupPermission permission,
        LocalDateTime joinedAt
) {
    public static GroupMemberResult from(GroupMember entity) {
        return new GroupMemberResult(
                entity.getId(),
                entity.getMember().getId(),
                entity.getMember().getName(),
                entity.getMember().getEmail(),
                entity.getPermission(),
                entity.getJoinedAt()
        );
    }
}
