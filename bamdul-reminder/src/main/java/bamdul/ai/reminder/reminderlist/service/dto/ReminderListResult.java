package bamdul.ai.reminder.reminderlist.service.dto;

import bamdul.ai.reminder.reminderlist.domain.ReminderList;

import java.time.LocalDateTime;

public record ReminderListResult(
        Long id,
        String name,
        String color,
        String icon,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReminderListResult from(ReminderList entity) {
        return new ReminderListResult(
                entity.getId(),
                entity.getName(),
                entity.getColor(),
                entity.getIcon(),
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
