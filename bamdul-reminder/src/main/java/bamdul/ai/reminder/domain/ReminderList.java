package bamdul.ai.reminder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_list")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReminderList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    private String icon;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public ReminderList(String name, String color, String icon, Integer sortOrder) {
        var now = LocalDateTime.now();
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, String color, String icon) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }
}
