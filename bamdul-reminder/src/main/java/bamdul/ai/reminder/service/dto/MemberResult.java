package bamdul.ai.reminder.service.dto;

import bamdul.ai.reminder.domain.Member;

import java.time.LocalDateTime;

public record MemberResult(
        Long id,
        String email,
        String name,
        LocalDateTime createdAt
) {
    public static MemberResult from(Member member) {
        return new MemberResult(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getCreatedAt()
        );
    }
}
