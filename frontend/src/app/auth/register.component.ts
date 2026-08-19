import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-left">
        <div class="auth-brand">
          <span class="logo">FT</span>
          <h1>Fitness Tracker</h1>
          <p>Crie sua conta e comece a monitorar sua alimentacao hoje mesmo.</p>
        </div>
      </div>

      <div class="auth-right">
        <section class="auth-card">
          <h2>Criar conta</h2>
          <p class="subtitle">Preencha os dados abaixo para se cadastrar.</p>

          <form [formGroup]="form" (ngSubmit)="submit()">
            <label>
              <span class="label-text">Nome completo</span>
              <input type="text" formControlName="fullName" placeholder="Seu nome" />
            </label>

            <label>
              <span class="label-text">Email</span>
              <input type="email" formControlName="email" placeholder="voce@email.com" />
            </label>

            <label>
              <span class="label-text">Senha</span>
              <input type="password" formControlName="password" placeholder="Minimo 6 caracteres" />
            </label>

            <button type="submit" class="btn-primary" [disabled]="form.invalid || loading()">
              {{ loading() ? 'Cadastrando...' : 'Cadastrar' }}
            </button>
          </form>

          @if (error()) {
            <div class="alert alert-error">{{ error() }}</div>
          }

          <p class="auth-link">Ja tem conta? <a routerLink="/login">Entrar</a></p>
        </section>
      </div>
    </div>
  `,
  styles: `
    .auth-page {
      min-height: 100vh;
      display: flex;
    }

    .auth-left {
      flex: 1;
      background: linear-gradient(135deg, var(--color-sidebar) 0%, #0f172a 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 3rem;
    }

    .auth-brand {
      max-width: 360px;
    }

    .auth-brand .logo {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-md);
      background: var(--color-primary);
      display: inline-grid;
      place-items: center;
      font-weight: 700;
      font-size: 1.1rem;
      margin-bottom: 1.5rem;
    }

    .auth-brand h1 {
      font-size: 2rem;
      margin-bottom: 0.75rem;
    }

    .auth-brand p {
      color: #94a3b8;
      font-size: 1.05rem;
      line-height: 1.6;
    }

    .auth-right {
      width: 480px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 3rem 2.5rem;
      background: var(--color-bg);
    }

    .auth-card {
      width: 100%;
      max-width: 380px;
    }

    .auth-card h2 {
      font-size: 1.6rem;
      margin-bottom: 0.35rem;
    }

    .subtitle {
      color: var(--color-text-secondary);
      margin-bottom: 2rem;
    }

    form {
      display: grid;
      gap: 1.1rem;
    }

    label {
      display: grid;
      gap: 0.4rem;
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

    .alert-error {
      background: var(--color-error-bg);
      color: var(--color-error);
      border: 1px solid #fecaca;
    }

    .auth-link {
      margin-top: 1.5rem;
      text-align: center;
      color: var(--color-text-secondary);
      font-size: 0.9rem;
    }

    .auth-link a {
      font-weight: 600;
    }

    @media (max-width: 768px) {
      .auth-page {
        flex-direction: column;
      }

      .auth-left {
        padding: 2rem 1.5rem;
        min-height: auto;
      }

      .auth-brand h1 {
        font-size: 1.5rem;
      }

      .auth-right {
        width: 100%;
        padding: 2rem 1.5rem;
      }
    }
  `
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal('');

  protected readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.maxLength(255)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.authService.register(this.form.getRawValue() as { fullName: string; email: string; password: string }).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Falha no cadastro');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
