package org.kirya343.main.model.DTOs;

import lombok.Data;

@Data
public class LocationDTO {
    
    private Long id;
    private boolean city;
    private Long countryId;
    private String fullName;
    private String name;
}
