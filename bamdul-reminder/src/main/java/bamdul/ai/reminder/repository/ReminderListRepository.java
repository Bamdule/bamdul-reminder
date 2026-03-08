package bamdul.ai.reminder.repository;

import bamdul.ai.reminder.domain.ReminderList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReminderListRepository extends JpaRepository<ReminderList, Long> {

    List<ReminderList> findAllByOrderBySortOrderAsc();

    List<ReminderList> findAllByMemberIdOrderBySortOrderAsc(Long memberId);

    Optional<ReminderList> findByIdAndMemberId(Long id, Long memberId);
}
