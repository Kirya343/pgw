package org.workswap.datasource.stats.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.stats.model.StatSnapshot;

@Repository
public interface StatsRepository extends JpaRepository<StatSnapshot, Long>{
    @Query("SELECT COUNT(s) FROM StatSnapshot s WHERE s.listingId = :listingId AND s.intervaType = :intervalType AND s.time >= :since")
    long countRecentSnapshots(@Param("listingId") Long listingId,
                            @Param("intervalType") StatSnapshot.IntervalType intervalType,
                            @Param("since") LocalDateTime since);

    List<StatSnapshot> findByListingIdAndIntervaTypeAndTimeAfter(
        Long listingId,
        StatSnapshot.IntervalType intervalType,
        LocalDateTime time
    );

}
