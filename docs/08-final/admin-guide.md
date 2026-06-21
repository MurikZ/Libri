# Руководство администратора — Libri

## Требования к окружению

| Компонент | Требование |
|---|---|
| Java | JDK 17 LTS |
| PostgreSQL | 14+ |
| Android | API 26+ (Android 8.0) |
| Android Studio | Hedgehog 2023.1.1+ |

## Установка сервера

### 1. База данных
```sql
CREATE USER libri WITH PASSWORD 'libri_secret';
CREATE DATABASE libri_db OWNER libri;
\i docs/04-database/ddl.sql
```

### 2. Настройка application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/libri_db
spring.datasource.username=libri
spring.datasource.password=libri_secret
app.jwt.secret=YOUR_SECRET_KEY_MIN_256_BITS
app.jwt.expiration=86400000
```

### 3. Запуск сервера
```bash
cd server
mvn spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html

## Установка Android-клиента

```bash
cd app
./gradlew assembleDebug
./gradlew installDebug
```

При первом запуске БД заполняется тестовыми данными автоматически.

## Тестовые аккаунты

| Email | Пароль | Роль |
|---|---|---|
| reader@lib.ru | 123456 | READER |
| librarian@lib.ru | 123456 | LIBRARIAN |
| admin@lib.ru | 123456 | ADMIN |

## Управление пользователями

Администратор может управлять пользователями через экран «Библиотекарь»:
- Просматривать список всех пользователей
- Изменять роли (READER / LIBRARIAN / ADMIN)
- Просматривать историю выдач любого пользователя