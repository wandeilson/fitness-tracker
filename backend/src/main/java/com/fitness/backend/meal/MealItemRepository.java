package com.fitness.backend.meal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {

    List<MealItem> findByMealIdOrderByCreatedAtAsc(Long mealId);

    Optional<MealItem> findByIdAndMealId(Long id, Long mealId);
}
