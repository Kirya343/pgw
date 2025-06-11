package org.kirya343.main.controller.components;

import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.model.DTOs.MessageDTO;
import org.kirya343.main.model.User;
import org.kirya343.main.services.ChatService;
import org.kirya343.main.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long conversationId, @AuthenticationPrincipal OAuth2User oauth2User) {
        User currentUser = userService.findUserFromOAuth2(oauth2User);

        Conversation conversation = chatService.getConversationById(conversationId);
        chatService.markMessagesAsRead(conversationId, currentUser.getId());

        if (conversation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404, если беседа не найдена
        }

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

