package org.workswap.datasource.main.model.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class PersistentNotification {
    @Id
    @GeneratedValue
    private Long id;
    private String userId;
    private String title;
    private String message;
    private String url;
    private Long conversationId;
    private LocalDateTime created = LocalDateTime.now();
    private boolean isRead = false;
    // + getters/setters

    @PrePersist
    public void prePersist() {
        if (created == null) {
            created = LocalDateTime.now();
        }
    }
}