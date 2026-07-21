-- V5: Suppliers, purchase orders/invoices, inventory audits.

CREATE TABLE suppliers (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    contact_person VARCHAR(150),
    phone          VARCHAR(30),
    email          VARCHAR(150),
    address        VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase_orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,
    ordered_by      BIGINT NOT NULL,
    order_date      DATE NOT NULL,
    status          ENUM('DRAFT','PENDING_APPROVAL','APPROVED','RECEIVED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
    budget_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    approved_by     BIGINT,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_ordered_by FOREIGN KEY (ordered_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_approved_by FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_po_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase_order_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    book_id           BIGINT,
    description       VARCHAR(255) NOT NULL,
    quantity          INT NOT NULL,
    unit_cost         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_poi_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE SET NULL,
    CONSTRAINT chk_poi_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invoices (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id  BIGINT NOT NULL,
    invoice_number     VARCHAR(60) NOT NULL,
    invoice_date       DATE NOT NULL,
    total_amount       DECIMAL(12,2) NOT NULL,
    file_path          VARCHAR(255),
    UNIQUE KEY uk_invoices_number (invoice_number),
    CONSTRAINT fk_invoices_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_audits (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id     BIGINT NOT NULL,
    conducted_by  BIGINT NOT NULL,
    started_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at  DATETIME,
    status        ENUM('IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_audit_branch FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE RESTRICT,
    CONSTRAINT fk_audit_conducted_by FOREIGN KEY (conducted_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_audit_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_audit_id  BIGINT NOT NULL,
    book_copy_id        BIGINT NOT NULL,
    expected_status     VARCHAR(30) NOT NULL,
    found_status        VARCHAR(30) NOT NULL,
    notes               VARCHAR(255),
    CONSTRAINT fk_audit_item_audit FOREIGN KEY (inventory_audit_id) REFERENCES inventory_audits (id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_item_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
