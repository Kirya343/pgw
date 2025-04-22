package org.kirya343.main.repository;

import org.kirya343.main.model.Conversation;
import org.kirya343.main.model.Listing;
import org.kirya343.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Найти разговор между двумя пользователями (в любом порядке)
    Optional<Conversation> findByUser1AndUser2(User user1, User user2);
    Optional<Conversation> findByUser2AndUser1(User user1, User user2);

    // Найти все разговоры пользователя (как для user1, так и для user2)
    List<Conversation> findByUser1OrUser2(User user1, User user2);

    // Если нужно сделать запрос с distinct (выбор уникальных разговоров)
    List<Conversation> findDistinctByUser1OrUser2(User user1, User user2);

    Optional<Conversation> findByUser1AndUser2AndListing(User user1, User user2, Listing listing);
    Optional<Conversation> findByUser2AndUser1AndListing(User user1, User user2, Listing listing);
    boolean existsByUser1AndUser2(User user1, User user2);
    boolean existsByUser2AndUser1(User user1, User user2);
}
