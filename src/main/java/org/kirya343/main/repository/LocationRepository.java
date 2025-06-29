package org.kirya343.main.repository;

import org.kirya343.main.model.listingModels.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByOrderByNameAsc();
    Optional<Location> findByName(String name);
}