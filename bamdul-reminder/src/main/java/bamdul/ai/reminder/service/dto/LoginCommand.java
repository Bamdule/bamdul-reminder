package bamdul.ai.reminder.service.dto;

public record LoginCommand(
        String email,
        String password
) {}
