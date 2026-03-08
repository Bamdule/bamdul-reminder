package bamdul.ai.reminder.auth.service.dto;

public record AuthResult(
        String token,
        MemberResult member
) {}
