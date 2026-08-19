import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivityLevel, ProfileService, Sex } from './profile.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="profile-page">
      <div class="page-header">
        <h1>Perfil</h1>
        <p>Gerencie seus dados pessoais e nivel de atividade.</p>
      </div>

      <div class="card">
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="field">
            <label class="label-text">Nome completo</label>
            <input type="text" formControlName="fullName" />
          </div>

          <div class="field-row">
            <div class="field">
              <label class="label-text">Idade</label>
              <input type="number" formControlName="age" />
            </div>
            <div class="field">
              <label class="label-text">Peso (kg)</label>
              <input type="number" step="0.1" formControlName="weightKg" />
            </div>
            <div class="field">
              <label class="label-text">Altura (cm)</label>
              <input type="number" formControlName="heightCm" />
            </div>
          </div>

          <div class="field-row">
            <div class="field">
              <label class="label-text">Sexo</label>
              <select formControlName="sex">
                <option [ngValue]="null">Selecione</option>
                @for (option of sexOptions; track option.value) {
                  <option [value]="option.value">{{ option.label }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label class="label-text">Nivel de atividade</label>
              <select formControlName="activityLevel">
                <option [ngValue]="null">Selecione</option>
                @for (option of activityLevelOptions; track option.value) {
                  <option [value]="option.value">{{ option.label }}</option>
                }
              </select>
            </div>
          </div>

          <button type="submit" class="btn-primary" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Salvando...' : 'Salvar perfil' }}
          </button>
        </form>

        @if (success()) {
          <div class="alert alert-success">Perfil salvo com sucesso.</div>
        }
        @if (error()) {
          <div class="alert alert-error">{{ error() }}</div>
        }
      </div>
    </div>
  `,
  styles: `
    .profile-page {
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

    .field-row {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.8rem;
    }

    .label-text {
      font-weight: 600;
      font-size: 0.88rem;
      color: var(--color-text);
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
      .field-row {
        grid-template-columns: 1fr;
      }
    }
  `
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);

  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal(false);

  protected readonly sexOptions: ReadonlyArray<{ value: Sex; label: string }> = [
    { value: 'MALE', label: 'Masculino' },
    { value: 'FEMALE', label: 'Feminino' }
  ];

  protected readonly activityLevelOptions: ReadonlyArray<{ value: ActivityLevel; label: string }> = [
    { value: 'SEDENTARY', label: 'Sedentario' },
    { value: 'LIGHTLY_ACTIVE', label: 'Levemente ativo' },
    { value: 'MODERATELY_ACTIVE', label: 'Moderadamente ativo' },
    { value: 'VERY_ACTIVE', label: 'Muito ativo' },
    { value: 'ATHLETE', label: 'Atleta' }
  ];

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(255)]],
    age: [null as number | null],
    weightKg: [null as number | null],
    heightCm: [null as number | null],
    sex: [null as Sex | null],
    activityLevel: [null as ActivityLevel | null]
  });

  ngOnInit(): void {
    this.profileService.getProfile().subscribe({
      next: (profile) => this.form.patchValue(profile),
      error: (err) => this.error.set(err?.error?.message ?? 'Falha ao carregar perfil')
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.success.set(false);

    this.profileService.saveProfile(this.form.getRawValue()).subscribe({
      next: (profile) => {
        this.form.patchValue(profile);
        this.success.set(true);
      },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Falha ao salvar perfil');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
