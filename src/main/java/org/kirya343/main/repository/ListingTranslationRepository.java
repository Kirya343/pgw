package org.kirya343.main.repository;

import org.kirya343.main.model.Listing;
import org.kirya343.main.model.listingModels.ListingTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingTranslationRepository extends JpaRepository<ListingTranslation, Long>{
    ListingTranslation findByListingAndLanguage(Listing listing, String lang);
}
