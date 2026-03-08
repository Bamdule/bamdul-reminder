package bamdul.ai.reminder.repository;

import bamdul.ai.reminder.domain.ReminderList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderListRepository extends JpaRepository<ReminderList, Long> {

    List<ReminderList> findAllByOrderBySortOrderAsc();
}
