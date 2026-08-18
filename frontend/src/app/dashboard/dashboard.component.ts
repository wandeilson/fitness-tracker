import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="dashboard">
      <header>
        <h1>Fitness Tracker</h1>
        <button (click)="logout()">Sair</button>
      </header>

      <section class="card">
        <h2>Fase 2 iniciada</h2>
        <p>Agora você pode definir metas diárias e manter dados de perfil.</p>
        <div class="actions">
          <a routerLink="/goals">Editar metas diárias</a>
          <a routerLink="/profile">Editar perfil</a>
        </div>
      </section>
    </main>
  `,
  styles: `
    .dashboard { min-height: 100vh; background: #f7fafc; padding: 2rem; }
    header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
    h1 { margin: 0; color: #102a43; }
    button { background: #334e68; color: #fff; border: 0; border-radius: 10px; padding: .6rem .9rem; cursor: pointer; }
    .card { background: #fff; border-radius: 14px; padding: 1.2rem; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
    h2 { margin-top: 0; color: #102a43; }
    .actions { display: flex; gap: .8rem; margin-top: 1rem; }
    a { text-decoration: none; color: #0f766e; font-weight: 600; }
  `
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}
