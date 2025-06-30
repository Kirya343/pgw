package org.kirya343.main.model.DTOs;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ListingForm {
    private Map<String, TranslationDTO> translations = new HashMap<>();
}
