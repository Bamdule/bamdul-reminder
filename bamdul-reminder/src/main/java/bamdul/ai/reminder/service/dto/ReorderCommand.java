package bamdul.ai.reminder.service.dto;

import java.util.List;

public record ReorderCommand(
        List<Long> ids
) {}
