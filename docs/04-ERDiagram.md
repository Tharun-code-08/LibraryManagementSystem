# Entity Relationship Diagram

```mermaid
erDiagram
    ROLE ||--o{ USER_ROLE : has
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ ROLE_PERMISSION : grants
    PERMISSION ||--o{ ROLE_PERMISSION : granted_via
    USER ||--o{ SESSION : opens
    USER ||--o{ AUDIT_LOG : performs
    USER ||--o| STUDENT : profile
    USER ||--o| FACULTY : profile
    USER ||--o| LIBRARIAN : profile

    BRANCH ||--o{ USER : employs
    BRANCH ||--o{ BOOK_COPY : stocks
    BRANCH ||--o{ STUDENT : enrolls_at

    BOOK ||--o{ BOOK_COPY : "has copies"
    BOOK }o--o{ AUTHOR : "written by (BOOK_AUTHOR)"
    BOOK }o--|| PUBLISHER : published_by
    BOOK }o--|| CATEGORY : classified_as
    CATEGORY ||--o{ CATEGORY : "parent of (self-ref)"
    BOOK ||--o{ BOOK_TAG : tagged_with

    MEMBERSHIP_TYPE ||--o{ MEMBERSHIP : defines
    STUDENT ||--o| MEMBERSHIP : holds
    FACULTY ||--o| MEMBERSHIP : holds

    BOOK_COPY ||--o{ ISSUE : "issued as"
    MEMBERSHIP ||--o{ ISSUE : borrows
    ISSUE ||--o| RETURN : closed_by
    ISSUE ||--o{ FINE : incurs
    FINE ||--o{ PAYMENT : settled_by

    BOOK ||--o{ RESERVATION : reserved
    MEMBERSHIP ||--o{ RESERVATION : requests

    SUPPLIER ||--o{ PURCHASE_ORDER : fulfills
    PURCHASE_ORDER ||--o{ PURCHASE_ORDER_ITEM : contains
    BOOK ||--o{ PURCHASE_ORDER_ITEM : ordered_as
    PURCHASE_ORDER ||--o| INVOICE : billed_by

    BOOK_COPY ||--o{ INVENTORY_AUDIT_ITEM : verified_in
    INVENTORY_AUDIT ||--o{ INVENTORY_AUDIT_ITEM : contains

    USER ||--o{ NOTIFICATION : receives
    USER ||--o{ SYSTEM_LOG : generates

    USER {
        bigint id PK
        varchar username
        varchar email
        varchar password_hash
        varchar status
        datetime created_at
    }
    STUDENT {
        bigint id PK
        bigint user_id FK
        varchar student_id
        varchar roll_number
        varchar department
        int year
        int semester
        varchar photo_path
    }
    FACULTY {
        bigint id PK
        bigint user_id FK
        varchar faculty_id
        varchar department
        varchar designation
    }
    BOOK {
        bigint id PK
        varchar isbn
        varchar title
        varchar edition
        varchar volume
        varchar language
        bigint publisher_id FK
        bigint category_id FK
        decimal cost
        date purchase_date
    }
    BOOK_COPY {
        bigint id PK
        bigint book_id FK
        bigint branch_id FK
        varchar barcode
        varchar shelf
        varchar rack
        varchar row_label
        varchar condition
        varchar status
    }
    ISSUE {
        bigint id PK
        bigint book_copy_id FK
        bigint membership_id FK
        bigint issued_by FK
        datetime issue_date
        datetime due_date
    }
    RETURN {
        bigint id PK
        bigint issue_id FK
        bigint received_by FK
        datetime return_date
        varchar condition_on_return
    }
    FINE {
        bigint id PK
        bigint issue_id FK
        decimal amount
        varchar reason
        varchar status
    }
    PAYMENT {
        bigint id PK
        bigint fine_id FK
        decimal amount
        varchar method
        datetime paid_at
    }
    RESERVATION {
        bigint id PK
        bigint book_id FK
        bigint membership_id FK
        int queue_position
        datetime expires_at
        varchar status
    }
    AUDIT_LOG {
        bigint id PK
        bigint user_id FK
        varchar action
        varchar entity_type
        bigint entity_id
        text before_snapshot
        text after_snapshot
        datetime created_at
    }
```

## Cardinality Summary
- One `BOOK` → many `BOOK_COPY` (physical inventory units)
- One `BOOK_COPY` → many `ISSUE` over time, but at most one **open** issue at a time (enforced by
  business rule + partial unique index on `status = 'ISSUED'`)
- One `ISSUE` → zero-or-one `RETURN`, zero-or-many `FINE`
- One `FINE` → many `PAYMENT` (partial payments supported)
- `CATEGORY` is self-referencing to support unlimited-depth nested sub-categories
- `USER` is the security/identity root; `STUDENT`/`FACULTY`/`LIBRARIAN` are 1:1 profile extensions
