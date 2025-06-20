package org.kirya343.main.model.DTOs;

import lombok.Data;

@Data
public class NotificationDTO {
    private String title;
    private String message;
    private String url;
    private Long conversationId;

    public NotificationDTO(String title, String message, String url, Long conversationId) {
        this.title = title;
        this.message = message;
        this.url = url;
        this.conversationId = conversationId;
    }
}