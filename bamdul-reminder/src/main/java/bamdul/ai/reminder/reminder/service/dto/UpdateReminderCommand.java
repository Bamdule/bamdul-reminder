package bamdul.ai.reminder.reminder.service.dto;

import bamdul.ai.reminder.reminder.domain.Priority;

import java.time.LocalDateTime;

public record UpdateReminderCommand(
        String title,
        String notes,
        LocalDateTime dueDate,
        Priority priority,
        Boolean flagged
) {}
