import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProfileService } from './profile.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <main class="page">
      <header>
        <h1>Perfil</h1>
        <a routerLink="/">Voltar</a>
      </header>

      <section class="card">
        <form [formGroup]="form" (ngSubmit)="save()">
          <label>Nome completo <input type="text" formControlName="fullName" /></label>
          <label>Idade <input type="number" formControlName="age" /></label>
          <label>Peso (kg) <input type="number" step="0.1" formControlName="weightKg" /></label>
          <label>Altura (cm) <input type="number" formControlName="heightCm" /></label>
          <label>Sexo <input type="text" formControlName="sex" placeholder="ex: masculino/feminino" /></label>
          <label>Nível de atividade <input type="text" formControlName="activityLevel" placeholder="ex: leve/moderado" /></label>

          <button type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Salvando...' : 'Salvar perfil' }}
          </button>
        </form>

        <p class="ok" *ngIf="success()">Perfil salvo com sucesso.</p>
        <p class="error" *ngIf="error()">{{ error() }}</p>
      </section>
    </main>
  `,
  styles: `
    .page { min-height: 100vh; background: #f7fafc; padding: 2rem; }
    header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
    .card { background: #fff; border-radius: 14px; padding: 1.2rem; box-shadow: 0 8px 24px rgba(0,0,0,0.08); max-width: 520px; }
    form { display: grid; gap: .8rem; }
    label { display: grid; gap: .4rem; color: #334e68; font-weight: 600; }
    input { border: 1px solid #bcccdc; border-radius: 10px; padding: .65rem .75rem; }
    button { margin-top: .6rem; background: #0f766e; color: #fff; border: 0; border-radius: 10px; padding: .7rem; font-weight: 600; }
    .ok { color: #067647; margin-top: .75rem; }
    .error { color: #b42318; margin-top: .75rem; }
  `
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);

  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal(false);

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(255)]],
    age: [null as number | null],
    weightKg: [null as number | null],
    heightCm: [null as number | null],
    sex: [null as string | null],
    activityLevel: [null as string | null]
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
      error: (err) => this.error.set(err?.error?.message ?? 'Falha ao salvar perfil'),
      complete: () => this.loading.set(false)
    });
  }
}
