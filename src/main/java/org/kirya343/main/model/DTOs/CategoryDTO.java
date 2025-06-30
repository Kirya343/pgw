package org.kirya343.main.model.DTOs;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId; // ID родительской категории (null для корневых)
    private boolean leaf; // Является ли конечной категорией
}
