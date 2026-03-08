package bamdul.ai.reminder.service;

import bamdul.ai.reminder.domain.ReminderList;
import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;
import bamdul.ai.reminder.exception.ResourceNotFoundException;
import bamdul.ai.reminder.repository.ReminderListRepository;
import bamdul.ai.reminder.service.port.in.ReminderListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultReminderListService implements ReminderListService {

    private final ReminderListRepository repository;

    @Override
    public List<ReminderListResult> findAll() {
        return repository.findAllByOrderBySortOrderAsc().stream()
                .map(ReminderListResult::from)
                .toList();
    }

    @Override
    public ReminderListResult findById(Long id) {
        return ReminderListResult.from(getById(id));
    }

    @Override
    @Transactional
    public ReminderListResult create(CreateReminderListCommand command) {
        ReminderList entity = command.toEntity();
        return ReminderListResult.from(repository.save(entity));
    }

    @Override
    @Transactional
    public ReminderListResult update(Long id, UpdateReminderListCommand command) {
        ReminderList entity = getById(id);
        entity.update(command.name(), command.color(), command.icon());
        return ReminderListResult.from(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ReminderList entity = getById(id);
        repository.delete(entity);
    }

    @Override
    @Transactional
    public void reorder(ReorderCommand command) {
        List<Long> ids = command.ids();
        for (int i = 0; i < ids.size(); i++) {
            ReminderList entity = getById(ids.get(i));
            entity.updateSortOrder(i);
        }
    }

    private ReminderList getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderList", id));
    }
}
