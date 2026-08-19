# AGENTS.md

## Project overview
- Fitness tracker (calorie/macro counting) — MyFitnessPal-style MVP
- Backend: Java 21 / Spring Boot 4.1 / Maven / PostgreSQL 16 / Flyway / JWT
- Frontend: Angular 20.3 (standalone, zoneless, SSR) / TypeScript 5.9 / SCSS
- No root-level monorepo tool — `backend/` and `frontend/` are independent

## Prerequisites
- Start the database: `docker compose up -d` (PostgreSQL on port 5433)
- Backend connects to `postgres://localhost:5433/fitnessdb` (user/pass: `fitness/fitness`)
- Flyway runs automatically on backend startup (DDL managed by migrations, not JPA)

## Key commands

### Backend (from repo root or `backend/`)
```bash
mvn -f backend/pom.xml test                    # unit tests
mvn -f backend/pom.xml verify                   # tests + JaCoCo coverage gate (line ≥80%, branch ≥70%)
mvn -f backend/pom.xml -Dtest=*ClassName* test  # single test class
```
- Backend runs on port **8080**
- CORS allows `http://localhost:4200`

### Frontend (from `frontend/`)
```bash
npm start / ng serve          # dev server on port 4200
npm test / ng test            # Karma unit tests
npm run build / ng build      # production build
npm run serve:ssr:frontend    # serve SSR build on port 4000
```
- All API URLs hardcoded to `http://localhost:8080` (no env-based config)

## Architecture notes
- Auth: JWT stored in `localStorage` as `fitness_token`, Bearer header attached by functional interceptor
- Backend layered: Controller → Service → Repository. DTOs are Java records.
- Food data seeded from TACO Excel spreadsheet on startup (`app.food.seed.enabled=true`)
- Frontend uses standalone components, functional guards/interceptors, signals — no NgModules
- Inline templates and styles (no separate `.html`/`.scss` files for most components)
- No ESLint on frontend; Prettier configured (100 width, single quotes, Angular HTML parser)
- No ESLint or formatter configured on backend

## Database
- Schema managed by Flyway in `backend/src/main/resources/db/migration/` (V1–V4)
- `ddl-auto` is OFF — never rely on JPA schema generation
- If adding migrations: create `V5__description.sql` (numbered sequentially)

## Testing conventions
- Backend: JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks` pattern
- No integration tests with real DB yet (Testcontainers planned but not implemented)
- Frontend: Jasmine + Karma, basic creation tests exist
- JaCoCo enforces minimum coverage — `verify` must pass before committing test changes

## Gotchas
- No CI/CD pipelines exist — no automated checks run on push
- JWT secret is hardcoded in `application.properties` (not externalized)
- Frontend has no meal/food UI yet — Phase 3 frontend work is incomplete
- Dashboard is a placeholder — Phase 4 pending
- Angular production budgets: initial bundle warning at 500kB, error at 1MB
- SSR prerender mode on all routes — auth guard checks `isPlatformBrowser` before token check
