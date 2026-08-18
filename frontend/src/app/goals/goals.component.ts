import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { GoalPayload, GoalResponse, GoalsService } from './goals.service';

@Component({
  selector: 'app-goals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="page">
      <div class="layout">
      <header>
        <h1>Metas diárias</h1>
        <a class="back-btn" routerLink="/">Voltar</a>
      </header>

      <section class="card">
        <form [formGroup]="form" (ngSubmit)="save()">
          <label>Meta diária (kcal) <input type="number" formControlName="calories" (input)="recalculate()" /></label>

          <label class="checkbox">
            <input type="checkbox" formControlName="customizeDistribution" (change)="onCustomizeChange()" />
            Personalizar distribuição
          </label>

          <div class="percent-grid">
            <label>Carboidratos (%) <input type="number" formControlName="carbsPercent" (input)="recalculate()" /></label>
            <label>Proteínas (%) <input type="number" formControlName="proteinPercent" (input)="recalculate()" /></label>
            <label>Gorduras (%) <input type="number" formControlName="fatPercent" (input)="recalculate()" /></label>
          </div>

          <p class="sum" *ngIf="form.value.customizeDistribution" [class.sum-ok]="sumPercent() === 100" [class.sum-warn]="sumPercent() !== 100">
            Soma atual: {{ sumPercent() }}%
          </p>

          <p class="error" *ngIf="form.value.customizeDistribution && sumPercent() !== 100">
            A soma dos percentuais deve ser exatamente 100%.
          </p>

          <div class="result-grid">
            <h3>Distribuição calculada</h3>
            <p>Carboidratos: {{ macro.carbsPercent }}% | {{ macro.carbsCalories }} kcal | {{ macro.carbsG }} g</p>
            <p>Proteínas: {{ macro.proteinPercent }}% | {{ macro.proteinCalories }} kcal | {{ macro.proteinG }} g</p>
            <p>Gorduras: {{ macro.fatPercent }}% | {{ macro.fatCalories }} kcal | {{ macro.fatG }} g</p>
          </div>

          <button type="submit" [disabled]="isSaveDisabled()">
            {{ loading() ? 'Salvando...' : 'Salvar metas' }}
          </button>
        </form>

        <p class="ok" *ngIf="success()">Metas salvas com sucesso.</p>
        <p class="error" *ngIf="error()">{{ error() }}</p>
      </section>
      </div>
    </main>
  `,
  styles: `
    .page { min-height: 100vh; background: #f7fafc; padding: 2rem 1rem; display: flex; align-items: center; justify-content: center; }
    .layout { width: min(100%, 560px); }
    header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
    .back-btn {
      text-decoration: none;
      background: #334e68;
      color: #fff;
      border-radius: 10px;
      padding: .5rem .8rem;
      font-weight: 700;
    }
    .card { background: #fff; border-radius: 14px; padding: 1.2rem; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
    form { display: grid; gap: .8rem; }
    label { display: grid; gap: .4rem; color: #334e68; font-weight: 600; }
    input { border: 1px solid #bcccdc; border-radius: 10px; padding: .65rem .75rem; }
    .checkbox { grid-template-columns: 22px 1fr; align-items: center; font-weight: 500; }
    .percent-grid { display: grid; grid-template-columns: 1fr; gap: .8rem; }
    .sum { margin-top: .2rem; font-weight: 600; }
    .sum-ok { color: #067647; }
    .sum-warn { color: #b42318; }
    .result-grid { background: #f0f4f8; border-radius: 10px; padding: .9rem; margin-top: .5rem; }
    h3 { margin: 0 0 .5rem; color: #102a43; font-size: 1rem; }
    p { margin: .2rem 0; color: #334e68; }
    button { margin-top: .6rem; background: #0f766e; color: #fff; border: 0; border-radius: 10px; padding: .7rem; font-weight: 600; }
    .ok { color: #067647; margin-top: .75rem; }
    .error { color: #b42318; margin-top: .75rem; }
    @media (max-width: 640px) {
      .page { padding: 1rem .8rem; align-items: flex-start; }
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
