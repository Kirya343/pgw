package org.kirya343.main.model.DTOs;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class NewsForm {
    private Map<String, NewsTranslationDTO> translations = new HashMap<>();
}
