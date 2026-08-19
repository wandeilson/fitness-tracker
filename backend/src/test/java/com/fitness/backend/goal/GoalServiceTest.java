package com.fitness.backend.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GoalService goalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("user@test.com");
        user.setFullName("User Test");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void upsertGoalShouldApplyDefaultDistributionWhenPercentagesAreNull() {
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findActiveByUserIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoal("user@test.com", new GoalRequest(2000, null, null, null));

        assertEquals(LocalDate.now(), response.validFrom());
        assertEquals(null, response.validUntil());
        assertBigDecimalEquals("50", response.carbsPercent());
        assertBigDecimalEquals("25", response.proteinPercent());
        assertBigDecimalEquals("25", response.fatPercent());
        assertBigDecimalEquals("1000.00", response.carbsCalories());
        assertBigDecimalEquals("500.00", response.proteinCalories());
        assertBigDecimalEquals("500.00", response.fatCalories());
        assertBigDecimalEquals("250.00", response.carbsG());
        assertBigDecimalEquals("125.00", response.proteinG());
        assertBigDecimalEquals("55.56", response.fatG());
    }

    @Test
    void upsertGoalShouldApplyCustomDistributionWhenPercentagesSumTo100() {
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findActiveByUserIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoal("user@test.com", new GoalRequest(2000, 40.0, 30.0, 30.0));

        assertEquals(LocalDate.now(), response.validFrom());
        assertBigDecimalEquals("40.00", response.carbsPercent());
        assertBigDecimalEquals("30.00", response.proteinPercent());
        assertBigDecimalEquals("30.00", response.fatPercent());
        assertBigDecimalEquals("800.00", response.carbsCalories());
        assertBigDecimalEquals("600.00", response.proteinCalories());
        assertBigDecimalEquals("600.00", response.fatCalories());
        assertBigDecimalEquals("200.00", response.carbsG());
        assertBigDecimalEquals("150.00", response.proteinG());
        assertBigDecimalEquals("66.67", response.fatG());
    }

    @Test
    void upsertGoalShouldClosePreviousGoalWhenItStartedBeforeToday() {
        Goal previousGoal = buildGoal(1L, LocalDate.now().minusDays(10), null);
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.of(previousGoal));
        when(goalRepository.findActiveByUserIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        goalService.upsertGoal("user@test.com", new GoalRequest(2500, null, null, null));

        assertEquals(LocalDate.now().minusDays(1), previousGoal.getValidUntil());
        verify(goalRepository).save(previousGoal);
    }

    @Test
    void upsertGoalShouldNotCloseGoalThatStartsToday() {
        Goal todayGoal = buildGoal(1L, LocalDate.now(), null);
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.of(todayGoal));
        when(goalRepository.findActiveByUserIdAndDate(1L, LocalDate.now())).thenReturn(Optional.of(todayGoal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        goalService.upsertGoal("user@test.com", new GoalRequest(1800, null, null, null));

        assertEquals(null, todayGoal.getValidUntil());
    }

    @Test
    void upsertGoalShouldThrowWhenCustomPercentagesDoNotSumTo100() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> goalService.upsertGoal("user@test.com", new GoalRequest(2000, 40.0, 30.0, 20.0))
        );

        assertEquals("Macro percentages must sum exactly to 100", ex.getMessage());
    }

    @Test
    void upsertGoalShouldThrowWhenOnlyPartOfCustomPercentagesIsProvided() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> goalService.upsertGoal("user@test.com", new GoalRequest(2000, 40.0, null, 60.0))
        );

        assertEquals("All macro percentages must be provided when customizing distribution", ex.getMessage());
    }

    @Test
    void getGoalShouldReturnCurrentGoal() {
        Goal current = buildGoal(1L, LocalDate.now().minusDays(5), null);
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.of(current));

        GoalResponse response = goalService.getGoal("user@test.com");

        assertEquals(2000, response.calories());
    }

    @Test
    void getGoalShouldFallbackToMostRecentClosedGoal() {
        Goal closed = buildGoal(1L, LocalDate.now().minusDays(10), LocalDate.now().minusDays(1));
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findMostRecentClosedByUserId(1L)).thenReturn(Optional.of(closed));

        GoalResponse response = goalService.getGoal("user@test.com");

        assertEquals(2000, response.calories());
    }

    @Test
    void getGoalShouldThrowWhenNoGoalExists() {
        when(goalRepository.findCurrentByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.findMostRecentClosedByUserId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> goalService.getGoal("user@test.com")
        );

        assertEquals("Goal not found", ex.getMessage());
    }

    @Test
    void getGoalForDateShouldReturnGoalForSpecificDate() {
        Goal pastGoal = buildGoal(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        pastGoal.setCalories(1500);
        when(goalRepository.findActiveByUserIdAndDate(1L, LocalDate.of(2026, 3, 15)))
            .thenReturn(Optional.of(pastGoal));

        Optional<GoalResponse> result = goalService.getGoalForDate("user@test.com", LocalDate.of(2026, 3, 15));

        assertNotNull(result);
        assertEquals(1500, result.get().calories());
    }

    private Goal buildGoal(Long id, LocalDate validFrom, LocalDate validUntil) {
        Goal goal = new Goal();
        ReflectionTestUtils.setField(goal, "id", id);
        goal.setUser(user);
        goal.setValidFrom(validFrom);
        goal.setValidUntil(validUntil);
        goal.setCalories(2000);
        goal.setCarbsPercent(new BigDecimal("50.00"));
        goal.setProteinPercent(new BigDecimal("25.00"));
        goal.setFatPercent(new BigDecimal("25.00"));
        goal.setCarbsCalories(new BigDecimal("1000.00"));
        goal.setProteinCalories(new BigDecimal("500.00"));
        goal.setFatCalories(new BigDecimal("500.00"));
        goal.setCarbsG(new BigDecimal("250.00"));
        goal.setProteinG(new BigDecimal("125.00"));
        goal.setFatG(new BigDecimal("55.56"));
        goal.setCreatedAt(Instant.now());
        goal.setUpdatedAt(Instant.now());
        return goal;
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}
