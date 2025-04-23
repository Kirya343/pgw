package org.kirya343.main.model.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class MessageDTO {
    private Long id;
    private String text;
    private LocalDateTime sentAt;
    private Long senderId;
    private Long conversationId;
    private Long receiverId;
    private boolean isOwn;

    public MessageDTO(Long id, String text, LocalDateTime sentAt, Long senderId, Long conversationId, Long receiverId, boolean isOwn) {
        this.id = id;
        this.text = text;
        this.sentAt = sentAt;
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.receiverId = receiverId;
        this.isOwn = isOwn;
    }
}