package com.fitness.backend.goal;

import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
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
        LocalDate today = LocalDate.now();
        MacroDistribution distribution = resolveDistribution(request);
        BigDecimal calories = BigDecimal.valueOf(request.calories());
        Instant now = Instant.now();

        BigDecimal carbsCalories = calculateMacroCalories(calories, distribution.carbsPercent());
        BigDecimal proteinCalories = calculateMacroCalories(calories, distribution.proteinPercent());
        BigDecimal fatCalories = calculateMacroCalories(calories, distribution.fatPercent());

        // Close the current open goal if it started before today
        Optional<Goal> currentOpt = goalRepository.findCurrentByUserId(user.getId());
        currentOpt.ifPresent(current -> {
            if (current.getValidFrom().isBefore(today)) {
                current.setValidUntil(today.minusDays(1));
                current.setUpdatedAt(now);
                goalRepository.save(current);
            }
        });

        // Check if today already has a goal (same valid_from)
        Optional<Goal> todayGoalOpt = goalRepository.findActiveByUserIdAndDate(user.getId(), today);
        Goal goal;
        if (todayGoalOpt.isPresent() && todayGoalOpt.get().getValidFrom().equals(today)) {
            // Update today's existing goal
            goal = todayGoalOpt.get();
        } else {
            // Create new goal starting today
            goal = new Goal();
            goal.setUser(user);
            goal.setValidFrom(today);
            goal.setCreatedAt(now);
        }

        goal.setValidUntil(null);
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
        goal.setUpdatedAt(now);

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoal(String email) {
        User user = getUserByEmail(email);
        Goal goal = goalRepository.findCurrentByUserId(user.getId())
            .or(() -> goalRepository.findMostRecentClosedByUserId(user.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        return toResponse(goal);
    }

    @Transactional(readOnly = true)
    public Optional<GoalResponse> getGoalForDate(String email, LocalDate date) {
        User user = getUserByEmail(email);
        return goalRepository.findActiveByUserIdAndDate(user.getId(), date)
            .or(() -> goalRepository.findMostRecentClosedByUserId(user.getId()))
            .map(this::toResponse);
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
            goal.getValidFrom(),
            goal.getValidUntil(),
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
