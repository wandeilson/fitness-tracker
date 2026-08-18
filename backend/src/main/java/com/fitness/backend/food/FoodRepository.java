package com.fitness.backend.food;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {

    long countBySourceIgnoreCase(String source);

    List<Food> findBySourceIgnoreCase(String source);

    Optional<Food> findBySourceIgnoreCaseAndSourceCode(String source, String sourceCode);

    List<Food> findByActiveTrueOrderByNameAsc();

    List<Food> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Food> findByIdAndActiveTrue(Long id);
}
