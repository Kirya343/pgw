package org.kirya343.main.model.DTOs;

import java.util.HashMap;
import java.util.Map;

import org.kirya343.main.model.Listing;

import lombok.Data;

@Data
public class ListingForm {

    private Listing listing = new Listing();
    private Map<String, TranslationDTO> translations = new HashMap<>();
}
