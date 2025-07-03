package org.kirya343.main.model;

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

    @ManyToOne
    @JoinColumn(name = "executor_id")
    private User executor;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    public enum Status {
        NEW,
        IN_PROGRESS,
        COMPLETED,
        CANCELED
    }

    public enum TaskType {
        DEVELOPMENT,         // Разработка
        CONTENT_UPDATE,      // Дополнение контента
        MODERATION,          // Модерация
        DESIGN,              // Дизайн
        TESTING,             // Тестирование
        MAINTENANCE,         // Техническое обслуживание
        SUPPORT              // Поддержка пользователей
    }

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private TaskType taskType;
}
