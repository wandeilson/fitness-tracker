package com.fitness.backend.goal;

import com.fitness.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer calories;

    @Column(name = "carbs_percent", nullable = false)
    private BigDecimal carbsPercent;

    @Column(name = "protein_percent", nullable = false)
    private BigDecimal proteinPercent;

    @Column(name = "fat_percent", nullable = false)
    private BigDecimal fatPercent;

    @Column(name = "carbs_calories", nullable = false)
    private BigDecimal carbsCalories;

    @Column(name = "protein_calories", nullable = false)
    private BigDecimal proteinCalories;

    @Column(name = "fat_calories", nullable = false)
    private BigDecimal fatCalories;

    @Column(name = "carbs_g", nullable = false)
    private BigDecimal carbsG;

    @Column(name = "protein_g", nullable = false)
    private BigDecimal proteinG;

    @Column(name = "fat_g", nullable = false)
    private BigDecimal fatG;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public BigDecimal getCarbsPercent() {
        return carbsPercent;
    }

    public void setCarbsPercent(BigDecimal carbsPercent) {
        this.carbsPercent = carbsPercent;
    }

    public BigDecimal getProteinPercent() {
        return proteinPercent;
    }

    public void setProteinPercent(BigDecimal proteinPercent) {
        this.proteinPercent = proteinPercent;
    }

    public BigDecimal getFatPercent() {
        return fatPercent;
    }

    public void setFatPercent(BigDecimal fatPercent) {
        this.fatPercent = fatPercent;
    }

    public BigDecimal getCarbsCalories() {
        return carbsCalories;
    }

    public void setCarbsCalories(BigDecimal carbsCalories) {
        this.carbsCalories = carbsCalories;
    }

    public BigDecimal getProteinCalories() {
        return proteinCalories;
    }

    public void setProteinCalories(BigDecimal proteinCalories) {
        this.proteinCalories = proteinCalories;
    }

    public BigDecimal getFatCalories() {
        return fatCalories;
    }

    public void setFatCalories(BigDecimal fatCalories) {
        this.fatCalories = fatCalories;
    }

    public BigDecimal getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(BigDecimal carbsG) {
        this.carbsG = carbsG;
    }

    public BigDecimal getProteinG() {
        return proteinG;
    }

    public void setProteinG(BigDecimal proteinG) {
        this.proteinG = proteinG;
    }

    public BigDecimal getFatG() {
        return fatG;
    }

    public void setFatG(BigDecimal fatG) {
        this.fatG = fatG;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
