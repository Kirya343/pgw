package org.kirya343.main.model.chat;

import lombok.Getter;
import lombok.Setter;
import org.kirya343.main.model.Listing;

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
    private ListingDTO listing;
}
