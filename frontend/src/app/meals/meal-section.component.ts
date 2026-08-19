import { Component, inject, input, output, signal } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { FoodResponse } from '../models/food.model';
import { MealItemResponse, MealType, MEAL_TYPE_LABELS } from '../models/meal.model';
import { FoodSearchComponent } from './food-search.component';
import { AddFoodDialogComponent, AddFoodDialogData } from './add-food-dialog.component';
import { MealService } from './meal.service';

@Component({
  selector: 'app-meal-section',
  standalone: true,
  imports: [MatDialogModule, MatIconModule],
  template: `
    <div class="meal-section">
      <div class="section-header">
        <div class="section-title">
          <h3>{{ label }}</h3>
          @if (totalKcal() > 0) {
            <span class="section-total">{{ totalKcal() }} kcal</span>
          }
        </div>
        <button class="add-btn" (click)="openSearch()" title="Adicionar alimento">
          <mat-icon>add</mat-icon>
        </button>
      </div>

      @if (items().length === 0) {
        <div class="empty-section">
          Nenhum alimento registrado.
        </div>
      } @else {
        <div class="items">
          @for (item of items(); track item.id) {
            <div class="item-row">
              <div class="item-info">
                <span class="item-name">{{ item.foodName }}</span>
                <span class="item-grams">{{ item.grams }}g</span>
              </div>
              <div class="item-macros">
                <span class="macro kcal">{{ round(item.kcalConsumed) }} kcal</span>
                <span class="macro carb">{{ round(item.carbsConsumed) }}g</span>
                <span class="macro prot">{{ round(item.proteinConsumed) }}g</span>
                <span class="macro fat">{{ round(item.fatConsumed) }}g</span>
              </div>
              <div class="item-actions">
                <button class="icon-btn delete" (click)="removeItem(item)" title="Remover">
                  <mat-icon>close</mat-icon>
                </button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .meal-section {
      background: var(--color-surface);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-lg);
      overflow: hidden;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.85rem 1.1rem;
      background: var(--color-bg);
      border-bottom: 1px solid var(--color-border);
    }

    .section-title {
      display: flex;
      align-items: baseline;
      gap: 0.75rem;
    }

    .section-title h3 {
      margin: 0;
      font-size: 0.95rem;
    }

    .section-total {
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--color-primary);
    }

    .add-btn {
      width: 32px;
      height: 32px;
      border-radius: var(--radius-full);
      border: 1.5px solid var(--color-border);
      background: var(--color-surface);
      color: var(--color-primary);
      display: grid;
      place-items: center;
      transition: background 0.15s, border-color 0.15s;
    }

    .add-btn:hover {
      background: var(--color-primary-bg);
      border-color: var(--color-primary);
    }

    .add-btn mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .empty-section {
      padding: 1rem 1.1rem;
      color: var(--color-text-secondary);
      font-size: 0.88rem;
      text-align: center;
    }

    .items {
      display: flex;
      flex-direction: column;
    }

    .item-row {
      display: flex;
      align-items: center;
      padding: 0.65rem 1.1rem;
      border-bottom: 1px solid var(--color-border);
      gap: 0.75rem;
    }

    .item-row:last-child {
      border-bottom: none;
    }

    .item-info {
      display: flex;
      flex-direction: column;
      gap: 0.05rem;
      min-width: 0;
      flex: 1;
    }

    .item-name {
      font-weight: 600;
      font-size: 0.9rem;
      color: var(--color-text);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .item-grams {
      font-size: 0.78rem;
      color: var(--color-text-secondary);
    }

    .item-macros {
      display: flex;
      gap: 0.4rem;
      flex-shrink: 0;
    }

    .macro {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.15rem 0.35rem;
      border-radius: var(--radius-sm);
    }

    .macro.kcal { background: var(--color-primary-bg); color: var(--color-primary); }
    .macro.carb { background: #ecfeff; color: #0891b2; }
    .macro.prot { background: #f5f3ff; color: #7c3aed; }
    .macro.fat { background: #fffbeb; color: #d97706; }

    .item-actions {
      flex-shrink: 0;
    }

    .icon-btn {
      width: 28px;
      height: 28px;
      border: none;
      background: transparent;
      border-radius: var(--radius-full);
      display: grid;
      place-items: center;
      color: var(--color-text-secondary);
      transition: background 0.15s, color 0.15s;
    }

    .icon-btn:hover {
      background: var(--color-error-bg);
      color: var(--color-error);
    }

    .icon-btn mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    @media (max-width: 640px) {
      .item-macros {
        display: none;
      }
    }
  `,
})
export class MealSectionComponent {
  private readonly dialog = inject(MatDialog);
  private readonly mealService = inject(MealService);

  mealType = input.required<MealType>();
  mealDate = input.required<string>();
  items = input.required<MealItemResponse[]>();
  mealId = input<number | null>(null);

  refresh = output<void>();

  protected readonly adding = signal(false);

  get label(): string {
    return MEAL_TYPE_LABELS[this.mealType()];
  }

  protected totalKcal(): number {
    return this.items().reduce((sum, item) => sum + item.kcalConsumed, 0);
  }

  protected round(value: number): string {
    return value % 1 === 0 ? value.toString() : value.toFixed(1);
  }

  openSearch(): void {
    const ref = this.dialog.open(FoodSearchComponent, { panelClass: 'food-search-panel' });
    ref.afterClosed().subscribe((food) => {
      if (food) this.openAddDialog(food);
    });
  }

  private openAddDialog(food: FoodResponse): void {
    const ref = this.dialog.open(AddFoodDialogComponent, {
      data: { food } as AddFoodDialogData,
      panelClass: 'add-food-panel',
    });
    ref.afterClosed().subscribe((result) => {
      if (result?.grams) this.addItem(food.id, result.grams);
    });
  }

  private addItem(foodId: number, grams: number): void {
    this.adding.set(true);

    const ensureMeal$ = this.mealId()
      ? new Promise<number>((resolve) => resolve(this.mealId()!))
      : new Promise<number>((resolve) => {
          this.mealService
            .createMeal({ mealDate: this.mealDate(), mealType: this.mealType() })
            .subscribe((meal) => resolve(meal.id));
        });

    ensureMeal$.then((mealId) => {
      this.mealService.addItem(mealId, { foodId, grams }).subscribe({
        next: () => {
          this.adding.set(false);
          this.refresh.emit();
        },
        error: () => this.adding.set(false),
      });
    });
  }

  protected removeItem(item: MealItemResponse): void {
    if (!this.mealId()) return;
    this.mealService.deleteItem(this.mealId()!, item.id).subscribe(() => {
      this.refresh.emit();
    });
  }
}
