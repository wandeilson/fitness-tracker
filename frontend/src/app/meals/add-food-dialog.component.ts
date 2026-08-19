import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { FoodResponse } from '../models/food.model';

export interface AddFoodDialogData {
  food: FoodResponse;
}

@Component({
  selector: 'app-add-food-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
  ],
  template: `
    <div class="add-food-dialog">
      <h2 mat-dialog-title>Adicionar alimento</h2>

      <mat-dialog-content>
        <div class="food-header">
          <span class="food-name">{{ data.food.name }}</span>
          <span class="food-source">{{ data.food.source }}</span>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Quantidade (gramas)</mat-label>
          <input
            matInput
            type="number"
            [formControl]="gramsControl"
            min="1"
            autofocus
          />
          <span matTextSuffix>g</span>
        </mat-form-field>

        @if (gramsControl.value && gramsControl.value > 0) {
          <div class="preview">
            <h4>Valores para {{ gramsControl.value }}g</h4>
            <div class="preview-grid">
              <div class="preview-item kcal">
                <span class="preview-value">{{ calc(gramsControl.value, data.food.kcalPer100g) }}</span>
                <span class="preview-label">kcal</span>
              </div>
              <div class="preview-item carb">
                <span class="preview-value">{{ calc(gramsControl.value, data.food.carbsPer100g) }}g</span>
                <span class="preview-label">Carboidratos</span>
              </div>
              <div class="preview-item prot">
                <span class="preview-value">{{ calc(gramsControl.value, data.food.proteinPer100g) }}g</span>
                <span class="preview-label">Proteinas</span>
              </div>
              <div class="preview-item fat">
                <span class="preview-value">{{ calc(gramsControl.value, data.food.fatPer100g) }}g</span>
                <span class="preview-label">Gorduras</span>
              </div>
            </div>
          </div>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Cancelar</button>
        <button
          mat-flat-button
          color="primary"
          [disabled]="gramsControl.invalid || loading()"
          (click)="confirm()"
        >
          {{ loading() ? 'Adicionando...' : 'Adicionar' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: `
    .add-food-dialog {
      min-width: 380px;
      max-width: 460px;
    }

    .full-width {
      width: 100%;
      margin-top: 0.5rem;
    }

    .food-header {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
      padding: 0.5rem 0;
    }

    .food-name {
      font-weight: 700;
      font-size: 1.05rem;
      color: var(--color-text);
    }

    .food-source {
      font-size: 0.78rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
    }

    .preview {
      background: var(--color-bg);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-md);
      padding: 1rem;
      margin-top: 0.25rem;
    }

    .preview h4 {
      font-size: 0.82rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.3px;
      margin-bottom: 0.6rem;
    }

    .preview-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 0.5rem;
    }

    .preview-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.1rem;
    }

    .preview-value {
      font-size: 1.05rem;
      font-weight: 700;
      color: var(--color-text);
    }

    .preview-label {
      font-size: 0.72rem;
      color: var(--color-text-secondary);
      text-align: center;
    }

    .preview-item.kcal .preview-value { color: var(--color-primary); }
    .preview-item.carb .preview-value { color: #0891b2; }
    .preview-item.prot .preview-value { color: #7c3aed; }
    .preview-item.fat .preview-value { color: #d97706; }

    @media (max-width: 480px) {
      .add-food-dialog {
        min-width: unset;
        width: 100%;
      }

      .preview-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  `,
})
export class AddFoodDialogComponent {
  protected readonly data = inject<AddFoodDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<AddFoodDialogComponent>);

  protected readonly gramsControl = new FormControl(100, [
    Validators.required,
    Validators.min(1),
    Validators.max(10000),
  ]);
  protected readonly loading = signal(false);

  protected calc(grams: number, per100: number): string {
    const value = (per100 * grams) / 100;
    return value % 1 === 0 ? value.toString() : value.toFixed(1);
  }

  confirm(): void {
    if (this.gramsControl.invalid) return;
    this.dialogRef.close({ grams: this.gramsControl.value });
  }
}
