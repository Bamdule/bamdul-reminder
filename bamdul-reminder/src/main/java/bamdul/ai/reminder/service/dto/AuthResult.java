package bamdul.ai.reminder.service.dto;

public record AuthResult(
        String token,
        MemberResult member
) {}
