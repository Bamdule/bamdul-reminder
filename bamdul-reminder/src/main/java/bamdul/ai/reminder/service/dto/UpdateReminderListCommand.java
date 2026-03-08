package bamdul.ai.reminder.service.dto;

public record UpdateReminderListCommand(
        String name,
        String color,
        String icon
) {}
