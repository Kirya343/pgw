package org.kirya343.main.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 2000)
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private LocalDateTime completed;

    public enum Status {
        NEW("Новая"),
        IN_PROGRESS("В процессе"),
        COMPLETED("Завершена"),
        CANCELED("Отменена");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TaskType {
        DEVELOPMENT("Разработка"),
        CONTENT_UPDATE("Дополнение контента"),
        MODERATION("Модерация"),
        DESIGN("Дизайн"),
        TESTING("Тестирование"),
        MAINTENANCE("Обслуживание"),
        SUPPORT("Поддержка");

        private final String displayName;

        TaskType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @ManyToOne
    private User executor;

    @ManyToOne
    private User author;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments = new ArrayList<>(); 

    @Transient // чтобы Hibernate не пытался сохранять это поле в БД
    public Duration getDuration() {
        if (createdAt != null && deadline != null) {
            return Duration.between(createdAt, deadline);
        }
        return null;
    }
}
