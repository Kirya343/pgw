package org.workswap.datasource.main.model.DTOs;

public class MarkAsReadDTO {
    private Long conversationId;

    // геттеры и сеттеры
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}