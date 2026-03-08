package bamdul.ai.reminder.service.dto;

import bamdul.ai.reminder.domain.Member;
import bamdul.ai.reminder.domain.ReminderList;

public record CreateReminderListCommand(
        String name,
        String color,
        String icon,
        Integer sortOrder
) {
    public ReminderList toEntity(Member member) {
        return ReminderList.builder()
                .member(member)
                .name(name)
                .color(color)
                .icon(icon)
                .sortOrder(sortOrder)
                .build();
    }
}
