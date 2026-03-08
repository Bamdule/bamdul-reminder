package bamdul.ai.reminder.reminderlist.service.dto;

public record UpdateReminderListCommand(
        String name,
        String color,
        String icon
) {}
