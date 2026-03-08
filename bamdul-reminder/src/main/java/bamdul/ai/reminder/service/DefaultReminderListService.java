package bamdul.ai.reminder.service;

import bamdul.ai.reminder.domain.Member;
import bamdul.ai.reminder.domain.ReminderList;
import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;
import bamdul.ai.reminder.exception.ResourceNotFoundException;
import bamdul.ai.reminder.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Override
    public List<ReminderListResult> findAll(Long memberId) {
        return repository.findAllByMemberIdOrderBySortOrderAsc(memberId).stream()
                .map(ReminderListResult::from)
                .toList();
    }

    @Override
    public ReminderListResult findById(Long id, Long memberId) {
        return ReminderListResult.from(getByIdAndMemberId(id, memberId));
    }

    @Override
    @Transactional
    public ReminderListResult create(CreateReminderListCommand command, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
        ReminderList entity = command.toEntity(member);
        return ReminderListResult.from(repository.save(entity));
    }

    @Override
    @Transactional
    public ReminderListResult update(Long id, UpdateReminderListCommand command, Long memberId) {
        ReminderList entity = getByIdAndMemberId(id, memberId);
        entity.update(command.name(), command.color(), command.icon());
        return ReminderListResult.from(entity);
    }

    @Override
    @Transactional
    public void delete(Long id, Long memberId) {
        ReminderList entity = getByIdAndMemberId(id, memberId);
        repository.delete(entity);
    }

    @Override
    @Transactional
    public void reorder(ReorderCommand command, Long memberId) {
        List<Long> ids = command.ids();
        for (int i = 0; i < ids.size(); i++) {
            ReminderList entity = getByIdAndMemberId(ids.get(i), memberId);
            entity.updateSortOrder(i);
        }
    }

    private ReminderList getByIdAndMemberId(Long id, Long memberId) {
        return repository.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("ReminderList", id));
    }
}
