package org.kirya343.main.controller.chat;

import org.kirya343.main.model.User;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.chat.ConversationDTO;
import org.kirya343.main.model.chat.ListingDTO;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ChatMapper {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private ChatService chatService;


    public ConversationDTO convertToDTO(Conversation conversation, User currentUser) {
        User interlocutor = conversation.getOtherParticipant(currentUser);
        User freshInterlocutor = userService.findById(interlocutor.getId());

        ListingDTO listingDTO = new ListingDTO();
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setInterlocutorName(freshInterlocutor.getName());
        dto.setInterlocutorAvatar(avatarService.resolveAvatarPath(freshInterlocutor));
        dto.setUnreadCount(chatService.getUnreadMessageCount(conversation, currentUser));
        dto.setListing(listingDTO.convertToListingDTO(conversation.getListing()));

        if (!conversation.getMessages().isEmpty()) {
            Message lastMessage = conversation.getMessages().get(conversation.getMessages().size() - 1);
            dto.setLastMessagePreview(lastMessage.getText());
            dto.setLastMessageTime(lastMessage.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        return dto;
    }

}
