# ntupay-backend (repositório dedicado)

Este diretório está pronto para ser usado como repositório independente do back-end.

## Stack
- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Swagger/OpenAPI (springdoc)

## Executar local
```bash
cd backend
export $(cat ../.env.local | xargs)
mvn spring-boot:run
```

## Executar testes
```bash
cd backend
mvn test
```

## Swagger
- UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoint UC001
`POST /api/v1/users/register`
