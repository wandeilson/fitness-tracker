import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout">
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="logo">FT</span>
          <span class="brand">Fitness Tracker</span>
        </div>

        <nav class="sidebar-nav">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
            <span class="nav-icon">&#9632;</span>
            Dashboard
          </a>
          <a routerLink="/goals" routerLinkActive="active">
            <span class="nav-icon">&#9654;</span>
            Metas
          </a>
          <a routerLink="/profile" routerLinkActive="active">
            <span class="nav-icon">&#9679;</span>
            Perfil
          </a>
        </nav>

        <div class="sidebar-footer">
          <button class="logout-btn" (click)="logout()">Sair</button>
        </div>
      </aside>

      <div class="mobile-header">
        <button class="menu-toggle" (click)="mobileOpen.set(!mobileOpen())">&#9776;</button>
        <span class="mobile-brand">Fitness Tracker</span>
        <button class="mobile-logout" (click)="logout()">Sair</button>
      </div>

      @if (mobileOpen()) {
        <div class="mobile-overlay" (click)="mobileOpen.set(false)"></div>
        <nav class="mobile-nav">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }" (click)="mobileOpen.set(false)">Dashboard</a>
          <a routerLink="/goals" routerLinkActive="active" (click)="mobileOpen.set(false)">Metas</a>
          <a routerLink="/profile" routerLinkActive="active" (click)="mobileOpen.set(false)">Perfil</a>
        </nav>
      }

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: `
    .layout {
      display: flex;
      min-height: 100vh;
    }

    .sidebar {
      width: var(--sidebar-width);
      background: var(--color-sidebar);
      color: var(--color-text-inverse);
      display: flex;
      flex-direction: column;
      position: fixed;
      top: 0;
      left: 0;
      bottom: 0;
      z-index: 100;
    }

    .sidebar-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1.5rem 1.25rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }

    .logo {
      width: 36px;
      height: 36px;
      border-radius: var(--radius-sm);
      background: var(--color-primary);
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 0.85rem;
      letter-spacing: -0.5px;
      flex-shrink: 0;
    }

    .brand {
      font-weight: 700;
      font-size: 1rem;
      white-space: nowrap;
    }

    .sidebar-nav {
      flex: 1;
      padding: 1rem 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .sidebar-nav a {
      display: flex;
      align-items: center;
      gap: 0.7rem;
      padding: 0.65rem 0.85rem;
      border-radius: var(--radius-md);
      color: #cbd5e1;
      font-weight: 500;
      font-size: 0.93rem;
      text-decoration: none;
      transition: background 0.15s, color 0.15s;
    }

    .sidebar-nav a:hover {
      background: var(--color-sidebar-hover);
      color: #fff;
      text-decoration: none;
    }

    .sidebar-nav a.active {
      background: var(--color-primary);
      color: #fff;
    }

    .nav-icon {
      font-size: 0.65rem;
      width: 1.25rem;
      text-align: center;
      flex-shrink: 0;
    }

    .sidebar-footer {
      padding: 1rem 1.25rem;
      border-top: 1px solid rgba(255, 255, 255, 0.08);
    }

    .logout-btn {
      width: 100%;
      padding: 0.6rem;
      background: transparent;
      border: 1.5px solid rgba(255, 255, 255, 0.15);
      border-radius: var(--radius-md);
      color: #cbd5e1;
      font-weight: 600;
      font-size: 0.88rem;
      transition: background 0.15s, color 0.15s, border-color 0.15s;
    }

    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.25);
      color: #fff;
    }

    .content {
      flex: 1;
      margin-left: var(--sidebar-width);
      padding: 2rem 2.5rem;
      max-width: 960px;
    }

    .mobile-header {
      display: none;
    }

    .mobile-overlay {
      display: none;
    }

    .mobile-nav {
      display: none;
    }

    @media (max-width: 768px) {
      .sidebar {
        display: none;
      }

      .mobile-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0.85rem 1rem;
        background: var(--color-sidebar);
        color: #fff;
        position: sticky;
        top: 0;
        z-index: 90;
      }

      .mobile-brand {
        font-weight: 700;
        font-size: 0.95rem;
      }

      .menu-toggle,
      .mobile-logout {
        background: transparent;
        border: none;
        color: #cbd5e1;
        font-size: 1.1rem;
        padding: 0.35rem;
      }

      .mobile-overlay {
        display: block;
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.4);
        z-index: 110;
      }

      .mobile-nav {
        display: flex;
        flex-direction: column;
        position: fixed;
        top: 0;
        left: 0;
        bottom: 0;
        width: 260px;
        background: var(--color-sidebar);
        z-index: 120;
        padding: 4.5rem 1rem 1.5rem;
        gap: 0.25rem;
      }

      .mobile-nav a {
        display: block;
        padding: 0.75rem 1rem;
        color: #cbd5e1;
        font-weight: 500;
        border-radius: var(--radius-md);
        text-decoration: none;
      }

      .mobile-nav a:hover {
        background: var(--color-sidebar-hover);
        color: #fff;
        text-decoration: none;
      }

      .mobile-nav a.active {
        background: var(--color-primary);
        color: #fff;
      }

      .content {
        margin-left: 0;
        padding: 1.25rem 1rem;
      }
    }
  `
})
export class LayoutComponent {
  private readonly authService = inject(AuthService);
  protected readonly mobileOpen = signal(false);

  logout(): void {
    this.authService.logout();
  }
}
