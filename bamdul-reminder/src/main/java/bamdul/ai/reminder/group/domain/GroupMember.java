package bamdul.ai.reminder.group.domain;

import bamdul.ai.reminder.auth.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "member_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ReminderGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupPermission permission;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public GroupMember(ReminderGroup group, Member member, GroupPermission permission) {
        this.group = group;
        this.member = member;
        this.permission = permission;
        this.joinedAt = LocalDateTime.now();
    }

    public void updatePermission(GroupPermission permission) {
        this.permission = permission;
    }

    public boolean canWrite() {
        return permission == GroupPermission.READ_WRITE;
    }
}
