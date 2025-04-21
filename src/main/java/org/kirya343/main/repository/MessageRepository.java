package org.kirya343.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.kirya343.main.model.Conversation;
import org.kirya343.main.model.Message;
import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Получить все непрочитанные сообщения для пользователя
    List<Message> findByReceiverAndReadFalse(User receiver);

    // Получить все непрочитанные сообщения для пользователя в конкретном разговоре
    List<Message> findByConversationAndReceiverAndReadFalse(Conversation conversation, User receiver);

    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    // Новый метод: получить сообщения по ID разговора (с сортировкой по времени)
    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.conversation.id = :conversationId AND m.receiver.id = :userId AND m.read = false")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}


