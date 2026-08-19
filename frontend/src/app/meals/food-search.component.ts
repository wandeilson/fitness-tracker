import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged, switchMap, finalize } from 'rxjs';
import { FoodResponse } from '../models/food.model';
import { FoodService } from '../foods/food.service';

@Component({
  selector: 'app-food-search',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="food-search-dialog">
      <h2 mat-dialog-title>Buscar alimento</h2>

      <mat-dialog-content>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome do alimento</mat-label>
          <input
            matInput
            [formControl]="searchControl"
            placeholder="Ex: arroz, feijao, frango..."
            autofocus
          />
          <mat-icon matPrefix>search</mat-icon>
        </mat-form-field>

        @if (loading()) {
          <div class="loading">
            <mat-spinner diameter="28"></mat-spinner>
          </div>
        }

        @if (results().length > 0) {
          <div class="results">
            @for (food of results(); track food.id) {
              <button class="food-item" (click)="select(food)">
                <div class="food-info">
                  <span class="food-name">{{ food.name }}</span>
                  <span class="food-source">{{ food.source }}</span>
                </div>
                <div class="food-macros">
                  <span class="macro kcal">{{ food.kcalPer100g }} kcal</span>
                  <span class="macro carb">C {{ food.carbsPer100g }}g</span>
                  <span class="macro prot">P {{ food.proteinPer100g }}g</span>
                  <span class="macro fat">G {{ food.fatPer100g }}g</span>
                </div>
              </button>
            }
          </div>
        }

        @if (searched() && !loading() && results().length === 0) {
          <div class="empty">Nenhum alimento encontrado.</div>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Cancelar</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: `
    .food-search-dialog {
      min-width: 420px;
      max-width: 520px;
    }

    .full-width {
      width: 100%;
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 1.5rem;
    }

    .results {
      max-height: 320px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      margin-top: 0.5rem;
    }

    .food-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      padding: 0.7rem 0.85rem;
      background: var(--color-bg);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: border-color 0.15s, background 0.15s;
      text-align: left;
    }

    .food-item:hover {
      border-color: var(--color-primary);
      background: var(--color-primary-bg);
    }

    .food-info {
      display: flex;
      flex-direction: column;
      gap: 0.1rem;
      min-width: 0;
    }

    .food-name {
      font-weight: 600;
      font-size: 0.92rem;
      color: var(--color-text);
    }

    .food-source {
      font-size: 0.78rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
    }

    .food-macros {
      display: flex;
      gap: 0.5rem;
      flex-shrink: 0;
      font-size: 0.78rem;
      font-weight: 600;
    }

    .macro {
      padding: 0.15rem 0.4rem;
      border-radius: var(--radius-sm);
    }

    .macro.kcal { background: var(--color-primary-bg); color: var(--color-primary); }
    .macro.carb { background: #ecfeff; color: #0891b2; }
    .macro.prot { background: #f5f3ff; color: #7c3aed; }
    .macro.fat { background: #fffbeb; color: #d97706; }

    .empty {
      text-align: center;
      padding: 2rem 1rem;
      color: var(--color-text-secondary);
      font-size: 0.92rem;
    }

    @media (max-width: 480px) {
      .food-search-dialog {
        min-width: unset;
        width: 100%;
      }

      .food-macros {
        display: none;
      }
    }
  `,
})
export class FoodSearchComponent {
  private readonly foodService = inject(FoodService);
  private readonly dialogRef = inject(MatDialogRef<FoodSearchComponent>);

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly results = signal<FoodResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly searched = signal(false);

  constructor() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) => {
          if (query.trim().length < 2) {
            this.results.set([]);
            this.searched.set(false);
            return [];
          }
          this.loading.set(true);
          this.searched.set(true);
          return this.foodService.search(query).pipe(finalize(() => this.loading.set(false)));
        })
      )
      .subscribe((foods) => this.results.set(foods));
  }

  select(food: FoodResponse): void {
    this.dialogRef.close(food);
  }
}
