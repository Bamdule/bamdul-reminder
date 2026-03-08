package bamdul.ai.reminder.reminder.service.dto;

public record SmartListCountResult(
        long today,
        long scheduled,
        long all,
        long flagged,
        long completed
) {}
