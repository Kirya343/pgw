package org.kirya343.main.model;

import lombok.Getter;
import lombok.Setter;

// Новый DTO класс
@Setter
@Getter
public class ConversationDTO {
    private Long id;
    private String interlocutorName;
    private String interlocutorAvatar;
    private long unreadCount;
    private String lastMessagePreview;
    private String lastMessageTime;
    private Listing listing;
}
