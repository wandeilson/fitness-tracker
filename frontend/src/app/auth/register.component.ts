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
    <main class="auth-page">
      <section class="auth-card">
        <h1>Criar conta</h1>
        <p>Cadastre-se para começar a registrar refeições.</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Nome completo
            <input type="text" formControlName="fullName" placeholder="Seu nome" />
          </label>

          <label>
            Email
            <input type="email" formControlName="email" placeholder="voce@email.com" />
          </label>

          <label>
            Senha
            <input type="password" formControlName="password" placeholder="******" />
          </label>

          <button type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Cadastrando...' : 'Cadastrar' }}
          </button>
        </form>

        <p class="error" *ngIf="error()">{{ error() }}</p>
        <p class="helper">Já tem conta? <a routerLink="/login">Entrar</a></p>
      </section>
    </main>
  `,
  styles: `
    .auth-page { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(145deg, #f0f4f8, #d9e2ec); padding: 1rem; }
    .auth-card { width: min(420px, 100%); background: #fff; padding: 1.5rem; border-radius: 14px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); }
    h1 { margin: 0 0 .4rem; }
    p { margin: 0 0 1rem; color: #486581; }
    form { display: grid; gap: .9rem; }
    label { display: grid; gap: .4rem; font-weight: 600; color: #334e68; }
    input { border: 1px solid #bcccdc; border-radius: 10px; padding: .7rem .75rem; }
    button { background: #0f766e; color: #fff; border: 0; border-radius: 10px; padding: .75rem; font-weight: 600; cursor: pointer; }
    button[disabled] { opacity: .6; cursor: not-allowed; }
    .error { color: #b42318; margin-top: .75rem; }
    .helper { margin-top: .75rem; }
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
