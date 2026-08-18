package com.fitness.backend.meal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByUserIdAndMealDateOrderByCreatedAtAsc(Long userId, LocalDate mealDate);

    Optional<Meal> findByIdAndUserId(Long id, Long userId);
}
