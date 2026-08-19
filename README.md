# Fitness Tracker

Aplicação fullstack para acompanhamento de calorias e macronutrientes, inspirada no MyFitnessPal.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 / Spring Boot 4.1 / Maven |
| Banco de dados | PostgreSQL 16 / Flyway |
| Autenticação | Spring Security + JWT |
| Frontend | Angular 20.3 (standalone, zoneless, SSR) |
| UI | Angular Material (dialogs, ícones) |
| Estilo | SCSS + estilos inline |

## Funcionalidades

### Autenticação
- Cadastro de usuário (nome completo, e-mail, senha com hash BCrypt)
- Login com retorno de JWT (armazenado no `localStorage`)
- Interceptador funcional que anexa o token Bearer automaticamente
- Guard funcional que protege rotas autenticadas
- Logout com limpeza do token

### Perfil
- Campos: nome completo, idade, peso (kg), altura (cm), sexo (MASCULINO/FEMININO), nível de atividade (5 opções)
- Consulta e atualização via `GET/PUT /api/profile`

### Metas
- Definição de caloria diária alvo (kcal)
- Distribuição percentual de macronutrientes (carboidratos, proteínas, gorduras)
- Defaults automáticos: 50/25/25 — com opção de personalização
- Validação: soma dos percentuais deve ser exatamente 100% quando customizado
- Recálculo em tempo real de gramas e kcal por macro
- Upsert com rastreamento por período (`valid_from` / `valid_until`) — meta vigente muda por data

### Diário de Refeições
- Navegação por data com botões anterior/próximo e botão "Hoje"
- 6 seções fixas por tipo de refeição:
  - Café da manhã
  - Lanche da manhã
  - Almoço
  - Lanche da tarde
  - Jantar
  - Ceia
- Criação automática da refeição ao adicionar o primeiro item

### Adição de Alimentos
- Busca por nome com debounce (300ms, mínimo 2 caracteres) via diálogo Material
- Seleção do alimento结果显示 nome, fonte (TACO) e macronutrientes
- Diálogo de quantidade em gramas (padrão 100g, intervalo 1–10000g)
- Pré-visualização ao vivo de calorias/macros para a quantidade informada
- Cálculo automático dos valores nutricionais consumidos

### Resumo Diário
- Barras de progresso comparando consumo vs meta para:
  - Calorias (kcal)
  - Carboidratos (g)
  - Proteínas (g)
  - Gorduras (g)
- A meta exibida é a vigente para a data selecionada

### Banco de Dados de Alimentos
- Base de dados TACO (Tabela Brasileira de Composição de Alimentos) importada na inicialização
- Leitura de planilha Excel (`alimentos.xlsx`) via Apache POI
- Lógica idempotente: insere novos, atualiza existentes, pula já cadastrados
- Configurável via propriedades: `app.food.seed.enabled`, `app.food.seed.resource`, etc.

### Dashboard
- Página inicial com links para Metas e Perfil
- Status "Em breve" para funcionalidades futuras

## Estrutura do Projeto

```
├── backend/
│   └── src/main/java/com/fitness/backend/
│       ├── auth/          # JWT, filtros, registro/login
│       ├── food/          # Alimentos, seed TACO
│       ├── goal/          # Metas com período de validade
│       ├── meal/          # Refeições e itens
│       └── profile/       # Perfil do usuário
├── frontend/
│   └── src/app/
│       ├── auth/          # Login, registro, guard, interceptor
│       ├── foods/         # Serviço de busca de alimentos
│       ├── goals/         # Página e serviço de metas
│       ├── meals/         # Diário, seções, resumo, diálogos
│       ├── models/        # Interfaces TypeScript
│       └── profile/       # Página e serviço de perfil
├── docker-compose.yml     # PostgreSQL 16 (porta 5433)
└── AGENTS.md              # Documentação para agentes de código
```

## Endpoints da API

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Cadastro de usuário |
| POST | `/api/auth/login` | Login |
| GET | `/api/auth/me` | Dados do usuário autenticado |
| GET | `/api/profile` | Consultar perfil |
| PUT | `/api/profile` | Atualizar perfil |
| GET | `/api/goals` | Consultar meta atual |
| PUT | `/api/goals` | Criar/atualizar meta |
| GET | `/api/foods?q=...` | Buscar alimentos |
| GET | `/api/foods/{id}` | Detalhes de um alimento |
| POST | `/api/meals` | Criar refeição |
| GET | `/api/meals?date=YYYY-MM-DD` | Listar refeições do dia |
| GET | `/api/meals/summary?date=YYYY-MM-DD` | Resumo diário (consumo + meta) |
| POST | `/api/meals/{mealId}/items` | Adicionar item à refeição |
| PUT | `/api/meals/{mealId}/items/{itemId}` | Atualizar quantidade do item |
| DELETE | `/api/meals/{mealId}/items/{itemId}` | Remover item da refeição |
| DELETE | `/api/meals/{mealId}` | Excluir refeição inteira |

## Como Rodar

### Pré-requisitos
- Java 21
- Node.js 20+
- Docker (para o PostgreSQL)

### Banco de Dados
```bash
docker compose up -d
```
PostgreSQL roda na porta 5433 com usuário/senha `fitness/fitness` e banco `fitnessdb`. As migrações Flyway executam automaticamente na inicialização do backend.

### Backend (porta 8080)
```bash
mvn -f backend/pom.xml spring-boot:run
```

### Frontend (porta 4200)
```bash
cd frontend
npm install
npm start
```

### Testes
```bash
# Backend — 65 testes unitários
mvn -f backend/pom.xml test

# Frontend — Karma/Jasmine
cd frontend && npm test
```

## Fluxo de Autenticação

```
Registro/Login → Backend retorna JWT → Frontend armazena no localStorage ('fitness_token')
    → Interceptor anexa 'Authorization: Bearer <token>' em todas as requisições
        → Guard verifica token antes de carregar rotas protegidas
            → 401 → logout automático → redireciona para /login
```

## Arquitetura

- **Backend**: Camadas Controller → Service → Repository. DTOs são records Java.
- **Frontend**: Componentes standalone, signals para estado, formulários reativos, sem NgModules.
- **SSR**: Modo prerender ativo em todas as rotas; o guard verifica `isPlatformBrowser` antes de validar o token.
- **Schema do banco**: Gerenciado 100% por Flyway (DDL nunca gerado pelo JPA — `ddl-auto=OFF`).
