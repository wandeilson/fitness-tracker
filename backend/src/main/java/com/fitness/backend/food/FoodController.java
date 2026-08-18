package com.fitness.backend.food;

import com.fitness.backend.food.dto.FoodResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public ResponseEntity<List<FoodResponse>> listFoods(@RequestParam(required = false, name = "q") String query) {
        return ResponseEntity.ok(foodService.listFoods(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFood(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFood(id));
    }
}
