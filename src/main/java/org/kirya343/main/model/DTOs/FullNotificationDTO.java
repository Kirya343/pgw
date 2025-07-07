package org.kirya343.main.model.DTOs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FullNotificationDTO {

    private Long id;
    private Long recipientId;
    private boolean isRead;
    private String title;
    private String content;
    private String link;
    private String type;
    private String importance;
    private LocalDateTime createdAt;
}
