package org.kirya343.main.repository;

import org.kirya343.main.model.chat.PersistentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<PersistentNotification, Long> {
    List<PersistentNotification> findByUserId(String userId);

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
    void deleteOldestByUserId(@Param("userId") String userId);
}
