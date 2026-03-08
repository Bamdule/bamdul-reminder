package bamdul.ai.reminder.reminder.service.dto;

import java.util.List;

public record ReorderReminderCommand(
        List<Long> ids
) {}
