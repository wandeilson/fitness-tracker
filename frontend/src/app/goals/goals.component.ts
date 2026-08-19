import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { GoalPayload, GoalResponse, GoalsService } from './goals.service';

@Component({
  selector: 'app-goals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="goals-page">
      <div class="page-header">
        <h1>Metas diarias</h1>
        <p>Defina suas calorias e a distribuicao de macronutrientes.</p>
      </div>

      <div class="card">
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="field">
            <label class="label-text">Meta diaria (kcal)</label>
            <input type="number" formControlName="calories" (input)="recalculate()" />
          </div>

          <label class="checkbox-field">
            <input type="checkbox" formControlName="customizeDistribution" (change)="onCustomizeChange()" />
            <span>Personalizar distribuicao de macros</span>
          </label>

          <div class="percent-grid">
            <div class="field">
              <label class="label-text">Carboidratos (%)</label>
              <input type="number" formControlName="carbsPercent" (input)="recalculate()" />
            </div>
            <div class="field">
              <label class="label-text">Proteinas (%)</label>
              <input type="number" formControlName="proteinPercent" (input)="recalculate()" />
            </div>
            <div class="field">
              <label class="label-text">Gorduras (%)</label>
              <input type="number" formControlName="fatPercent" (input)="recalculate()" />
            </div>
          </div>

          @if (form.value.customizeDistribution) {
            <div class="sum-status" [class.ok]="sumPercent() === 100" [class.warn]="sumPercent() !== 100">
              Soma atual: {{ sumPercent() }}%
            </div>
          }

          @if (form.value.customizeDistribution && sumPercent() !== 100) {
            <div class="alert alert-error">A soma dos percentuais deve ser exatamente 100%.</div>
          }

          <div class="result-card">
            <h3>Distribuicao calculada</h3>
            <div class="result-grid">
              <div class="result-item">
                <span class="result-label carbs">Carboidratos</span>
                <span class="result-value">{{ macro.carbsG }} g</span>
                <span class="result-sub">{{ macro.carbsCalories }} kcal</span>
              </div>
              <div class="result-item">
                <span class="result-label protein">Proteinas</span>
                <span class="result-value">{{ macro.proteinG }} g</span>
                <span class="result-sub">{{ macro.proteinCalories }} kcal</span>
              </div>
              <div class="result-item">
                <span class="result-label fat">Gorduras</span>
                <span class="result-value">{{ macro.fatG }} g</span>
                <span class="result-sub">{{ macro.fatCalories }} kcal</span>
              </div>
            </div>
          </div>

          <button type="submit" class="btn-primary" [disabled]="isSaveDisabled()">
            {{ loading() ? 'Salvando...' : 'Salvar metas' }}
          </button>
        </form>

        @if (success()) {
          <div class="alert alert-success">Metas salvas com sucesso.</div>
        }
        @if (error()) {
          <div class="alert alert-error">{{ error() }}</div>
        }
      </div>
    </div>
  `,
  styles: `
    .goals-page {
      max-width: 560px;
    }

    .page-header {
      margin-bottom: 1.5rem;
    }

    .page-header h1 {
      margin-bottom: 0.25rem;
    }

    .page-header p {
      color: var(--color-text-secondary);
      font-size: 0.95rem;
    }

    .card {
      background: var(--color-surface);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-lg);
      padding: 1.75rem;
    }

    form {
      display: grid;
      gap: 1.1rem;
    }

    .field {
      display: grid;
      gap: 0.4rem;
    }

    .label-text {
      font-weight: 600;
      font-size: 0.88rem;
      color: var(--color-text);
    }

    .checkbox-field {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      font-weight: 500;
      font-size: 0.92rem;
      color: var(--color-text);
      cursor: pointer;
    }

    .checkbox-field input[type="checkbox"] {
      width: 18px;
      height: 18px;
      accent-color: var(--color-primary);
      cursor: pointer;
    }

    .percent-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.8rem;
    }

    .sum-status {
      font-weight: 600;
      font-size: 0.9rem;
      padding: 0.5rem 0.75rem;
      border-radius: var(--radius-sm);
    }

    .sum-status.ok {
      background: var(--color-success-bg);
      color: var(--color-success);
    }

    .sum-status.warn {
      background: var(--color-error-bg);
      color: var(--color-error);
    }

    .result-card {
      background: var(--color-bg);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-md);
      padding: 1.1rem 1.25rem;
    }

    .result-card h3 {
      margin-bottom: 0.75rem;
      font-size: 0.92rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .result-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.75rem;
    }

    .result-item {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }

    .result-label {
      font-size: 0.78rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }

    .result-label.carbs { color: #0891b2; }
    .result-label.protein { color: #7c3aed; }
    .result-label.fat { color: #d97706; }

    .result-value {
      font-size: 1.15rem;
      font-weight: 700;
      color: var(--color-text);
    }

    .result-sub {
      font-size: 0.8rem;
      color: var(--color-text-secondary);
    }

    .btn-primary {
      margin-top: 0.5rem;
      padding: 0.75rem 1.5rem;
      background: var(--color-primary);
      color: #fff;
      border: none;
      border-radius: var(--radius-md);
      font-weight: 600;
      font-size: 0.95rem;
      transition: background 0.15s, transform 0.1s;
    }

    .btn-primary:hover:not(:disabled) {
      background: var(--color-primary-hover);
    }

    .btn-primary:active:not(:disabled) {
      transform: scale(0.98);
    }

    .alert {
      margin-top: 1rem;
      padding: 0.7rem 0.85rem;
      border-radius: var(--radius-md);
      font-size: 0.88rem;
      font-weight: 500;
    }

    .alert-success {
      background: var(--color-success-bg);
      color: var(--color-success);
      border: 1px solid #a7f3d0;
    }

    .alert-error {
      background: var(--color-error-bg);
      color: var(--color-error);
      border: 1px solid #fecaca;
    }

    @media (max-width: 640px) {
      .percent-grid,
      .result-grid {
        grid-template-columns: 1fr;
      }
    }
  `
})
export class GoalsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly goalsService = inject(GoalsService);

  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal(false);

  protected macro = {
    carbsPercent: 50,
    proteinPercent: 25,
    fatPercent: 25,
    carbsCalories: 1000,
    proteinCalories: 500,
    fatCalories: 500,
    carbsG: 250,
    proteinG: 125,
    fatG: 55.56
  };

  protected readonly form = this.fb.group({
    calories: [2000, [Validators.required, Validators.min(1)]],
    customizeDistribution: [false],
    carbsPercent: [50, [Validators.required, Validators.min(0), Validators.max(100)]],
    proteinPercent: [25, [Validators.required, Validators.min(0), Validators.max(100)]],
    fatPercent: [25, [Validators.required, Validators.min(0), Validators.max(100)]]
  });

  ngOnInit(): void {
    this.goalsService.getGoal().subscribe({
      next: (goal) => this.applyGoal(goal),
      error: () => {}
    });

    this.onCustomizeChange();
    this.recalculate();
  }

  save(): void {
    if (this.form.invalid || (this.form.value.customizeDistribution && this.sumPercent() !== 100)) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.success.set(false);

    const raw = this.form.getRawValue();
    const payload: GoalPayload = {
      calories: Number(raw.calories ?? 0),
      carbsPercent: Number(raw.carbsPercent ?? 0),
      proteinPercent: Number(raw.proteinPercent ?? 0),
      fatPercent: Number(raw.fatPercent ?? 0)
    };

    this.goalsService.saveGoal(payload).subscribe({
      next: (goal) => {
        this.applyGoal(goal);
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Falha ao salvar metas');
        this.loading.set(false);
      }
    });
  }

  protected onCustomizeChange(): void {
    const customize = !!this.form.value.customizeDistribution;

    if (!customize) {
      this.form.patchValue({ carbsPercent: 50, proteinPercent: 25, fatPercent: 25 }, { emitEvent: false });
      this.form.controls.carbsPercent.disable({ emitEvent: false });
      this.form.controls.proteinPercent.disable({ emitEvent: false });
      this.form.controls.fatPercent.disable({ emitEvent: false });
    } else {
      this.form.controls.carbsPercent.enable({ emitEvent: false });
      this.form.controls.proteinPercent.enable({ emitEvent: false });
      this.form.controls.fatPercent.enable({ emitEvent: false });
    }

    this.recalculate();
  }

  protected sumPercent(): number {
    const raw = this.form.getRawValue();
    const carbs = Number(raw.carbsPercent ?? 0);
    const protein = Number(raw.proteinPercent ?? 0);
    const fat = Number(raw.fatPercent ?? 0);
    return Number((carbs + protein + fat).toFixed(2));
  }

  protected recalculate(): void {
    const raw = this.form.getRawValue();
    const calories = Number(raw.calories ?? 0);
    const carbsPercent = Number(raw.carbsPercent ?? 0);
    const proteinPercent = Number(raw.proteinPercent ?? 0);
    const fatPercent = Number(raw.fatPercent ?? 0);

    const carbsCalories = this.round2(calories * (carbsPercent / 100));
    const proteinCalories = this.round2(calories * (proteinPercent / 100));
    const fatCalories = this.round2(calories * (fatPercent / 100));

    this.macro = {
      carbsPercent,
      proteinPercent,
      fatPercent,
      carbsCalories,
      proteinCalories,
      fatCalories,
      carbsG: this.round2(carbsCalories / 4),
      proteinG: this.round2(proteinCalories / 4),
      fatG: this.round2(fatCalories / 9)
    };
  }

  protected isSaveDisabled(): boolean {
    if (this.loading() || this.form.invalid) {
      return true;
    }

    return !!this.form.value.customizeDistribution && this.sumPercent() !== 100;
  }

  private applyGoal(goal: GoalResponse): void {
    const customize = !(goal.carbsPercent === 50 && goal.proteinPercent === 25 && goal.fatPercent === 25);

    this.form.patchValue({
      calories: goal.calories,
      customizeDistribution: customize,
      carbsPercent: goal.carbsPercent,
      proteinPercent: goal.proteinPercent,
      fatPercent: goal.fatPercent
    }, { emitEvent: false });

    this.onCustomizeChange();
    this.recalculate();
  }

  private round2(value: number): number {
    return Number(value.toFixed(2));
  }
}
