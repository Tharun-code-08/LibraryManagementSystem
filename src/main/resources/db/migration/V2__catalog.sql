-- V2: Book catalog — authors, publishers, categories, books, tags, physical copies.

CREATE TABLE authors (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    biography    TEXT,
    nationality  VARCHAR(80),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_authors_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE publishers (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(150) NOT NULL,
    address  VARCHAR(255),
    phone    VARCHAR(30),
    email    VARCHAR(150),
    INDEX idx_publishers_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categories (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(120) NOT NULL,
    parent_id    BIGINT,
    description  VARCHAR(255),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE RESTRICT,
    INDEX idx_categories_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE books (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn             VARCHAR(20)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    subtitle         VARCHAR(255),
    edition          VARCHAR(50),
    volume           VARCHAR(50),
    language         VARCHAR(50),
    publisher_id     BIGINT,
    category_id      BIGINT,
    cost             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    purchase_date    DATE,
    vendor           VARCHAR(150),
    cover_image_path VARCHAR(255),
    pdf_path         VARCHAR(255),
    qr_code_path     VARCHAR(255),
    barcode_value    VARCHAR(50),
    is_deleted       TINYINT(1) NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_books_isbn (isbn),
    UNIQUE KEY uk_books_barcode (barcode_value),
    CONSTRAINT fk_books_publisher FOREIGN KEY (publisher_id) REFERENCES publishers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
    CONSTRAINT chk_books_cost CHECK (cost >= 0),
    FULLTEXT KEY ft_books_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_ba_author FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    UNIQUE KEY uk_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_tags (
    book_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (book_id, tag_id),
    CONSTRAINT fk_bt_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_bt_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_copies (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id     BIGINT NOT NULL,
    branch_id   BIGINT NOT NULL,
    barcode     VARCHAR(50) NOT NULL,
    shelf       VARCHAR(30),
    rack        VARCHAR(30),
    row_label   VARCHAR(30),
    `condition` ENUM('NEW','GOOD','WORN','DAMAGED','LOST') NOT NULL DEFAULT 'NEW',
    status      ENUM('AVAILABLE','ISSUED','RESERVED','MAINTENANCE','LOST','RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    acquired_at DATE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_book_copies_barcode (barcode),
    CONSTRAINT fk_copies_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE RESTRICT,
    CONSTRAINT fk_copies_branch FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE RESTRICT,
    INDEX idx_copies_status_branch (status, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
