# PulseAPI

## About

PulseAPI is a backend service for monitoring HTTP endpoints uptime and availability.

The system performs scheduled checks, records latency and failures, opens incidents,
generates alerts and exposes availability metrics through a REST API.

This project was built to simulate a production-ready backend service and demonstrate
backend engineering skills such as system design, background processing, persistence,
testing and CI.
[![CI](https://github.com/epaim/fluentiaapi/actions/workflows/ci.yml/badge.svg)](https://github.com/epaim/fluentiaapi/actions/workflows/ci.yml)

Backend do PulseAPI para monitoramento de uptime. Inclui autenticação JWT, CRUD de monitores, checks, incidentes, alertas, métricas de disponibilidade e migrações com Flyway.

## Arquitetura
- Camadas separadas: controller, service e repository.
- Persistência com JPA e migrações versionadas via Flyway.
- Autenticação JWT e autorização por perfil.
- Scheduler para execução de checks com isolamento do fluxo de API.
- Métricas de disponibilidade e alertas armazenados no banco.

## Tech Stack
- Java 17 + Spring Boot 3.x
- Spring Web, Spring Security, Spring Data JPA, Validation
- PostgreSQL + Flyway
- Swagger/OpenAPI (springdoc)
- Testcontainers + JUnit 5 + Mockito

## Requisitos
- Java 17+
- Docker (para Postgres local e Testcontainers)
- Maven (ou `./mvnw`)

## Configuração local
1. Subir o Postgres (e pgAdmin opcional):
```bash
docker compose up -d postgres
# Opcional:
docker compose --profile tools up -d pgadmin
```

2. Rodar a aplicação:
```bash
./mvnw spring-boot:run
```

A API estará em `http://localhost:8080`.
Swagger: `http://localhost:8080/swagger-ui/index.html`

## Variáveis de ambiente
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `pulseapi`)
- `DB_USER` (default: `pulseapi`)
- `DB_PASSWORD` (default: `pulseapi`)
- `JWT_SECRET` (min. 32 chars, obrigatório)
- `JWT_EXP_MINUTES` (default: `120`)
- `APP_PORT` (default: `8080`)
- `DB_URL` (prod)
- `PORT` (prod, default: `8080`)
- `CORS_ALLOWED_ORIGINS` (prod, lista separada por vírgula, obrigatório para deploy)
- `APP_SECURITY_ALLOW_PRIVATE_TARGETS` (prod, default: `false`; mantém bloqueio SSRF para destinos privados)

## Endpoints principais
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `GET /api/v1/monitors`
- `POST /api/v1/monitors`
- `GET /api/v1/monitors/{id}`
- `PUT /api/v1/monitors/{id}`
- `PATCH /api/v1/monitors/{id}/enable`
- `DELETE /api/v1/monitors/{id}`
- `POST /api/v1/monitors/{id}/recheck`
- `GET /api/v1/monitors/{id}/checks/summary?window=24h|7d|30d`
- `GET /api/v1/dashboard/overview?window=7d`

## Postman
- Collection: `postman/PulseAPI.postman_collection.json`
- Environment local: `postman/PulseAPI.local.postman_environment.json`

### Como usar em 2 minutos
1. Importe os dois arquivos no Postman.
2. Selecione o environment `PulseAPI Local`.
3. Ajuste `baseUrl` para local ou Railway.
4. Execute em ordem:
   - `Auth > Register` (ou `Auth > Login` se o email já existir)
   - `Auth > Login`
   - `Monitors > Create Monitor`
   - requests restantes conforme necessário

Notas:
- A collection preenche `accessToken`, `monitorId` e `alertId` automaticamente quando possível.
- Não há segredos hardcoded; ajuste apenas variáveis do environment.

## Quick test (1 minute demo)

### 1. Register user
```bash
curl -X POST https://pulseapi-production-c537.up.railway.app/api/v1/auth/register \
 -H "Content-Type: application/json" \
 -d '{"email":"test@test.com","password":"123456"}'
```

### 2. Login
```bash
curl -X POST https://pulseapi-production-c537.up.railway.app/api/v1/auth/login \
 -H "Content-Type: application/json" \
 -d '{"email":"test@test.com","password":"123456"}'
```

Copy the token returned.

### 3. Create monitor
```bash
curl -X POST https://pulseapi-production-c537.up.railway.app/api/v1/monitors \
 -H "Authorization: Bearer TOKEN_AQUI" \
 -H "Content-Type: application/json" \
 -d '{"name":"Google","url":"https://google.com","intervalSec":60,"timeoutMs":5000,"enabled":true}'
```

## Rodar testes
```bash
./mvnw test
```
Observação: os testes de integração usam Testcontainers e requerem Docker disponível. Sem Docker, esses testes serão pulados.
Se o Docker estiver instalado, mas os testes falharem por versão de API, confira `src/test/resources/testcontainers.properties`.

Nota: o `p95LatencyMs` é aproximado via ordenação no banco e offset do percentil (trade-off de precisão vs. performance).

## Deploy (Railway)
1. Crie um novo projeto e conecte este repositório.
2. Em `Variables`, adicione:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL` (string JDBC do Postgres)
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET` (min. 32 chars)
   - `CORS_ALLOWED_ORIGINS`
3. Garanta que o serviço do banco esteja provisionado.
4. Faça o deploy (Railway detecta Maven automaticamente).
5. Verifique `/actuator/health` para healthcheck.

## Deploy (Render)
1. Crie um novo Web Service e conecte este repositório.
2. Configure:
   - Build command: `./mvnw -DskipTests package`
   - Start command: `java -jar target/pulseapi-0.0.1-SNAPSHOT.jar`
3. Em `Environment`, adicione:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET` (min. 32 chars)
   - `CORS_ALLOWED_ORIGINS`
4. Provisione Postgres e conecte as credenciais.
5. Verifique `/actuator/health`.
