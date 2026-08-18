package com.fitness.backend.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import java.math.BigDecimal;
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
        when(goalRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoal("user@test.com", new GoalRequest(2000, null, null, null));

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
        when(goalRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoal("user@test.com", new GoalRequest(2000, 40.0, 30.0, 30.0));

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
    void getGoalShouldThrowWhenGoalDoesNotExist() {
        when(goalRepository.findByUserId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> goalService.getGoal("user@test.com")
        );

        assertEquals("Goal not found", ex.getMessage());
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}
