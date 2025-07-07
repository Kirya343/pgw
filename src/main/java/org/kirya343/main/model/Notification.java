package org.kirya343.main.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="recipient_id")
    private User recipient;

    private boolean isRead = false;
    
    private String title;

    @Column(length = 2000)
    private String content;

    private String link;

    public enum NotificationType{
        SYSTEM("Системное"),
        CHAT("Чат");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public enum Importance {
        INFO,
        WARNING,
        ERROR
    }

    @Enumerated(EnumType.STRING)
    private Importance importance;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
