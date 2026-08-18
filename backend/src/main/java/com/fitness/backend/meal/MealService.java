package com.fitness.backend.meal;

import com.fitness.backend.food.Food;
import com.fitness.backend.food.FoodRepository;
import com.fitness.backend.meal.dto.DailySummaryResponse;
import com.fitness.backend.meal.dto.MealCreateRequest;
import com.fitness.backend.meal.dto.MealItemRequest;
import com.fitness.backend.meal.dto.MealItemResponse;
import com.fitness.backend.meal.dto.MealItemUpdateRequest;
import com.fitness.backend.meal.dto.MealResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public MealService(
        MealRepository mealRepository,
        MealItemRepository mealItemRepository,
        FoodRepository foodRepository,
        UserRepository userRepository
    ) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MealResponse createMeal(String email, MealCreateRequest request) {
        User user = getUserByEmail(email);

        Meal meal = new Meal();
        meal.setUser(user);
        meal.setMealDate(request.mealDate());
        meal.setMealType(request.mealType());
        meal.setConsumedAt(request.consumedAt());
        meal.setNotes(request.notes());
        meal.setCreatedAt(Instant.now());

        Meal saved = mealRepository.save(meal);
        return toMealResponse(saved, List.of());
    }

    @Transactional(readOnly = true)
    public List<MealResponse> listMealsByDate(String email, LocalDate date) {
        User user = getUserByEmail(email);
        List<Meal> meals = mealRepository.findByUserIdAndMealDateOrderByCreatedAtAsc(user.getId(), date);

        List<MealResponse> result = new ArrayList<>();
        for (Meal meal : meals) {
            List<MealItem> items = mealItemRepository.findByMealIdOrderByCreatedAtAsc(meal.getId());
            result.add(toMealResponse(meal, items));
        }

        return result;
    }

    @Transactional
    public MealResponse addMealItem(String email, Long mealId, MealItemRequest request) {
        Meal meal = getOwnedMeal(email, mealId);
        Food food = foodRepository.findByIdAndActiveTrue(request.foodId())
            .orElseThrow(() -> new IllegalArgumentException("Food not found"));

        MealItem item = new MealItem();
        item.setMeal(meal);
        item.setFood(food);
        item.setGrams(scale2(request.grams()));
        item.setKcalConsumed(calculateConsumed(food.getKcalPer100g(), request.grams()));
        item.setCarbsConsumed(calculateConsumed(food.getCarbsPer100g(), request.grams()));
        item.setProteinConsumed(calculateConsumed(food.getProteinPer100g(), request.grams()));
        item.setFatConsumed(calculateConsumed(food.getFatPer100g(), request.grams()));
        item.setCreatedAt(Instant.now());

        mealItemRepository.save(item);

        List<MealItem> items = mealItemRepository.findByMealIdOrderByCreatedAtAsc(meal.getId());
        return toMealResponse(meal, items);
    }

    @Transactional
    public MealResponse updateMealItem(String email, Long mealId, Long itemId, MealItemUpdateRequest request) {
        Meal meal = getOwnedMeal(email, mealId);

        MealItem item = mealItemRepository.findByIdAndMealId(itemId, mealId)
            .orElseThrow(() -> new IllegalArgumentException("Meal item not found"));

        item.setGrams(scale2(request.grams()));
        item.setKcalConsumed(calculateConsumed(item.getFood().getKcalPer100g(), request.grams()));
        item.setCarbsConsumed(calculateConsumed(item.getFood().getCarbsPer100g(), request.grams()));
        item.setProteinConsumed(calculateConsumed(item.getFood().getProteinPer100g(), request.grams()));
        item.setFatConsumed(calculateConsumed(item.getFood().getFatPer100g(), request.grams()));

        mealItemRepository.save(item);

        List<MealItem> items = mealItemRepository.findByMealIdOrderByCreatedAtAsc(meal.getId());
        return toMealResponse(meal, items);
    }

    @Transactional
    public void deleteMealItem(String email, Long mealId, Long itemId) {
        getOwnedMeal(email, mealId);

        MealItem item = mealItemRepository.findByIdAndMealId(itemId, mealId)
            .orElseThrow(() -> new IllegalArgumentException("Meal item not found"));

        mealItemRepository.delete(item);
    }

    @Transactional
    public void deleteMeal(String email, Long mealId) {
        Meal meal = getOwnedMeal(email, mealId);
        mealRepository.delete(meal);
    }

    @Transactional(readOnly = true)
    public DailySummaryResponse getDailySummary(String email, LocalDate date) {
        List<MealResponse> meals = listMealsByDate(email, date);

        BigDecimal kcalTotal = BigDecimal.ZERO;
        BigDecimal carbsTotal = BigDecimal.ZERO;
        BigDecimal proteinTotal = BigDecimal.ZERO;
        BigDecimal fatTotal = BigDecimal.ZERO;

        for (MealResponse meal : meals) {
            kcalTotal = kcalTotal.add(meal.kcalTotal());
            carbsTotal = carbsTotal.add(meal.carbsTotal());
            proteinTotal = proteinTotal.add(meal.proteinTotal());
            fatTotal = fatTotal.add(meal.fatTotal());
        }

        return new DailySummaryResponse(
            date,
            scale2(kcalTotal),
            scale2(carbsTotal),
            scale2(proteinTotal),
            scale2(fatTotal)
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Meal getOwnedMeal(String email, Long mealId) {
        User user = getUserByEmail(email);

        return mealRepository.findByIdAndUserId(mealId, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Meal not found"));
    }

    private BigDecimal calculateConsumed(BigDecimal valuePer100g, BigDecimal grams) {
        return valuePer100g.multiply(grams).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private MealResponse toMealResponse(Meal meal, List<MealItem> items) {
        List<MealItemResponse> itemResponses = new ArrayList<>();

        BigDecimal kcalTotal = BigDecimal.ZERO;
        BigDecimal carbsTotal = BigDecimal.ZERO;
        BigDecimal proteinTotal = BigDecimal.ZERO;
        BigDecimal fatTotal = BigDecimal.ZERO;

        for (MealItem item : items) {
            kcalTotal = kcalTotal.add(item.getKcalConsumed());
            carbsTotal = carbsTotal.add(item.getCarbsConsumed());
            proteinTotal = proteinTotal.add(item.getProteinConsumed());
            fatTotal = fatTotal.add(item.getFatConsumed());

            itemResponses.add(new MealItemResponse(
                item.getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getGrams(),
                item.getKcalConsumed(),
                item.getCarbsConsumed(),
                item.getProteinConsumed(),
                item.getFatConsumed()
            ));
        }

        return new MealResponse(
            meal.getId(),
            meal.getMealDate(),
            meal.getMealType(),
            meal.getConsumedAt(),
            meal.getNotes(),
            scale2(kcalTotal),
            scale2(carbsTotal),
            scale2(proteinTotal),
            scale2(fatTotal),
            itemResponses
        );
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
