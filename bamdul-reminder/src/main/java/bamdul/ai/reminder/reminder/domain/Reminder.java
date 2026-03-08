package bamdul.ai.reminder.reminder.domain;

import bamdul.ai.reminder.reminderlist.domain.ReminderList;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private ReminderList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reminder parent;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    private Boolean completed = false;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.NONE;

    @Column(nullable = false)
    private Boolean flagged = false;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Reminder(ReminderList list, Reminder parent, String title, String notes,
                    LocalDateTime dueDate, Priority priority, Boolean flagged, Integer sortOrder) {
        var now = LocalDateTime.now();
        this.list = list;
        this.parent = parent;
        this.title = title;
        this.notes = notes;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : Priority.NONE;
        this.flagged = flagged != null ? flagged : false;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.completed = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String title, String notes, LocalDateTime dueDate, Priority priority, Boolean flagged) {
        this.title = title;
        this.notes = notes;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : Priority.NONE;
        this.flagged = flagged != null ? flagged : false;
        this.updatedAt = LocalDateTime.now();
    }

    public void toggleComplete() {
        this.completed = !this.completed;
        this.completedAt = this.completed ? LocalDateTime.now() : null;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
        this.updatedAt = LocalDateTime.now();
    }
}
