package org.workswap.main.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.*;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.chat.*;
import org.workswap.main.services.NotificationService;
import org.workswap.main.services.UserService;
import org.workswap.main.services.chat.ChatService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final ChatService chatService;
    private final UserService userService;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO messageDTO, Principal principal, @Header("locale") String lang) throws AccessDeniedException {
        Locale locale = Locale.of(lang);

        User sender = userService.findUser(principal.getName(), SearchParamType.ID);

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

        chatService.notifyConversationUpdate(message.getConversation().getId(), sender, locale);
        chatService.notifyConversationUpdate(message.getConversation().getId(), receiver, locale);

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

        User currentUser = userService.findUser(principal.getName(), SearchParamType.SUB);

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
    public void markAsRead(MarkAsReadDTO markAsReadDTO, Principal principal, @Header("locale") String lang) {
        Locale locale = Locale.of(lang);
        User user = userService.findUser(principal.getName(), SearchParamType.SUB);
        Long conversationId = markAsReadDTO.getConversationId();

        chatService.markMessagesAsRead(conversationId, user);
        // Уведомляем об обновлении
        chatService.notifyConversationUpdate(markAsReadDTO.getConversationId(), user, locale);
    }

    @MessageMapping("/getConversations")
    public void getConversations(Principal principal, @Header("locale") String lang) {
        Locale locale = Locale.of(lang);
        User user = userService.findUser(principal.getName(), SearchParamType.SUB);
        List<Conversation> conversations = chatService.getUserConversations(user);

        conversations.stream()
            .sorted((c1, c2) -> {
                LocalDateTime date1 = c1.getLastMessage() != null ? c1.getLastMessage().getSentAt() : c1.getCreatedAt();
                LocalDateTime date2 = c2.getLastMessage() != null ? c2.getLastMessage().getSentAt() : c2.getCreatedAt();
                return date2.compareTo(date1);
            })
            .map(conv -> chatService.convertToDTO(conv, user, locale))
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
        User currentUser = userService.findUser(principal.getName(), SearchParamType.SUB);
        Long conversationId = request.getConversationId();

        Conversation conversation = chatService.getConversationById(conversationId);
        if (conversation == null) {
            throw new AccessDeniedException("No access to this conversation");
        }

        User interlocutor = conversation.getInterlocutor(currentUser);
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