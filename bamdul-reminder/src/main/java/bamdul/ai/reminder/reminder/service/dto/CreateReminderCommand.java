package bamdul.ai.reminder.reminder.service.dto;

import bamdul.ai.reminder.reminder.domain.Priority;
import bamdul.ai.reminder.reminder.domain.Reminder;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;

import java.time.LocalDateTime;

public record CreateReminderCommand(
        String title,
        String notes,
        LocalDateTime dueDate,
        Priority priority,
        Boolean flagged,
        Integer sortOrder,
        Long parentId
) {
    public Reminder toEntity(ReminderList list, Reminder parent) {
        return Reminder.builder()
                .list(list)
                .parent(parent)
                .title(title)
                .notes(notes)
                .dueDate(dueDate)
                .priority(priority)
                .flagged(flagged)
                .sortOrder(sortOrder)
                .build();
    }
}
