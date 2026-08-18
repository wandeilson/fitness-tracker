package com.fitness.backend.meal;

import com.fitness.backend.food.Food;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "meal_items")
public class MealItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false)
    private BigDecimal grams;

    @Column(name = "kcal_consumed", nullable = false)
    private BigDecimal kcalConsumed;

    @Column(name = "carbs_consumed", nullable = false)
    private BigDecimal carbsConsumed;

    @Column(name = "protein_consumed", nullable = false)
    private BigDecimal proteinConsumed;

    @Column(name = "fat_consumed", nullable = false)
    private BigDecimal fatConsumed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public BigDecimal getGrams() {
        return grams;
    }

    public void setGrams(BigDecimal grams) {
        this.grams = grams;
    }

    public BigDecimal getKcalConsumed() {
        return kcalConsumed;
    }

    public void setKcalConsumed(BigDecimal kcalConsumed) {
        this.kcalConsumed = kcalConsumed;
    }

    public BigDecimal getCarbsConsumed() {
        return carbsConsumed;
    }

    public void setCarbsConsumed(BigDecimal carbsConsumed) {
        this.carbsConsumed = carbsConsumed;
    }

    public BigDecimal getProteinConsumed() {
        return proteinConsumed;
    }

    public void setProteinConsumed(BigDecimal proteinConsumed) {
        this.proteinConsumed = proteinConsumed;
    }

    public BigDecimal getFatConsumed() {
        return fatConsumed;
    }

    public void setFatConsumed(BigDecimal fatConsumed) {
        this.fatConsumed = fatConsumed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
