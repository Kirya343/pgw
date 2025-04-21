package org.kirya343.main.controller;

import org.kirya343.main.model.Conversation;
import org.kirya343.main.model.Message;
import org.kirya343.main.model.MessageDTO;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO messageDTO, Principal principal) throws AccessDeniedException {
        // Проверка прав доступа
        if (!chatService.hasAccessToConversation(principal.getName(), messageDTO.getConversationId())) {
            throw new AccessDeniedException("No access to this conversation");
        }

        // Сохраняем сообщение в базу
        User sender = userService.findByEmail(principal.getName());
        Conversation conversation = chatService.getConversationById(messageDTO.getConversationId());
        Message message = chatService.sendMessage(conversation, sender, messageDTO.getText());

        // Отправляем сообщение всем подписчикам
        messagingTemplate.convertAndSend("/topic/messages." + messageDTO.getConversationId(),
                new MessageDTO(
                        message.getId(),
                        message.getText(),
                        message.getSentAt(),
                        message.getSender().getId(),
                        message.getConversation().getId()
                ));
    }
}