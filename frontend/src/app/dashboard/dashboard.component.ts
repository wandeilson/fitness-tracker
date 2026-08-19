import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="dashboard">
      <div class="welcome">
        <h1>Bem-vindo de volta</h1>
        <p>Gerencie sua alimentacao e acompanhe seus objetivos.</p>
      </div>

      <div class="cards">
        <a routerLink="/goals" class="card">
          <div class="card-icon card-icon-goals">&#9654;</div>
          <div class="card-body">
            <h3>Metas diarias</h3>
            <p>Defina suas calorias e distribuicao de macros.</p>
          </div>
        </a>

        <a routerLink="/profile" class="card">
          <div class="card-icon card-icon-profile">&#9679;</div>
          <div class="card-body">
            <h3>Meu perfil</h3>
            <p>Atualize seus dados pessoais e nivel de atividade.</p>
          </div>
        </a>
      </div>

      <div class="info-banner">
        <strong>Em breve:</strong> registro de refeicoes, historico e dashboard com graficos.
      </div>
    </div>
  `,
  styles: `
    .dashboard {
      max-width: 640px;
    }

    .welcome {
      margin-bottom: 2rem;
    }

    .welcome h1 {
      margin-bottom: 0.3rem;
    }

    .welcome p {
      color: var(--color-text-secondary);
      font-size: 1.02rem;
    }

    .cards {
      display: grid;
      gap: 1rem;
    }

    .card {
      display: flex;
      align-items: center;
      gap: 1.1rem;
      padding: 1.25rem 1.5rem;
      background: var(--color-surface);
      border: 1.5px solid var(--color-border);
      border-radius: var(--radius-lg);
      text-decoration: none;
      color: inherit;
      transition: border-color 0.15s, box-shadow 0.15s, transform 0.1s;
    }

    .card:hover {
      border-color: var(--color-primary);
      box-shadow: var(--shadow-md);
      transform: translateY(-1px);
      text-decoration: none;
    }

    .card-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-md);
      display: grid;
      place-items: center;
      font-size: 1.1rem;
      flex-shrink: 0;
    }

    .card-icon-goals {
      background: var(--color-primary-light);
      color: var(--color-primary);
    }

    .card-icon-profile {
      background: #ede9fe;
      color: #7c3aed;
    }

    .card-body h3 {
      margin-bottom: 0.2rem;
      font-size: 1.02rem;
    }

    .card-body p {
      color: var(--color-text-secondary);
      font-size: 0.9rem;
    }

    .info-banner {
      margin-top: 2rem;
      padding: 1rem 1.25rem;
      background: var(--color-primary-bg);
      border: 1.5px solid #99f6e4;
      border-radius: var(--radius-md);
      color: var(--color-text-secondary);
      font-size: 0.9rem;
    }

    .info-banner strong {
      color: var(--color-primary);
    }
  `
})
export class DashboardComponent {}
