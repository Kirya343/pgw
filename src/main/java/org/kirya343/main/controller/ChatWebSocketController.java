package org.kirya343.main.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.kirya343.main.controller.mappers.ChatMapper;
import org.kirya343.main.model.DTOs.*;
import org.kirya343.main.model.chat.*;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.NotificationService;
import org.kirya343.main.services.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final ChatService chatService;
    private final UserService userService;
    private final ChatMapper chatMapper;
    private final NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO messageDTO, @AuthenticationPrincipal OAuth2User oauth2User) throws AccessDeniedException {
        // Получаем отправителя
        User sender = userService.findUserFromOAuth2(oauth2User);
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

        // Отправка уведомления получателю
        User receiver = message.getReceiver();
        String receiverSub = receiver.getSub();

        NotificationDTO notification = new NotificationDTO(
                "Новое сообщение",
                sender.getName() + ": " + message.getText(),
                "/chat/" + conversation.getId(),
                conversation.getId()
        );

        if (isUserOnline(receiverSub)) {
            messagingTemplate.convertAndSendToUser(
                    receiverSub,
                    "/queue/notifications",
                    notification
            );
        } else {
            notificationService.saveOfflineNotification(receiverSub, notification);
        }
    }

    // Проверка активности пользователя
    private boolean isUserOnline(String userSub) {
        return simpUserRegistry.getUser(userSub) != null;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal != null) {
            String username = principal.getName();
            List<NotificationDTO> notifications = notificationService.loadPendingNotifications(username);

            if (!notifications.isEmpty()) {
                notifications.forEach(notification -> {
                    messagingTemplate.convertAndSendToUser(
                            username,
                            "/queue/notifications",
                            notification
                    );
                });
                notificationService.clearNotifications(username);
            }
        }
    }

    @MessageMapping("/chat.loadMessages/{conversationId}")
    @SendTo("/topic/history.messages/{conversationId}")
    public List<MessageDTO> loadMessagesForConversation(@DestinationVariable Long conversationId, @AuthenticationPrincipal OAuth2User oauth2User) {

        User user = userService.findUserFromOAuth2(oauth2User);

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
                        msg.getSender().equals(user) // Проверка на владельца сообщения
                ))
                .collect(Collectors.toList());
    }

    @MessageMapping("/chat.markAsRead")
    public void markAsRead(MarkAsReadDTO markAsReadDTO, @AuthenticationPrincipal OAuth2User oauth2User) {
        User user = userService.findUserFromOAuth2(oauth2User);
        Long conversationId = markAsReadDTO.getConversationId();

        chatService.markMessagesAsRead(conversationId, user.getId());
        // Уведомляем об обновлении
        chatService.notifyConversationUpdate(markAsReadDTO.getConversationId(), user);
    }
    @MessageMapping("/getConversations")
    @SendToUser("/queue/conversations")
    public List<ConversationDTO> getConversations(Principal principal) {
        User user = userService.findBySub(principal.getName());
        List<Conversation> conversations = chatService.getUserConversations(user);

        // Сортируем по дате последнего сообщения (новые сверху)
        return conversations.stream()
                .sorted((c1, c2) -> {
                    LocalDateTime date1 = c1.getLastMessage() != null ? c1.getLastMessage().getSentAt() : c1.getCreatedAt();
                    LocalDateTime date2 = c2.getLastMessage() != null ? c2.getLastMessage().getSentAt() : c2.getCreatedAt();
                    return date2.compareTo(date1); // Сортировка по убыванию
                })
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
        String interlocutorAvatar = interlocutorName.getAvatarUrl() != null ? interlocutorName.getAvatarUrl() : "/images/avatar-placeholder.png";

        return new UserDTO(interlocutorName, interlocutorAvatar);
    }

    /* @MessageMapping("/chat.subscribeToConversations")
    @SendToUser("/queue/conversations.updates")
    public ConversationDTO subscribeToConversations(Principal principal) {
        User user = userService.findBySub(principal.getName());
        // Можно сразу вернуть текущий список или просто подтвердить подписку
        return null; // Реальная логика будет в сервисе
    } */

    @MessageMapping("/notifications.requestPending")
    public void requestPendingNotifications(Principal principal) {
        String username = principal.getName();
        List<NotificationDTO> pending = notificationService.loadPendingNotifications(username);

        if (!pending.isEmpty()) {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    pending
            );
            notificationService.clearNotifications(username);
        }
    }
}