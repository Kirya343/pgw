package org.workswap.main.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.workswap.datasource.main.model.DTOs.LocationDTO;
import org.workswap.datasource.main.model.listingModels.Location;
import org.workswap.datasource.main.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public List<Location> getAllDescendants(Location location) {
        List<Location> descendants = new ArrayList<>();
        descendants.add(location);
        if (!location.isCity()) {
            List<Location> cities = locationRepository.findByCountry(location);
            for (Location child : cities) {
                descendants.add(child);
            }
        }
        return descendants;
    }

    public List<Location> getCities(Long coutryId) {
        return locationRepository.findByCountryId(coutryId);
    }

    public LocationDTO toDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setCity(location.isCity());
        dto.setCountryId(location.isCity() == true ? location.getCountry().getId() : null);
        dto.setFullName(location.getFullName());
        dto.setName(location.getName());
        dto.setId(location.getId());
        return dto;
    }
}
