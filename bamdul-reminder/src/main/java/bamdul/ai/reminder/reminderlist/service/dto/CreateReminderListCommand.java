package bamdul.ai.reminder.reminderlist.service.dto;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;

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
