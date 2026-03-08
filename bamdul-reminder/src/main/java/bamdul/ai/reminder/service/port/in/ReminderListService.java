package bamdul.ai.reminder.service.port.in;

import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;

import java.util.List;

public interface ReminderListService {

    List<ReminderListResult> findAll(Long memberId);

    ReminderListResult findById(Long id, Long memberId);

    ReminderListResult create(CreateReminderListCommand command, Long memberId);

    ReminderListResult update(Long id, UpdateReminderListCommand command, Long memberId);

    void delete(Long id, Long memberId);

    void reorder(ReorderCommand command, Long memberId);
}
