package org.kirya343.main.model.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Новый DTO класс
@Setter
@Getter
public class ConversationDTO {
    private Long id;
    private String interlocutorName;
    private String interlocutorAvatar;
    private long unreadCount;
    private String lastMessagePreview;
    private LocalDateTime lastMessageTime;
    private String formattedLastMessageTime;
    private ListingDTO listing;
    private boolean hasNewMessage;

}
