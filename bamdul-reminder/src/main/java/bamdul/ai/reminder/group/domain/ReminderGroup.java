package bamdul.ai.reminder.group.domain;

import bamdul.ai.reminder.auth.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReminderGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ReminderGroup(String name, Member owner) {
        var now = LocalDateTime.now();
        this.name = name;
        this.owner = owner;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOwner(Long memberId) {
        return owner.getId().equals(memberId);
    }
}
