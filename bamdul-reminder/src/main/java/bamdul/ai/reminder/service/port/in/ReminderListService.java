package bamdul.ai.reminder.service.port.in;

import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;

import java.util.List;

public interface ReminderListService {

    List<ReminderListResult> findAll();

    ReminderListResult findById(Long id);

    ReminderListResult create(CreateReminderListCommand command);

    ReminderListResult update(Long id, UpdateReminderListCommand command);

    void delete(Long id);

    void reorder(ReorderCommand command);
}
