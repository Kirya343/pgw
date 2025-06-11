package org.kirya343.main.controller.mappers;

import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.DTOs.ConversationDTO;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.services.components.AvatarService;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ChatMapper {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private ListingMapper listingMapper;

    @Autowired
    @Lazy
    private ChatService chatService;

    public ConversationDTO convertToDTO(Conversation conversation, User currentUser) {
        User interlocutor = conversation.getOtherParticipant(currentUser);
        User freshInterlocutor = userService.findById(interlocutor.getId());

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setInterlocutorName(freshInterlocutor.getName());
        dto.setInterlocutorAvatar(avatarService.resolveAvatarPath(freshInterlocutor));
        dto.setUnreadCount(chatService.getUnreadMessageCount(conversation, currentUser));

        // Проверка на наличие привязанного объявления
        if (conversation.getListing() != null) {
            dto.setListing(listingMapper.convertToDTO(conversation.getListing()));
        } else {
            dto.setListing(null);
        }

        // Обработка последнего сообщения
        Message lastMessage = conversation.getLastMessage();
        if (lastMessage != null) {
            dto.setLastMessagePreview(lastMessage.getText());
            dto.setLastMessageTime(lastMessage.getSentAt());
            dto.setFormattedLastMessageTime(
                    lastMessage.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm"))
            );
        } else {
            dto.setLastMessageTime(conversation.getCreatedAt());
        }

        // Определяем, есть ли новые сообщения
        boolean hasNewMessage = conversation.getMessages().stream()
                .filter(msg -> msg.getReceiver().equals(currentUser))
                .anyMatch(msg -> !msg.isRead());
        dto.setHasNewMessage(hasNewMessage);

        return dto;
    }
}