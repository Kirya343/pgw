package org.kirya343.main.controller.chat;

import jakarta.transaction.Transactional;
import org.kirya343.main.model.chat.*;
import org.kirya343.main.model.User;
import org.kirya343.main.services.AvatarService;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private AvatarService interlocutorAvatar;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO messageDTO, Principal principal) throws AccessDeniedException {
        // Получаем отправителя
        User sender = userService.findBySub(principal.getName());
        // Получаем беседу
        Conversation conversation = chatService.getConversationById(messageDTO.getConversationId());



        // Сохраняем сообщение в базу
        Message message = chatService.sendMessage(conversation, sender, messageDTO.getText());

        // Отправляем сообщение всем подписчикам
        messagingTemplate.convertAndSend("/topic/messages/" + messageDTO.getConversationId(),
                new MessageDTO(
                        message.getId(),
                        message.getText(),
                        message.getSentAt(),
                        message.getSender().getId(),
                        message.getReceiver().getId(),
                        message.getConversation().getId(),
                        message.isOwn(sender)
                )
        );
    }

    @MessageMapping("/chat.loadMessages/{conversationId}")
    @SendTo("/topic/history.messages/{conversationId}")
    public List<MessageDTO> loadMessagesForConversation(@DestinationVariable Long conversationId, Principal principal) {
        logger.info("Получение сообщений для разговора с ID: {}", conversationId);

        User currentUser = userService.findBySub(principal.getName());

        // Получаем разговор по ID
        Conversation conversation = chatService.getConversationById(conversationId);
        if (conversation == null) {
            throw new AccessDeniedException("Conversation not found");
        }

        // Получаем все сообщения для этого разговора
        List<Message> messages = chatService.getMessages(conversation);

        // Преобразуем сообщения в DTO и отправляем клиенту
        return messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getId(),
                        msg.getText(),
                        msg.getSentAt(),
                        msg.getSender().getId(),
                        conversation.getId(),
                        msg.getReceiver().getId(),
                        msg.getSender().equals(currentUser) // Проверка на владельца сообщения
                ))
                .collect(Collectors.toList());
    }

    @MessageMapping("/chat.markAsRead")
    public void markAsRead(MarkAsReadDTO markAsReadDTO, Principal principal) {
        User user = userService.findBySub(principal.getName());
        Long conversationId = markAsReadDTO.getConversationId();

        chatService.markMessagesAsRead(conversationId, user);
    }
    @MessageMapping("/getConversations")
    @SendToUser("/queue/conversations")
    public List<ConversationDTO> getConversations(Principal principal) {
        User user = userService.findBySub(principal.getName());
        List<Conversation> conversations = chatService.getUserConversations(user);
        return conversations.stream()
                .map(conv -> chatMapper.convertToDTO(conv, user))
                .toList();
    }

    @Transactional
    @MessageMapping("/chat.getInterlocutorInfo")
    @SendToUser("/queue/interlocutorInfo")
    public UserDTO getInterlocutorInfo(ConversationRequest request, Principal principal) {
        User currentUser = userService.findBySub(principal.getName());
        Long conversationId = request.getConversationId();

        Conversation conversation = chatService.getConversationById(conversationId);
        if (conversation == null) {
            throw new AccessDeniedException("No access to this conversation");
        }

        User interlocutorName = conversation.getOtherParticipant(currentUser);
        if (interlocutorName == null) {
            throw new AccessDeniedException("No access to this conversation");
        }

        return new UserDTO(interlocutorName, interlocutorAvatar);
    }
}