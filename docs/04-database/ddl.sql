-- Libri Database Schema — PostgreSQL 14+

CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(60)  NOT NULL,
    first_name        VARCHAR(50)  NOT NULL,
    last_name         VARCHAR(50)  NOT NULL,
    phone             VARCHAR(20),
    role              VARCHAR(20)  NOT NULL DEFAULT 'READER',
    registration_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE authors (
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name  VARCHAR(50) NOT NULL
);

CREATE TABLE books (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    isbn             VARCHAR(13) UNIQUE,
    publication_year SMALLINT,
    publisher        VARCHAR(100),
    description      TEXT
);

CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL REFERENCES books(id)   ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_instances (
    id               BIGSERIAL PRIMARY KEY,
    book_id          BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    inventory_number VARCHAR(50) UNIQUE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_instance_status
        CHECK (status IN ('AVAILABLE','ON_LOAN','RESERVED','DAMAGED','LOST'))
);

CREATE TABLE loans (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id)          ON DELETE CASCADE,
    book_instance_id BIGINT NOT NULL REFERENCES book_instances(id) ON DELETE RESTRICT,
    loan_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date         DATE NOT NULL,
    return_date      DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_loan_status CHECK (status IN ('ACTIVE','RETURNED','OVERDUE'))
);

CREATE TABLE reservations (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    book_id          BIGINT NOT NULL REFERENCES books(id)  ON DELETE CASCADE,
    reservation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date      DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_reservation_status
        CHECK (status IN ('PENDING','ACTIVE','EXPIRED','CANCELLED'))
);

CREATE TABLE fines (
    id              BIGSERIAL PRIMARY KEY,
    loan_id         BIGINT        NOT NULL UNIQUE REFERENCES loans(id) ON DELETE CASCADE,
    user_id         BIGINT        NOT NULL REFERENCES users(id)        ON DELETE CASCADE,
    amount          NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    paid            BOOLEAN       NOT NULL DEFAULT FALSE,
    calculated_date DATE          NOT NULL DEFAULT CURRENT_DATE,
    reason          TEXT
);

-- Индексы
CREATE INDEX idx_loans_user_status   ON loans(user_id, status);
CREATE INDEX idx_loans_due_date      ON loans(due_date) WHERE status = 'ACTIVE';
CREATE INDEX idx_fines_user_unpaid   ON fines(user_id) WHERE paid = FALSE;
CREATE INDEX idx_books_title         ON books(title);
CREATE INDEX idx_reservations_expiry ON reservations(expiry_date) WHERE status = 'PENDING';
CREATE INDEX idx_book_instances_book ON book_instances(book_id, status);

-- Тестовые данные
INSERT INTO authors (first_name, last_name) VALUES
('Фёдор',     'Достоевский'),
('Лев',       'Толстой'),
('Михаил',    'Булгаков'),
('Джордж',    'Оруэлл'),
('Джоан',     'Роулинг'),
('Антуан',    'де Сент-Экзюпери'),
('Эрих Мария','Ремарк'),
('Александр', 'Пушкин'),
('Борис',     'Пастернак');

INSERT INTO books (title, isbn, publication_year, publisher) VALUES
('Преступление и наказание',          '9785170839735', 1866, 'АСТ'),
('Война и мир',                       '9785171134389', 1869, 'АСТ'),
('Мастер и Маргарита',                '9785171058371', 1967, 'АСТ'),
('1984',                              '9785171145521', 1949, 'АСТ'),
('Гарри Поттер и философский камень', '9785389077843', 1997, 'Махаон'),
('Маленький принц',                   '9785171064976', 1943, 'АСТ'),
('Три товарища',                      '9785170612178', 1936, 'АСТ'),
('Евгений Онегин',                    '9785171059859', 1833, 'АСТ'),
('Доктор Живаго',                     '9785040947478', 1957, 'Эксмо'),
('Идиот',                             '9785171145057', 1869, 'АСТ');

INSERT INTO book_authors (book_id, author_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,1);

INSERT INTO book_instances (book_id, inventory_number, status) VALUES
(1,'INV-001-1','AVAILABLE'),(1,'INV-001-2','AVAILABLE'),
(2,'INV-002-1','AVAILABLE'),(2,'INV-002-2','ON_LOAN'),
(3,'INV-003-1','AVAILABLE'),(4,'INV-004-1','AVAILABLE'),
(5,'INV-005-1','AVAILABLE'),(5,'INV-005-2','AVAILABLE'),
(6,'INV-006-1','AVAILABLE'),(7,'INV-007-1','AVAILABLE'),
(8,'INV-008-1','AVAILABLE'),(9,'INV-009-1','AVAILABLE'),
(10,'INV-010-1','AVAILABLE');

-- Пользователи (пароль: 123456, BCrypt rounds=12)
INSERT INTO users (email, password_hash, first_name, last_name, role) VALUES
('reader@lib.ru',    '$2a$12$DwKilNkCCWw5ySwRPVOePOI5mNNV7tEbxwvXDCCwHmwAWJlvpzqZi', 'Иван',   'Петров',   'READER'),
('librarian@lib.ru', '$2a$12$DwKilNkCCWw5ySwRPVOePOI5mNNV7tEbxwvXDCCwHmwAWJlvpzqZi', 'Мария',  'Сидорова', 'LIBRARIAN'),
('admin@lib.ru',     '$2a$12$DwKilNkCCWw5ySwRPVOePOI5mNNV7tEbxwvXDCCwHmwAWJlvpzqZi', 'Алексей','Иванов',   'ADMIN');
