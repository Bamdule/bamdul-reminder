package bamdul.ai.reminder.group.repository;

import bamdul.ai.reminder.group.domain.ReminderGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReminderGroupRepository extends JpaRepository<ReminderGroup, Long> {

    Optional<ReminderGroup> findByIdAndOwnerId(Long id, Long ownerId);
}
