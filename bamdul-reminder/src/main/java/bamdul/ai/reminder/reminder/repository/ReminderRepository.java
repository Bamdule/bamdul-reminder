package bamdul.ai.reminder.reminder.repository;

import bamdul.ai.reminder.reminder.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findAllByListIdAndParentIsNullOrderBySortOrderAsc(Long listId);

    Optional<Reminder> findByIdAndListMemberId(Long id, Long memberId);

    List<Reminder> findAllByParentIdOrderBySortOrderAsc(Long parentId);

    List<Reminder> findAllByListMemberIdAndCompletedFalseOrderBySortOrderAsc(Long memberId);

    List<Reminder> findAllByListMemberIdAndDueDateBetweenAndCompletedFalseOrderByDueDateAsc(
            Long memberId, LocalDateTime start, LocalDateTime end);

    List<Reminder> findAllByListMemberIdAndDueDateIsNotNullAndCompletedFalseOrderByDueDateAsc(Long memberId);

    List<Reminder> findAllByListMemberIdAndFlaggedTrueAndCompletedFalseOrderBySortOrderAsc(Long memberId);

    List<Reminder> findAllByListMemberIdAndCompletedTrueOrderByCompletedAtDesc(Long memberId);

    List<Reminder> findAllByListMemberIdAndTitleContainingIgnoreCaseOrderBySortOrderAsc(Long memberId, String keyword);
}
