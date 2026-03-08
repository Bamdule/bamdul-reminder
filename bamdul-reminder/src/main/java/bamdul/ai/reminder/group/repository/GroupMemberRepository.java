package bamdul.ai.reminder.group.repository;

import bamdul.ai.reminder.group.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findAllByMemberId(Long memberId);

    List<GroupMember> findAllByGroupId(Long groupId);

    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);
}
