package bamdul.ai.reminder.group.service.dto;

import bamdul.ai.reminder.group.domain.ReminderGroup;

import java.time.LocalDateTime;

public record GroupResult(
        Long id,
        String name,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GroupResult from(ReminderGroup entity) {
        return new GroupResult(
                entity.getId(),
                entity.getName(),
                entity.getOwner().getId(),
                entity.getOwner().getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
