package org.workswap.datasource.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.workswap.datasource.stats.model.StatSnapshot;

@Repository
public interface StatsRepository extends JpaRepository<StatSnapshot, Long>{

}
