package com.fitness.backend.goal;

import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {

    private static final BigDecimal DEFAULT_CARBS_PERCENT = BigDecimal.valueOf(50);
    private static final BigDecimal DEFAULT_PROTEIN_PERCENT = BigDecimal.valueOf(25);
    private static final BigDecimal DEFAULT_FAT_PERCENT = BigDecimal.valueOf(25);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal CARBS_KCAL_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal PROTEIN_KCAL_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal FAT_KCAL_PER_GRAM = BigDecimal.valueOf(9);

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GoalResponse upsertGoal(String email, GoalRequest request) {
        User user = getUserByEmail(email);
        MacroDistribution distribution = resolveDistribution(request);
        BigDecimal calories = BigDecimal.valueOf(request.calories());

        BigDecimal carbsCalories = calculateMacroCalories(calories, distribution.carbsPercent());
        BigDecimal proteinCalories = calculateMacroCalories(calories, distribution.proteinPercent());
        BigDecimal fatCalories = calculateMacroCalories(calories, distribution.fatPercent());

        Goal goal = goalRepository.findByUserId(user.getId()).orElseGet(Goal::new);
        goal.setUser(user);
        goal.setCalories(request.calories());
        goal.setCarbsPercent(distribution.carbsPercent());
        goal.setProteinPercent(distribution.proteinPercent());
        goal.setFatPercent(distribution.fatPercent());
        goal.setCarbsCalories(carbsCalories);
        goal.setProteinCalories(proteinCalories);
        goal.setFatCalories(fatCalories);
        goal.setCarbsG(calculateMacroGrams(carbsCalories, CARBS_KCAL_PER_GRAM));
        goal.setProteinG(calculateMacroGrams(proteinCalories, PROTEIN_KCAL_PER_GRAM));
        goal.setFatG(calculateMacroGrams(fatCalories, FAT_KCAL_PER_GRAM));
        goal.setUpdatedAt(Instant.now());

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(String email) {
        User user = getUserByEmail(email);

        Goal goal = goalRepository.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        return toResponse(goal);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
            goal.getCalories(),
            goal.getCarbsPercent(),
            goal.getProteinPercent(),
            goal.getFatPercent(),
            goal.getCarbsCalories(),
            goal.getProteinCalories(),
            goal.getFatCalories(),
            goal.getCarbsG(),
            goal.getProteinG(),
            goal.getFatG(),
            goal.getUpdatedAt()
        );
    }

    private MacroDistribution resolveDistribution(GoalRequest request) {
        if (request.carbsPercent() == null && request.proteinPercent() == null && request.fatPercent() == null) {
            return new MacroDistribution(DEFAULT_CARBS_PERCENT, DEFAULT_PROTEIN_PERCENT, DEFAULT_FAT_PERCENT);
        }

        if (request.carbsPercent() == null || request.proteinPercent() == null || request.fatPercent() == null) {
            throw new IllegalArgumentException("All macro percentages must be provided when customizing distribution");
        }

        BigDecimal carbsPercent = BigDecimal.valueOf(request.carbsPercent());
        BigDecimal proteinPercent = BigDecimal.valueOf(request.proteinPercent());
        BigDecimal fatPercent = BigDecimal.valueOf(request.fatPercent());

        BigDecimal totalPercent = carbsPercent.add(proteinPercent).add(fatPercent).setScale(2, RoundingMode.HALF_UP);
        if (totalPercent.compareTo(HUNDRED.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("Macro percentages must sum exactly to 100");
        }

        return new MacroDistribution(
            carbsPercent.setScale(2, RoundingMode.HALF_UP),
            proteinPercent.setScale(2, RoundingMode.HALF_UP),
            fatPercent.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateMacroCalories(BigDecimal totalCalories, BigDecimal macroPercent) {
        return totalCalories
            .multiply(macroPercent)
            .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMacroGrams(BigDecimal macroCalories, BigDecimal kcalPerGram) {
        return macroCalories.divide(kcalPerGram, 2, RoundingMode.HALF_UP);
    }

    private record MacroDistribution(BigDecimal carbsPercent, BigDecimal proteinPercent, BigDecimal fatPercent) {
    }
}
