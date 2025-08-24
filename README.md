# BankingSystem

Spring Boot + PostgreSQL, миграции Flyway, OpenAPI (Swagger).

## Системные требования

* **JDK 21**
* **Maven 3.9+**
* (опционально) **Docker Desktop** 4.x
* Свободные порты: **8080** (приложение) и **5432** (PostgreSQL)

## Быстрый старт в Docker

Файлы расположения:

* `./Dockerfile`
* `./docker-compose.yaml`
* `./src/main/resources/application.properties`

### 1) Запуск

```bash
docker compose up --build -d
```

Проверить статус:

```bash
docker compose ps
docker compose logs -f app
```

### 2) Swagger / OpenAPI

* Swagger UI: **[http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)**
* OpenAPI JSON: **[http://localhost:8080/api-docs](http://localhost:8080/api-docs)**

Вы можете **протестировать API прямо в Swagger UI**.

### Переменные окружения (Compose)

В контейнере используются значения из `application.properties`, но для подключения к БД **хост должен быть `db`**, поэтому в `docker-compose.yaml` заданы:

```
DB_URL=jdbc:postgresql://db:5432/bank
DB_USER=bank
DB_PASS=bank
```

## Локальный запуск без Docker

1. Поднимите локальный PostgreSQL и создайте пользователя/БД:

```sql
CREATE USER bank WITH PASSWORD 'bank';
CREATE DATABASE bank OWNER bank;
GRANT ALL PRIVILEGES ON DATABASE bank TO bank;
```

2. Запуск приложения:

```bash
mvn clean spring-boot:run
```

3. Открыть Swagger:

* [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)
* [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Тесты
```bash
mvn test
```

## Конфигурация (важное из `application.properties`)

```properties
server.port=8080

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/bank}
spring.datasource.username=${DB_USER:bank}
spring.datasource.password=${DB_PASS:bank}

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui
```

## Путь к файлу с SQL запросами для 2 части задания
* `./src/main/java/ab/task/banking_system/sql/queries.sql`
