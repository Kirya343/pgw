package org.kirya343.main.repository;

import java.util.List;

import org.kirya343.main.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Найти все корневые категории (без родителя)
    List<Category> findByParentIsNull();
    
    // Найти все дочерние категории для указанного родителя
    List<Category> findByParentId(Long parentId);
    
    // Проверить существование категории по имени
    boolean existsByName(String name);
    
    // Найти категории по флагу leaf (конечные/не конечные)
    List<Category> findByLeaf(boolean leaf);
}
