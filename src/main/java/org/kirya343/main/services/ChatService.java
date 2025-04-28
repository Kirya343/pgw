package org.kirya343.main.services;

import jakarta.transaction.Transactional;
import org.kirya343.main.controller.mappers.ChatMapper;
import org.kirya343.main.model.chat.Conversation;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.DTOs.ConversationDTO;
import org.kirya343.main.model.chat.Message;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ConversationRepository;
import org.kirya343.main.repository.MessageRepository;
import org.kirya343.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

    public List<ConversationDTO> getConversationsForUser(User user) {
        List<Conversation> conversations = conversationRepository.findByUser1OrUser2(user, user);
        return conversations.stream()
                .map(conv -> chatMapper.convertToDTO(conv, user))
                .collect(Collectors.toList());
    }

    public void notifyConversationUpdate(Long conversationId, User user) {
        Conversation conversation = getConversationById(conversationId);
        ConversationDTO dto = chatMapper.convertToDTO(conversation, user);

        // Определяем, есть ли новые сообщения
        boolean hasNewMessage = conversation.getMessages().stream()
                .anyMatch(msg -> !msg.isRead() && msg.getReceiver().equals(user));

        // Устанавливаем флаг нового сообщения
        dto.setHasNewMessage(hasNewMessage);

        // Отправляем обновление конкретному пользователю
        messagingTemplate.convertAndSendToUser(
                user.getSub(),
                "/queue/conversations.updates",
                dto
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
        message.setReceiver(conversation.getOtherParticipant(sender));
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
}
