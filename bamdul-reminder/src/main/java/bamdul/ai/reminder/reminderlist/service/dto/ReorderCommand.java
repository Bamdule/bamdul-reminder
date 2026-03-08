package bamdul.ai.reminder.reminderlist.service.dto;

import java.util.List;

public record ReorderCommand(
        List<Long> ids
) {}
