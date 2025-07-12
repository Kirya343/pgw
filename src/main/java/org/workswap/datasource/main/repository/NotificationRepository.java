package org.workswap.datasource.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.main.model.Notification;
import org.workswap.datasource.main.model.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient(User recipient);

    List<Notification> findByRecipientId(Long recipientId);

    /*

    @Modifying
    @Query("DELETE FROM PersistentNotification n WHERE n.created < :cutoff")
    void deleteByCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("DELETE FROM PersistentNotification n WHERE n.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    long countByUserId(String userId);

    @Modifying
    @Query("DELETE FROM PersistentNotification n WHERE n.id IN " +
            "(SELECT n2.id FROM PersistentNotification n2 WHERE n2.userId = :userId ORDER BY n2.created ASC LIMIT 1)")
    void deleteOldestByUserId(@Param("userId") String userId); */
}
