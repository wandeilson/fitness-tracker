package com.fitness.backend.food;

import com.fitness.backend.food.dto.FoodResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Transactional(readOnly = true)
    public List<FoodResponse> listFoods(String query) {
        List<Food> foods;
        if (query == null || query.isBlank()) {
            foods = foodRepository.findByActiveTrueOrderByNameAsc();
        } else {
            foods = foodRepository.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        }

        return foods.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FoodResponse getFood(Long id) {
        Food food = foodRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new IllegalArgumentException("Food not found"));

        return toResponse(food);
    }

    private FoodResponse toResponse(Food food) {
        return new FoodResponse(
            food.getId(),
            food.getSource(),
            food.getSourceCode(),
            food.getName(),
            food.getKcalPer100g(),
            food.getCarbsPer100g(),
            food.getProteinPer100g(),
            food.getFatPer100g()
        );
    }
}
