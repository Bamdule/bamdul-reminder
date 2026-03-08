package bamdul.ai.reminder.reminder.service.port.in;

import bamdul.ai.reminder.reminder.service.dto.CreateReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.ReminderResult;
import bamdul.ai.reminder.reminder.service.dto.ReorderReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.UpdateReminderCommand;

import java.util.List;

public interface ReminderService {

    List<ReminderResult> findAllByListId(Long listId, Long memberId);

    ReminderResult findById(Long id, Long memberId);

    ReminderResult create(Long listId, CreateReminderCommand command, Long memberId);

    ReminderResult update(Long id, UpdateReminderCommand command, Long memberId);

    void delete(Long id, Long memberId);

    ReminderResult toggleComplete(Long id, Long memberId);

    void reorder(ReorderReminderCommand command, Long memberId);
}
