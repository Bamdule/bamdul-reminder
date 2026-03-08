package bamdul.ai.reminder.auth.service.dto;

public record LoginCommand(
        String email,
        String password
) {}
