package org.kirya343.main.services;

import java.util.ArrayList;
import java.util.List;

import org.kirya343.main.model.listingModels.Category;
import org.kirya343.main.model.listingModels.Location;
import org.kirya343.main.repository.LocationRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public List<Location> getAllDescendants(Location location) {
        List<Location> descendants = new ArrayList<>();
        descendants.add(location);
        if (location.isCountry()) {
            List<Location> cities = locationRepository.findByCountryLocation(location);
            for (Location child : cities) {
                descendants.add(child);
            }
        }
        return descendants;
    }
}
