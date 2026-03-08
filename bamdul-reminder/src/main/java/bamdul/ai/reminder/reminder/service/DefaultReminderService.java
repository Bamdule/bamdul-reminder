package bamdul.ai.reminder.reminder.service;

import bamdul.ai.reminder.global.exception.ResourceNotFoundException;
import bamdul.ai.reminder.reminder.domain.Reminder;
import bamdul.ai.reminder.reminder.repository.ReminderRepository;
import bamdul.ai.reminder.reminder.service.dto.CreateReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.ReminderResult;
import bamdul.ai.reminder.reminder.service.dto.ReorderReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.SmartListCountResult;
import bamdul.ai.reminder.reminder.service.dto.UpdateReminderCommand;
import bamdul.ai.reminder.reminder.service.port.in.ReminderService;
import bamdul.ai.reminder.reminderlist.domain.ReminderList;
import bamdul.ai.reminder.reminderlist.repository.ReminderListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultReminderService implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderListRepository reminderListRepository;

    @Override
    public List<ReminderResult> findAllByListId(Long listId, Long memberId) {
        getListByIdAndMemberId(listId, memberId);
        return reminderRepository.findAllByListIdAndParentIsNullOrderBySortOrderAsc(listId).stream()
                .map(this::toResultWithChildren)
                .toList();
    }

    @Override
    public ReminderResult findById(Long id, Long memberId) {
        return ReminderResult.from(getReminderByIdAndMemberId(id, memberId));
    }

    @Override
    @Transactional
    public ReminderResult create(Long listId, CreateReminderCommand command, Long memberId) {
        ReminderList list = getListByIdAndMemberId(listId, memberId);
        Reminder parent = null;
        if (command.parentId() != null) {
            parent = getReminderByIdAndMemberId(command.parentId(), memberId);
        }
        Reminder entity = command.toEntity(list, parent);
        return ReminderResult.from(reminderRepository.save(entity));
    }

    @Override
    @Transactional
    public ReminderResult update(Long id, UpdateReminderCommand command, Long memberId) {
        Reminder entity = getReminderByIdAndMemberId(id, memberId);
        entity.update(command.title(), command.notes(), command.dueDate(), command.priority(), command.flagged());
        return ReminderResult.from(entity);
    }

    @Override
    @Transactional
    public void delete(Long id, Long memberId) {
        Reminder entity = getReminderByIdAndMemberId(id, memberId);
        reminderRepository.delete(entity);
    }

    @Override
    @Transactional
    public ReminderResult toggleComplete(Long id, Long memberId) {
        Reminder entity = getReminderByIdAndMemberId(id, memberId);
        entity.toggleComplete();
        return ReminderResult.from(entity);
    }

    @Override
    @Transactional
    public void reorder(ReorderReminderCommand command, Long memberId) {
        List<Long> ids = command.ids();
        for (int i = 0; i < ids.size(); i++) {
            Reminder entity = getReminderByIdAndMemberId(ids.get(i), memberId);
            entity.updateSortOrder(i);
        }
    }

    @Override
    public List<ReminderResult> findToday(Long memberId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return reminderRepository.findAllByListMemberIdAndDueDateBetweenAndCompletedFalseOrderByDueDateAsc(
                memberId, startOfDay, endOfDay).stream().map(ReminderResult::from).toList();
    }

    @Override
    public List<ReminderResult> findScheduled(Long memberId) {
        return reminderRepository.findAllByListMemberIdAndDueDateIsNotNullAndCompletedFalseOrderByDueDateAsc(memberId)
                .stream().map(ReminderResult::from).toList();
    }

    @Override
    public List<ReminderResult> findAll(Long memberId) {
        return reminderRepository.findAllByListMemberIdAndCompletedFalseOrderBySortOrderAsc(memberId)
                .stream().map(ReminderResult::from).toList();
    }

    @Override
    public List<ReminderResult> findFlagged(Long memberId) {
        return reminderRepository.findAllByListMemberIdAndFlaggedTrueAndCompletedFalseOrderBySortOrderAsc(memberId)
                .stream().map(ReminderResult::from).toList();
    }

    @Override
    public List<ReminderResult> findCompleted(Long memberId) {
        return reminderRepository.findAllByListMemberIdAndCompletedTrueOrderByCompletedAtDesc(memberId)
                .stream().map(ReminderResult::from).toList();
    }

    @Override
    public SmartListCountResult countSmartLists(Long memberId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return new SmartListCountResult(
                reminderRepository.findAllByListMemberIdAndDueDateBetweenAndCompletedFalseOrderByDueDateAsc(memberId, startOfDay, endOfDay).size(),
                reminderRepository.findAllByListMemberIdAndDueDateIsNotNullAndCompletedFalseOrderByDueDateAsc(memberId).size(),
                reminderRepository.findAllByListMemberIdAndCompletedFalseOrderBySortOrderAsc(memberId).size(),
                reminderRepository.findAllByListMemberIdAndFlaggedTrueAndCompletedFalseOrderBySortOrderAsc(memberId).size(),
                reminderRepository.findAllByListMemberIdAndCompletedTrueOrderByCompletedAtDesc(memberId).size()
        );
    }

    @Override
    public List<ReminderResult> search(String keyword, Long memberId) {
        return reminderRepository.findAllByListMemberIdAndTitleContainingIgnoreCaseOrderBySortOrderAsc(memberId, keyword)
                .stream().map(ReminderResult::from).toList();
    }

    private ReminderList getListByIdAndMemberId(Long listId, Long memberId) {
        return reminderListRepository.findByIdAndMemberId(listId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderList", listId));
    }

    private Reminder getReminderByIdAndMemberId(Long id, Long memberId) {
        return reminderRepository.findByIdAndListMemberId(id, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", id));
    }

    private ReminderResult toResultWithChildren(Reminder entity) {
        List<ReminderResult> children = reminderRepository.findAllByParentIdOrderBySortOrderAsc(entity.getId())
                .stream()
                .map(this::toResultWithChildren)
                .toList();
        return ReminderResult.fromWithChildren(entity, children);
    }
}
