package org.workswap.main.services.chat;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.Listing;
import org.workswap.datasource.main.model.User;
import org.workswap.datasource.main.model.DTOs.ConversationDTO;
import org.workswap.datasource.main.model.ModelsSettings.SearchParamType;
import org.workswap.datasource.main.model.chat.Conversation;
import org.workswap.datasource.main.model.chat.Message;
import org.workswap.datasource.main.repository.ConversationRepository;
import org.workswap.datasource.main.repository.MessageRepository;
import org.workswap.main.services.ListingService;
import org.workswap.main.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ListingService listingService;

    public Conversation getOrCreateConversation(User user1, User user2, Listing listing) {
        if (listing != null) {
            // Сначала ищем чат с привязкой к объявлению
            Optional<Conversation> existing = conversationRepository.findByUser1AndUser2AndListing(user1, user2, listing)
                    .or(() -> conversationRepository.findByUser2AndUser1AndListing(user1, user2, listing));

            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Если чат с объявлением не найден, ищем любой чат между пользователями
        Optional<Conversation> anyConversation = conversationRepository.findByUser1AndUser2(user1, user2)
                .or(() -> conversationRepository.findByUser2AndUser1(user1, user2));

        if (anyConversation.isPresent() && listing == null) {
            return anyConversation.get();
        }

        // Создаем новый чат
        Conversation conversation = new Conversation();
        conversation.setUser1(user1);
        conversation.setUser2(user2);
        conversation.setListing(listing);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(User user) {
        List<Conversation> conversations = conversationRepository.findByUser1OrUser2(user, user);
        // Добавить логирование для проверки
        System.out.println("Conversations found: " + conversations.size());
        return conversations;
    }

    public List<ConversationDTO> getConversationsForUser(User user, Locale locale) {
        List<Conversation> conversations = conversationRepository.findByUser1OrUser2(user, user);
        return conversations.stream()
                .map(conv -> convertToDTO(conv, user, locale))
                .collect(Collectors.toList());
    }

    public void notifyConversationUpdate(Long conversationId, User user, Locale locale) {
        Conversation conversation = getConversationById(conversationId);
        ConversationDTO conversationDto = convertToDTO(conversation, user, locale);

        // Определяем, есть ли новые сообщения
        boolean hasNewMessage = conversation.getMessages().stream()
                .anyMatch(msg -> !msg.isRead() && msg.getReceiver().equals(user));

        // Устанавливаем флаг нового сообщения
        conversationDto.setHasNewMessage(hasNewMessage);

        // Отправляем обновление конкретному пользователю
        messagingTemplate.convertAndSendToUser(
                user.getSub(),
                "/queue/conversations.updates",
                conversationDto
        );
    }

    public boolean conversationExists(User user1, User user2) {
        return conversationRepository.findByUser1AndUser2(user1, user2).isPresent() ||
                conversationRepository.findByUser2AndUser1(user1, user2).isPresent();
    }

    public long getUnreadMessageCount(Conversation conversation, User user) {
        // Получаем все непрочитанные сообщения для конкретного разговора и пользователя
        return messageRepository.findByConversationAndReceiverAndReadFalse(conversation, user).size();
    }

    public List<Message> getMessages(Conversation conversation) {
        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    @Transactional
    public Message sendMessage(Conversation conversation, User sender, String text) {
        // Проверка и инициализация списка сообщений, если он пуст или еще не загружен
        if (conversation.getMessages() == null) {
            conversation.setMessages(new ArrayList<>());  // Инициализация списка сообщений, если он пуст
        } else {
            // Явная инициализация коллекции сообщений, если она использует ленивую загрузку
            conversation.getMessages().size();
        }

        // Создание нового сообщения
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(conversation.getInterlocutor(sender));
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        // Добавление сообщения в список сообщений беседы
        conversation.getMessages().add(message);

        // Сохраняем сообщение
        messageRepository.save(message);

        // Сохраняем изменения в беседе (если коллекция сообщений была обновлена)
        conversationRepository.save(conversation);

        return message;
    }

    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }
    @Transactional
    public void markMessagesAsRead(Long conversationId, User reader) {
        messageRepository.markMessagesAsRead(conversationId, reader.getId());
    }

    public ConversationDTO convertToDTO(Conversation conversation, User currentUser, Locale locale) {
        User interlocutor = userService.findUser(conversation.getInterlocutor(currentUser).getId().toString(), SearchParamType.ID);

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setInterlocutorName(interlocutor.getName());
        dto.setInterlocutorAvatar(interlocutor.getAvatarUrl());
        dto.setUnreadCount(getUnreadMessageCount(conversation, currentUser));
        dto.setListing(listingService.convertToDTO(conversation.getListing(), locale));

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
