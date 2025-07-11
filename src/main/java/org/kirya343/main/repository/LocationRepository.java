package org.kirya343.main.repository;

import org.kirya343.main.model.listingModels.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByOrderByNameAsc();
    Location findByName(String name);

    List<Location> findByCountry(Location country);
    List<Location> findByCountryId(Long countryId);

    List<Location> findByCity(Boolean city);
}