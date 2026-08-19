import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import {
  DailySummaryResponse,
  MealResponse,
  MealType,
  MEAL_TYPE_ORDER,
} from '../models/meal.model';
import { GoalResponse, GoalsService } from '../goals/goals.service';
import { MealService } from './meal.service';
import { MealSectionComponent } from './meal-section.component';
import { DailySummaryComponent } from './daily-summary.component';

@Component({
  selector: 'app-daily-log-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatIconModule,
    MealSectionComponent,
    DailySummaryComponent,
  ],
  template: `
    <div class="daily-page">
      <div class="page-header">
        <h1>Diario</h1>
        <div class="date-nav">
          <button class="nav-btn" (click)="prevDay()">
            <mat-icon>chevron_left</mat-icon>
          </button>
          <input
            type="date"
            [formControl]="dateControl"
            class="date-input"
          />
          <button class="nav-btn" (click)="nextDay()">
            <mat-icon>chevron_right</mat-icon>
          </button>
          @if (!isToday()) {
            <button class="today-btn" (click)="goToday()">Hoje</button>
          }
        </div>
      </div>

      @if (summary() && goal()) {
        <app-daily-summary [summary]="summary()!" [goal]="goal()!" />
      }

      <div class="sections">
        @for (type of mealTypes; track type) {
          <app-meal-section
            [mealType]="type"
            [mealDate]="dateControl.value"
            [items]="getItemsForType(type)"
            [mealId]="getMealIdForType(type)"
            (refresh)="loadData()"
          />
        }
      </div>
    </div>
  `,
  styles: `
    .daily-page {
      max-width: 720px;
    }

    .page-header {
      margin-bottom: 1.5rem;
    }

    .page-header h1 {
      margin-bottom: 0.75rem;
    }

    .date-nav {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .nav-btn {
      width: 36px;
      height: 36px;
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-md);
      background: var(--color-surface);
      color: var(--color-text);
      display: grid;
      place-items: center;
      transition: border-color 0.15s, background 0.15s;
    }

    .nav-btn:hover {
      border-color: var(--color-primary);
      background: var(--color-primary-bg);
    }

    .nav-btn mat-icon {
      font-size: 20px;
    }

    .date-input {
      padding: 0.5rem 0.85rem;
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-md);
      background: var(--color-surface);
      color: var(--color-text);
      font-weight: 600;
      font-size: 0.92rem;
      transition: border-color 0.15s;
    }

    .date-input:focus {
      outline: none;
      border-color: var(--color-border-focus);
      box-shadow: 0 0 0 3px var(--color-primary-light);
    }

    .today-btn {
      padding: 0.4rem 0.85rem;
      border: 1.5px solid var(--color-primary);
      border-radius: var(--radius-md);
      background: var(--color-primary-bg);
      color: var(--color-primary);
      font-weight: 600;
      font-size: 0.85rem;
      transition: background 0.15s;
    }

    .today-btn:hover {
      background: var(--color-primary-light);
    }

    .sections {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-top: 1.25rem;
    }
  `,
})
export class DailyLogPageComponent implements OnInit {
  private readonly mealService = inject(MealService);
  private readonly goalsService = inject(GoalsService);

  protected readonly dateControl = new FormControl(this.formatDate(new Date()), {
    nonNullable: true,
  });
  protected readonly meals = signal<MealResponse[]>([]);
  protected readonly summary = signal<DailySummaryResponse | null>(null);
  protected readonly goal = signal<GoalResponse | null>(null);
  protected readonly mealTypes = MEAL_TYPE_ORDER;

  ngOnInit(): void {
    this.loadData();
    this.goalsService.getGoal().subscribe({
      next: (g) => this.goal.set(g),
      error: () => {},
    });

    this.dateControl.valueChanges.subscribe(() => this.loadData());
  }

  protected prevDay(): void {
    const current = new Date(this.dateControl.value);
    current.setDate(current.getDate() - 1);
    this.dateControl.setValue(this.formatDate(current), { emitEvent: false });
    this.loadData();
  }

  protected nextDay(): void {
    const current = new Date(this.dateControl.value);
    current.setDate(current.getDate() + 1);
    this.dateControl.setValue(this.formatDate(current), { emitEvent: false });
    this.loadData();
  }

  protected goToday(): void {
    this.dateControl.setValue(this.formatDate(new Date()), { emitEvent: false });
    this.loadData();
  }

  protected isToday(): boolean {
    return this.dateControl.value === this.formatDate(new Date());
  }

  protected getItemsForType(type: MealType) {
    const meal = this.meals().find((m) => m.mealType === type);
    return meal?.items ?? [];
  }

  protected getMealIdForType(type: MealType): number | null {
    const meal = this.meals().find((m) => m.mealType === type);
    return meal?.id ?? null;
  }

  protected loadData(): void {
    const date = this.dateControl.value;
    this.mealService.getMeals(date).subscribe({
      next: (meals) => this.meals.set(meals),
      error: () => this.meals.set([]),
    });
    this.mealService.getSummary(date).subscribe({
      next: (summary) => this.summary.set(summary),
      error: () => this.summary.set(null),
    });
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
