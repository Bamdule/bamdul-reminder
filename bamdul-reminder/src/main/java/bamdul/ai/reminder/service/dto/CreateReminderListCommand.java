package bamdul.ai.reminder.service.dto;

import bamdul.ai.reminder.domain.ReminderList;

public record CreateReminderListCommand(
        String name,
        String color,
        String icon,
        Integer sortOrder
) {
    public ReminderList toEntity() {
        return ReminderList.builder()
                .name(name)
                .color(color)
                .icon(icon)
                .sortOrder(sortOrder)
                .build();
    }
}
