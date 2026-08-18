# Checklist de Implementacao de Testes - Backend

Objetivo: aumentar robustez do backend com cobertura alta e foco em regras criticas (auth, perfil e metas).

## Metas de cobertura
- [ ] Linha geral >= 50% (marco inicial)
- [ ] Linha geral >= 70% (marco intermediario)
- [ ] Linha geral >= 80% (meta final)
- [ ] Branch geral >= 60%
- [ ] Pacotes criticos (`auth`, `goal`, `profile`) >= 85%

## Fase 1 - Fundacao de testes
- [ ] Configurar JaCoCo no `pom.xml` com relatorio em `verify`
- [ ] Definir thresholds minimos de cobertura no build
- [ ] Padronizar estrutura de testes por modulo (`auth`, `goal`, `profile`)
- [ ] Criar builders/factories de teste para `User` e `Goal`
- [ ] Separar nomenclatura de testes unitarios e de integracao

Criterio de aceite:
- [ ] `mvn -f backend/pom.xml test` executa sem falhas
- [ ] `mvn -f backend/pom.xml verify` gera relatorio JaCoCo

## Fase 2 - Testes unitarios de regra de negocio

### GoalService
- [ ] Deve aplicar distribuicao padrao 50/25/25 quando nao houver personalizacao
- [ ] Deve aceitar distribuicao customizada quando soma = 100
- [ ] Deve rejeitar distribuicao customizada quando soma != 100
- [ ] Deve calcular kcal por macro com base na meta calorica
- [ ] Deve calcular gramas com regra 4/4/9
- [ ] Deve arredondar valores conforme padrao definido

### ProfileService
- [ ] Deve atualizar nome quando valor valido for informado
- [ ] Deve atualizar sexo com enum valido
- [ ] Deve atualizar nivel de atividade com enum valido
- [ ] Deve manter comportamento esperado com campos nulos opcionais

### Auth/JWT (servicos)
- [ ] Deve gerar token valido para usuario autenticado
- [ ] Deve rejeitar credenciais invalidas
- [ ] Deve tratar token expirado sem erro 500
- [ ] Deve tratar token malformado sem erro 500

Criterio de aceite:
- [ ] Suite unitaria executa em tempo rapido
- [ ] Regras criticas de metas cobertas por testes de sucesso e erro

## Fase 3 - Testes de API (Controller)

### ProfileController
- [ ] `GET /api/profile` retorna 200 para usuario autenticado
- [ ] `PUT /api/profile` atualiza dados validos e retorna 200
- [ ] Payload com enum invalido retorna 400 com mensagem amigavel

### GoalController
- [ ] `GET /api/goals` retorna meta atual
- [ ] `PUT /api/goals` com valores validos retorna 200
- [ ] `PUT /api/goals` com soma de percentuais invalida retorna 400

### AuthController
- [ ] `POST /api/auth/register` com dados validos retorna sucesso
- [ ] `POST /api/auth/login` com credenciais validas retorna token
- [ ] `POST /api/auth/login` com credenciais invalidas retorna 401

Criterio de aceite:
- [ ] Status HTTP e mensagens de erro alinhados ao contrato atual da API

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
- [ ] `mvn -f backend/pom.xml test`
- [ ] `mvn -f backend/pom.xml verify`
- [ ] `mvn -f backend/pom.xml -Dtest=*Goal* test`
- [ ] `mvn -f backend/pom.xml -Dtest=*Profile* test`

## Ordem recomendada de implementacao (curto prazo)
- [ ] 1) Cobrir `GoalService`
- [ ] 2) Cobrir `ProfileService`
- [ ] 3) Cobrir `Auth`/JWT
- [ ] 4) Cobrir controllers de `profile` e `goals`
- [ ] 5) Adicionar Testcontainers + testes de integracao
