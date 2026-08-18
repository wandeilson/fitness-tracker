# Plano: App estilo MyFitnessPal (Java Spring Boot + Angular)

## Decisões confirmadas
- Plataforma: Web app (SPA) com backend próprio
- Stack: Java 21 + Spring Boot (backend) / Angular (frontend)
- Auth: multi-usuário, JWT + Spring Security
- Banco: PostgreSQL
- Base de alimentos: cadastro manual pelo usuário no MVP; integração com API externa (Open Food Facts) fica para Fase 2
- Histórico diário simples incluído no MVP
- Estrutura: duas pastas/repos separados (`backend/`, `frontend/`)
- Meta calórica e de macros: meta calórica informada pelo usuário com distribuição padrão automática (50% carboidratos, 25% proteínas, 25% gorduras), com opção de personalização de percentuais
- Unidade de porção dos alimentos: gramas (quantidade em gramas), sem porções nomeadas no MVP

## Status de execução
- Fase 0 — Setup: concluída
- Fase 1 — Autenticação: concluída
- Fase 2 — Perfil e Metas: concluída
- Fase 3 — Alimentos e Refeições: pendente
- Fase 4 — Dashboard e Histórico: pendente

## Escopo funcional (MVP)
1. Autenticação: registro, login, JWT, rotas protegidas
2. Perfil do usuário: dados básicos (idade, peso, altura, sexo, nível de atividade) — apenas cadastrais/informativos, sem uso em cálculo de meta
3. Metas diárias: calorias totais + distribuição de macros por percentuais, com cálculo automático de calorias e gramas via regra 4/4/9, editável a qualquer momento
4. Cadastro de alimentos (base própria do usuário): nome, quantidade de referência em gramas, calorias, proteína, carbo, gordura
5. Registro de refeições: usuário lança alimentos consumidos, agrupados por tipo (café da manhã, almoço, jantar, lanche) e data
6. Dashboard diário: total consumido vs meta (calorias e macros), barra/gráfico de progresso
7. Histórico: navegação por dias anteriores, ver o que foi registrado e se bateu a meta

## Fora do escopo do MVP (Fase 2+)
- Integração com API externa de alimentos (Open Food Facts) e busca automática
- Relatórios/gráficos semanais e mensais avançados
- Acompanhamento de peso corporal e progresso ao longo do tempo
- Recursos sociais, notificações, app mobile

## Arquitetura

### Backend (Spring Boot)
- Camadas: Controller → Service → Repository (Spring Data JPA)
- Módulos/entidades principais:
  - `User` (id, email, senha hash, dados de perfil)
  - `Goal` (userId, calorias, proteína_g, carbo_g, gordura_g, vigente a partir de uma data)
  - `Food` (id, userId dono/criador, nome, porção referência, calorias, proteína, carbo, gordura)
  - `Meal` (id, userId, data, tipo [BREAKFAST/LUNCH/DINNER/SNACK])
  - `MealItem` (mealId, foodId, quantidade/porções)
- Segurança: Spring Security + JWT filter, endpoints `/api/auth/**` públicos, demais autenticados
- Validação: Bean Validation (`@NotNull`, `@Positive`, etc.) nos DTOs de entrada
- Migrations: Flyway para versionar schema PostgreSQL
- Testes: JUnit + Mockito para services; testes de integração com Testcontainers (PostgreSQL) para repositories/controllers

### Frontend (Angular)
- Módulos: `auth`, `profile`, `goals`, `foods`, `meals`, `dashboard`, `history`
- Guards de rota (`authGuard`) + `HttpInterceptor` para anexar JWT e tratar 401
- Services por domínio consumindo a API REST
- Componentes principais:
  - Tela de login/registro
  - Formulário de definição de metas (calorias e macros)
  - CRUD de alimentos (lista + formulário)
  - Tela de registro de refeição do dia (selecionar alimento, porção, tipo de refeição)
  - Dashboard com resumo do dia (consumido vs meta, progresso por macro)
  - Tela de histórico (seletor de data, lista de refeições daquele dia)
- Gerenciamento de estado simples via services + RxJS (BehaviorSubject), sem necessidade de NgRx no MVP

## Passos de implementação

### Fase 0 — Setup (paralelo entre si)
1. [Concluído] Criar `backend/` com Spring Boot (Web, Security, Data JPA, Validation, PostgreSQL driver, Flyway) via Spring Initializr
2. [Concluído] Criar `frontend/` com Angular CLI (routing + standalone components)
3. [Concluído] Configurar PostgreSQL local (docker-compose com serviço `db`) compartilhado pelas duas pastas
4. [Concluído] Ajustar porta do PostgreSQL em `docker-compose.yml` para `5433` no host e alinhar `spring.datasource.url`

### Fase 1 — Autenticação (backend depende da Fase 0.1; frontend depende de 1.backend)
4. [Concluído] Backend: entidade `User`, endpoints `/api/auth/register` e `/api/auth/login`, geração/validação de JWT, `SecurityFilterChain`
5. [Concluído] Frontend: telas de login/registro, `AuthService`, `authGuard`, `authInterceptor`

### Fase 2 — Perfil e Metas (*depende de Fase 1*)
6. [Concluído] Backend: entidade `Goal`, endpoint para definir/atualizar meta diária com percentuais, validação de soma exata de 100% e cálculo automático de calorias/gramas
7. [Concluído] Frontend: tela de perfil + formulário de metas com padrão 50/25/25 e opção "Personalizar distribuição"

### Fase 3 — Alimentos e Refeições (*depende de Fase 1, paralelo com Fase 2*)
8. Backend: CRUD `Food` (escopo por usuário), entidades `Meal`/`MealItem`, endpoint para registrar refeição em uma data
9. Frontend: CRUD de alimentos, tela de lançamento de refeição do dia

### Fase 4 — Dashboard e Histórico (*depende de Fases 2 e 3*)
10. Backend: endpoint agregador `/api/dashboard?date=` retornando totais consumidos vs meta do dia
11. Frontend: componente de dashboard (barras de progresso calorias/macros), tela de histórico com date-picker reutilizando o mesmo endpoint por data

## Arquivos/pastas relevantes
- `backend/src/main/java/.../auth/` — controllers/services de autenticação e JWT
- `backend/src/main/java/.../domain/` — entidades `User`, `Goal`, `Food`, `Meal`, `MealItem`
- `backend/src/main/resources/db/migration/` — scripts Flyway
- `frontend/src/app/auth/` — login, registro, guard, interceptor
- `frontend/src/app/dashboard/` — componente de resumo diário
- `frontend/src/app/meals/`, `frontend/src/app/foods/`, `frontend/src/app/goals/`
- `docker-compose.yml` (raiz do workspace) — serviço PostgreSQL para dev local

## Verificação
1. [Concluído Fase 1] Backend: `mvn test` executado com sucesso após implementação de autenticação
2. [Concluído Fase 1] Frontend: `ng build` executado com sucesso após implementação de autenticação
3. [Concluído Fase 2] Backend: `mvn test` executado com sucesso após implementação de perfil/metas
4. [Concluído Fase 2] Frontend: `ng build` executado com sucesso após implementação de perfil/metas
5. [Concluído Fase 2] Smoke test API autenticada: `GET/PUT /api/profile` e `GET/PUT /api/goals` com status 200
6. Backend: `mvn test` (unit + Testcontainers) cobrindo services de auth, goals, meals e cálculo de totais do dashboard
7. Frontend: `ng test` para componentes/guards/interceptor principais
8. Teste manual end-to-end: registrar usuário → login → definir meta → cadastrar 2-3 alimentos → lançar refeições no dia → conferir dashboard bate com soma esperada → navegar histórico para dia anterior
9. Testar cenário de token expirado/inválido redirecionando para login (interceptor)
