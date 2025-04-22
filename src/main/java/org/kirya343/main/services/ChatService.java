package org.kirya343.main.services;

import jakarta.transaction.Transactional;
import org.kirya343.main.model.Conversation;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.Message;
import org.kirya343.main.model.User;
import org.kirya343.main.repository.ConversationRepository;
import org.kirya343.main.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

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

    public boolean hasAccessToConversation(String username, Long conversationId) {
        if (username == null || conversationId == null) {
            return false;
        }

        Conversation conversation = getConversationById(conversationId);
        if (conversation == null) {
            return false;
        }

        return conversation.getUser1().getEmail().equals(username) ||
                conversation.getUser2().getEmail().equals(username);
    }

    public List<Conversation> getUserConversations(User user) {
        List<Conversation> conversations = conversationRepository.findByUser1OrUser2(user, user);
        // Добавить логирование для проверки
        System.out.println("Conversations found: " + conversations.size());
        return conversations;
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
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(conversation.getOtherParticipant(sender));
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        conversation.getMessages().add(message); // Добавляем в список сообщений диалога
        messageRepository.save(message);

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
