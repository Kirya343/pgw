package org.kirya343.main.model;

import java.time.Duration;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
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
    @JoinColumn(name = "executor_id")
    private User executor;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Transient // чтобы Hibernate не пытался сохранять это поле в БД
    public Duration getDuration() {
        if (createdAt != null && deadline != null) {
            return Duration.between(createdAt, deadline);
        }
        return null;
    }
}
