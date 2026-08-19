import { Component, input } from '@angular/core';
import { DailySummaryResponse } from '../models/meal.model';
import { GoalResponse } from '../goals/goals.service';

interface MacroBar {
  label: string;
  consumed: number;
  goal: number;
  unit: string;
  color: string;
}

@Component({
  selector: 'app-daily-summary',
  standalone: true,
  template: `
    <div class="summary-card">
      <h3>Resumo do dia</h3>
      <div class="bars">
        @for (bar of bars(); track bar.label) {
          <div class="bar-row">
            <div class="bar-header">
              <span class="bar-label">{{ bar.label }}</span>
              <span class="bar-value">
                {{ formatNum(bar.consumed) }} / {{ formatNum(bar.goal) }} {{ bar.unit }}
              </span>
            </div>
            <div class="bar-track">
              <div
                class="bar-fill"
                [style.width.%]="getPercent(bar)"
                [style.background]="bar.color"
                [class.over]="getPercent(bar) > 100"
              ></div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: `
    .summary-card {
      background: var(--color-surface);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-lg);
      padding: 1.25rem 1.5rem;
    }

    h3 {
      margin-bottom: 1rem;
      font-size: 0.95rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .bars {
      display: flex;
      flex-direction: column;
      gap: 0.9rem;
    }

    .bar-row {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
    }

    .bar-header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
    }

    .bar-label {
      font-weight: 600;
      font-size: 0.88rem;
      color: var(--color-text);
    }

    .bar-value {
      font-size: 0.82rem;
      color: var(--color-text-secondary);
      font-weight: 500;
    }

    .bar-track {
      height: 10px;
      background: var(--color-bg);
      border-radius: var(--radius-full);
      overflow: hidden;
      border: 1px solid var(--color-border);
    }

    .bar-fill {
      height: 100%;
      border-radius: var(--radius-full);
      transition: width 0.4s ease;
      min-width: 0;
    }

    .bar-fill.over {
      opacity: 0.8;
    }
  `,
})
export class DailySummaryComponent {
  summary = input.required<DailySummaryResponse>();
  goal = input.required<GoalResponse>();

  bars = (): MacroBar[] => {
    const s = this.summary();
    const g = this.goal();
    return [
      { label: 'Calorias', consumed: s.kcalTotal, goal: g.calories, unit: 'kcal', color: '#0d9488' },
      { label: 'Carboidratos', consumed: s.carbsTotal, goal: g.carbsG, unit: 'g', color: '#0891b2' },
      { label: 'Proteinas', consumed: s.proteinTotal, goal: g.proteinG, unit: 'g', color: '#7c3aed' },
      { label: 'Gorduras', consumed: s.fatTotal, goal: g.fatG, unit: 'g', color: '#d97706' },
    ];
  };

  getPercent(bar: MacroBar): number {
    if (bar.goal <= 0) return 0;
    return Math.min((bar.consumed / bar.goal) * 100, 100);
  }

  formatNum(value: number): string {
    return value % 1 === 0 ? value.toString() : value.toFixed(1);
  }
}
