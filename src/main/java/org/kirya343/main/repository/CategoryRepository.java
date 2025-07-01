package org.kirya343.main.repository;

import java.util.List;

import org.kirya343.main.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
        WITH RECURSIVE category_path AS (
            SELECT id, name, parent_id, leaf
            FROM category
            WHERE id = :categoryId
            
            UNION ALL
            
            SELECT c.id, c.name, c.parent_id, c.leaf
            FROM category c
            JOIN category_path cp ON c.id = cp.parent_id
        )
        SELECT * FROM category_path
        ORDER BY (parent_id IS NOT NULL), parent_id
        """, nativeQuery = true)
    List<Category> findCategoryPathWithNativeQuery(@Param("categoryId") Long categoryId);
}
