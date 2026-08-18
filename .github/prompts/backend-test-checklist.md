# Checklist de Implementacao de Testes - Backend

Objetivo: aumentar robustez do backend com cobertura alta e foco em regras criticas (auth, perfil e metas).

## Metas de cobertura
- [x] Linha geral >= 80% (padrao oficial)
- [x] Branch geral >= 70% (padrao oficial)
- [ ] Pacotes criticos (`auth`, `goal`, `profile`) >= 85%

## Metas da proxima execucao de testes
- [x] Linha geral >= 80%
- [x] Branch geral >= 70%
- [x] Ajustar gate JaCoCo para Linha >= 80% e Branch >= 70%

## Fase 1 - Fundacao de testes
- [x] Configurar JaCoCo no `pom.xml` com relatorio em `verify`
- [x] Definir thresholds minimos de cobertura no build
- [ ] Padronizar estrutura de testes por modulo (`auth`, `goal`, `profile`)
- [ ] Criar builders/factories de teste para `User` e `Goal`
- [ ] Separar nomenclatura de testes unitarios e de integracao

Criterio de aceite:
- [x] `mvn -f backend/pom.xml test` executa sem falhas
- [x] `mvn -f backend/pom.xml verify` gera relatorio JaCoCo

## Fase 2 - Testes unitarios de regra de negocio

### GoalService
- [x] Deve aplicar distribuicao padrao 50/25/25 quando nao houver personalizacao
- [x] Deve aceitar distribuicao customizada quando soma = 100
- [x] Deve rejeitar distribuicao customizada quando soma != 100
- [x] Deve calcular kcal por macro com base na meta calorica
- [x] Deve calcular gramas com regra 4/4/9
- [ ] Deve arredondar valores conforme padrao definido

### ProfileService
- [x] Deve atualizar nome quando valor valido for informado
- [x] Deve atualizar sexo com enum valido
- [x] Deve atualizar nivel de atividade com enum valido
- [x] Deve manter comportamento esperado com campos nulos opcionais

### Auth/JWT (servicos)
- [x] Deve gerar token valido para usuario autenticado
- [x] Deve rejeitar credenciais invalidas
- [x] Deve tratar token expirado sem erro 500
- [x] Deve tratar token malformado sem erro 500

### AuthServiceTest (novo)
- [x] Registro com email novo deve retornar sucesso
- [x] Registro com email existente deve retornar erro
- [x] Login com credenciais validas deve retornar token
- [x] Login com credenciais invalidas deve retornar erro esperado

### JwtServiceTest (novo)
- [x] Deve gerar token com subject correto
- [x] Deve extrair username do token valido
- [x] Deve identificar token expirado
- [x] Deve tratar token malformado/invalido

Criterio de aceite:
- [x] Suite unitaria executa em tempo rapido
- [x] Regras criticas de metas cobertas por testes de sucesso e erro

## Fase 3 - Testes de API (Controller)

### ProfileController
- [x] `GET /api/profile` retorna 200 para usuario autenticado
- [x] `PUT /api/profile` atualiza dados validos e retorna 200
- [ ] Payload com enum invalido retorna 400 com mensagem amigavel

### GoalController
- [x] `GET /api/goals` retorna meta atual
- [x] `PUT /api/goals` com valores validos retorna 200
- [x] `PUT /api/goals` com soma de percentuais invalida retorna 400

### AuthController
- [x] `POST /api/auth/register` com dados validos retorna sucesso
- [x] `POST /api/auth/login` com credenciais validas retorna token
- [x] `POST /api/auth/login` com credenciais invalidas retorna 401

### AuthControllerTest (novo)
- [x] `POST /api/auth/register` valido retorna status de sucesso
- [x] `POST /api/auth/register` invalido retorna 400
- [x] `POST /api/auth/login` valido retorna token no body
- [x] `POST /api/auth/login` invalido retorna 401

### JwtAuthenticationFilterTest (novo)
- [x] Requisicao sem Authorization deve seguir sem autenticar
- [x] Token invalido nao deve autenticar usuario
- [x] Token valido deve autenticar e seguir cadeia

### Complementos de controller (validacao)
- [x] ProfileController: payload com enum invalido retorna 400 com mensagem amigavel
- [x] GoalController: payload invalido por Bean Validation retorna 400

Criterio de aceite:
- [x] Status HTTP e mensagens de erro alinhados ao contrato atual da API

## Fase 4 - Integracao com banco (PostgreSQL)
- [ ] Subir testes com Testcontainers PostgreSQL
- [ ] Validar aplicacao de migrations Flyway no ambiente de teste
- [ ] Cobrir repositorios criticos com cenarios reais de persistencia
- [ ] Cobrir fluxo autenticado basico (`auth` -> `profile` -> `goals`)

Criterio de aceite:
- [ ] Integracao roda de forma reproduzivel local e no CI

## Fase 5 - Regressao e manutencao continua
- [ ] Todo bug corrigido deve incluir teste de regressao
- [ ] Toda regra nova deve incluir teste de sucesso e falha
- [ ] PR sem testes para mudanca de regra deve ser bloqueado
- [ ] Revisar thresholds de cobertura a cada fase entregue

## Comandos de validacao
- [x] `mvn -f backend/pom.xml test`
- [x] `mvn -f backend/pom.xml verify`
- [ ] `mvn -f backend/pom.xml -Dtest=*Goal* test`
- [ ] `mvn -f backend/pom.xml -Dtest=*Profile* test`

## Ordem recomendada de implementacao (curto prazo)
- [x] 1) Cobrir `GoalService`
- [x] 2) Cobrir `ProfileService`
- [ ] 3) Cobrir `Auth`/JWT
- [x] 4) Cobrir controllers de `profile` e `goals`
- [ ] 5) Adicionar Testcontainers + testes de integracao

## Proxima onda de implementacao (planejada)
- [x] 1) Criar `AuthServiceTest`
- [x] 2) Criar `JwtServiceTest`
- [x] 3) Criar `AuthControllerTest`
- [x] 4) Criar `JwtAuthenticationFilterTest`
- [x] 5) Completar validacoes pendentes de `ProfileController` e `GoalController`
