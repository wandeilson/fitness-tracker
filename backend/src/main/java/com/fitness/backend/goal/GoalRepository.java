package com.fitness.backend.goal;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    @Query("""
        SELECT g FROM Goal g
        WHERE g.user.id = :userId
          AND g.validFrom <= :date
          AND (g.validUntil IS NULL OR g.validUntil >= :date)
    """)
    Optional<Goal> findActiveByUserIdAndDate(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );

    @Query("""
        SELECT g FROM Goal g
        WHERE g.user.id = :userId
          AND g.validUntil IS NULL
    """)
    Optional<Goal> findCurrentByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT g FROM Goal g
        WHERE g.user.id = :userId
          AND g.validUntil IS NOT NULL
        ORDER BY g.validUntil DESC
    """)
    Optional<Goal> findMostRecentClosedByUserId(@Param("userId") Long userId);
}
