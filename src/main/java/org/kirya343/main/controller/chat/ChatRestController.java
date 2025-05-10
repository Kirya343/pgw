package org.kirya343.main.controller.chat;

import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.model.DTOs.MessageDTO;
import org.kirya343.main.model.User;
import org.kirya343.main.services.chat.ChatService;
import org.kirya343.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long conversationId, Principal principal) {
        User currentUser = userService.findBySub(principal.getName());

        Conversation conversation = chatService.getConversationById(conversationId);

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404, если беседа не найдена
        }

        // Маркируем сообщения как прочитанные
        chatService.markMessagesAsRead(conversationId, currentUser);

        // Получаем сообщения
        List<Message> messages = chatService.getMessages(conversation);

        if (messages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // 204, если сообщений нет
        }

        // Преобразуем в DTO и возвращаем
        List<MessageDTO> messageDTOs = messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getId(),
                        msg.getText(),
                        msg.getSentAt(),
                        msg.getSender().getId(),
                        msg.getReceiver().getId(),
                        msg.getConversation().getId(),
                        msg.isOwn(currentUser)
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

}

