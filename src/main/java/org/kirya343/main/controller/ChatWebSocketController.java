package org.kirya343.main.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.kirya343.main.controller.mappers.ChatMapper;
import org.kirya343.main.model.DTOs.*;
import org.kirya343.main.model.chat.*;
import org.kirya343.main.model.User;
import org.kirya343.main.services.NotificationService;
import org.kirya343.main.services.chat.ChatService;
import org.kirya343.main.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final ChatService chatService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO messageDTO, Principal principal) throws AccessDeniedException {

        User sender = userService.findBySub(principal.getName());

        Conversation conversation = chatService.getConversationById(messageDTO.getConversationId());

        Message message = chatService.sendMessage(conversation, sender, messageDTO.getText());

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

        // Отправка уведомления получателю
        User receiver = message.getReceiver();

        NotificationDTO notification = new NotificationDTO(
                "Новое сообщение",
                sender.getName() + ": " + message.getText(),
                "/secure/messengerconversationId=" + conversation.getId()
        );

        chatService.notifyConversationUpdate(message.getConversation().getId(), sender);
        chatService.notifyConversationUpdate(message.getConversation().getId(), receiver);

        if (isUserOnline(receiver)) {
            messagingTemplate.convertAndSendToUser(
                    receiver.getSub(),
                    "/queue/notifications",
                    notification
            );
        } else {
            notificationService.saveOfflineChatNotification(receiver.getSub(), notification);
        }
    }

    // Проверка активности пользователя
    private boolean isUserOnline(User user) {
        return simpUserRegistry.getUser(user.getSub()) != null;
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
        // Уведомляем об обновлении
        chatService.notifyConversationUpdate(markAsReadDTO.getConversationId(), user);
    }

    @MessageMapping("/getConversations")
    public void getConversations(Principal principal) {
        User user = userService.findBySub(principal.getName());
        List<Conversation> conversations = chatService.getUserConversations(user);

        conversations.stream()
            .sorted((c1, c2) -> {
                LocalDateTime date1 = c1.getLastMessage() != null ? c1.getLastMessage().getSentAt() : c1.getCreatedAt();
                LocalDateTime date2 = c2.getLastMessage() != null ? c2.getLastMessage().getSentAt() : c2.getCreatedAt();
                return date2.compareTo(date1);
            })
            .map(conv -> chatMapper.convertToDTO(conv, user))
            .forEach(dto -> {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/conversations",
                    dto
                );
            });
    }

    @Transactional
    @MessageMapping("/chat.getInterlocutorInfo")
    @SendToUser("/queue/interlocutorInfo")
    public InterlocutorInfoDTO getInterlocutorInfo(ConversationRequest request, Principal principal) {
        User currentUser = userService.findBySub(principal.getName());
        Long conversationId = request.getConversationId();

        Conversation conversation = chatService.getConversationById(conversationId);
        if (conversation == null) {
            throw new AccessDeniedException("No access to this conversation");
        }

        User interlocutor = conversation.getOtherParticipant(currentUser);
        if (interlocutor == null) {
            throw new AccessDeniedException("No access to this conversation");
        }
        String avatar = interlocutor.getAvatarUrl() != null ? interlocutor.getAvatarUrl() : "/images/avatar-placeholder.png";

        return new InterlocutorInfoDTO(interlocutor.getName(), avatar);
    }

    /* @MessageMapping("/chat.subscribeToConversations")
    @SendToUser("/queue/conversations.updates")
    public ConversationDTO subscribeToConversations(Principal principal) {
        User user = userService.findBySub(principal.getName());
        // Можно сразу вернуть текущий список или просто подтвердить подписку
        return null; // Реальная логика будет в сервисе
    } */
}