package bamdul.ai.reminder.reminder.service.port.in;

import bamdul.ai.reminder.reminder.service.dto.CreateReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.ReminderResult;
import bamdul.ai.reminder.reminder.service.dto.ReorderReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.SmartListCountResult;
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

    // Smart lists
    List<ReminderResult> findToday(Long memberId);

    List<ReminderResult> findScheduled(Long memberId);

    List<ReminderResult> findAll(Long memberId);

    List<ReminderResult> findFlagged(Long memberId);

    List<ReminderResult> findCompleted(Long memberId);

    SmartListCountResult countSmartLists(Long memberId);

    List<ReminderResult> search(String keyword, Long memberId);
}
