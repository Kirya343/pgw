package org.workswap.main.controller.components;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.MessageDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.chat.Conversation;
import org.workswap.datasource.main.model.chat.Message;
import org.workswap.main.services.UserService;
import org.workswap.main.services.chat.ChatService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long conversationId, Principal principal) {
        User currentUser = userService.findUser(principal.getName(), SearchParamType.SUB);

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

