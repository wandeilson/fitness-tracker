package com.fitness.backend.goal;

import com.fitness.backend.goal.dto.GoalRequest;
import com.fitness.backend.goal.dto.GoalResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PutMapping
    public ResponseEntity<GoalResponse> upsertGoal(@Valid @RequestBody GoalRequest request, Authentication authentication) {
        return ResponseEntity.ok(goalService.upsertGoal(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<GoalResponse> getGoal(Authentication authentication) {
        return ResponseEntity.ok(goalService.getGoal(authentication.getName()));
    }
}
