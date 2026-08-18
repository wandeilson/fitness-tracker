package com.fitness.backend.meal;

import com.fitness.backend.meal.dto.DailySummaryResponse;
import com.fitness.backend.meal.dto.MealCreateRequest;
import com.fitness.backend.meal.dto.MealItemRequest;
import com.fitness.backend.meal.dto.MealItemUpdateRequest;
import com.fitness.backend.meal.dto.MealResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    public ResponseEntity<MealResponse> createMeal(
        @Valid @RequestBody MealCreateRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealService.createMeal(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<MealResponse>> listMealsByDate(
        @RequestParam LocalDate date,
        Authentication authentication
    ) {
        return ResponseEntity.ok(mealService.listMealsByDate(authentication.getName(), date));
    }

    @GetMapping("/summary")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
        @RequestParam LocalDate date,
        Authentication authentication
    ) {
        return ResponseEntity.ok(mealService.getDailySummary(authentication.getName(), date));
    }

    @PostMapping("/{mealId}/items")
    public ResponseEntity<MealResponse> addMealItem(
        @PathVariable Long mealId,
        @Valid @RequestBody MealItemRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(mealService.addMealItem(authentication.getName(), mealId, request));
    }

    @PutMapping("/{mealId}/items/{itemId}")
    public ResponseEntity<MealResponse> updateMealItem(
        @PathVariable Long mealId,
        @PathVariable Long itemId,
        @Valid @RequestBody MealItemUpdateRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(mealService.updateMealItem(authentication.getName(), mealId, itemId, request));
    }

    @DeleteMapping("/{mealId}/items/{itemId}")
    public ResponseEntity<Void> deleteMealItem(
        @PathVariable Long mealId,
        @PathVariable Long itemId,
        Authentication authentication
    ) {
        mealService.deleteMealItem(authentication.getName(), mealId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{mealId}")
    public ResponseEntity<Void> deleteMeal(
        @PathVariable Long mealId,
        Authentication authentication
    ) {
        mealService.deleteMeal(authentication.getName(), mealId);
        return ResponseEntity.noContent().build();
    }
}
