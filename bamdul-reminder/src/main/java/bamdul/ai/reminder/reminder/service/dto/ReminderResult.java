package bamdul.ai.reminder.reminder.service.dto;

import bamdul.ai.reminder.reminder.domain.Priority;
import bamdul.ai.reminder.reminder.domain.Reminder;

import java.time.LocalDateTime;
import java.util.List;

public record ReminderResult(
        Long id,
        Long listId,
        Long parentId,
        String title,
        String notes,
        LocalDateTime dueDate,
        Boolean completed,
        LocalDateTime completedAt,
        Priority priority,
        Boolean flagged,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReminderResult> children
) {
    public static ReminderResult from(Reminder entity) {
        return new ReminderResult(
                entity.getId(),
                entity.getList().getId(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getTitle(),
                entity.getNotes(),
                entity.getDueDate(),
                entity.getCompleted(),
                entity.getCompletedAt(),
                entity.getPriority(),
                entity.getFlagged(),
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                List.of()
        );
    }

    public static ReminderResult fromWithChildren(Reminder entity, List<ReminderResult> children) {
        return new ReminderResult(
                entity.getId(),
                entity.getList().getId(),
                entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getTitle(),
                entity.getNotes(),
                entity.getDueDate(),
                entity.getCompleted(),
                entity.getCompletedAt(),
                entity.getPriority(),
                entity.getFlagged(),
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                children
        );
    }
}
