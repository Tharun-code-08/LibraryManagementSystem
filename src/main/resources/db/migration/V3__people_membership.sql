-- V3: People profiles (student/faculty/librarian) and membership rules.

CREATE TABLE students (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    student_id     VARCHAR(30) NOT NULL,
    roll_number    VARCHAR(30) NOT NULL,
    department     VARCHAR(100),
    year           INT,
    semester       INT,
    photo_path     VARCHAR(255),
    phone          VARCHAR(30),
    address        VARCHAR(255),
    guardian_name  VARCHAR(150),
    guardian_phone VARCHAR(30),
    status         ENUM('ACTIVE','GRADUATED','SUSPENDED','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    branch_id      BIGINT NOT NULL,
    UNIQUE KEY uk_students_user (user_id),
    UNIQUE KEY uk_students_student_id (student_id),
    UNIQUE KEY uk_students_roll_number (roll_number),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_students_branch FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE RESTRICT,
    INDEX idx_students_dept_year (department, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE faculty (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    faculty_id   VARCHAR(30) NOT NULL,
    department   VARCHAR(100),
    designation  VARCHAR(100),
    phone        VARCHAR(30),
    office       VARCHAR(100),
    UNIQUE KEY uk_faculty_user (user_id),
    UNIQUE KEY uk_faculty_faculty_id (faculty_id),
    CONSTRAINT fk_faculty_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE librarians (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    employee_id VARCHAR(30) NOT NULL,
    branch_id   BIGINT NOT NULL,
    UNIQUE KEY uk_librarians_user (user_id),
    UNIQUE KEY uk_librarians_employee_id (employee_id),
    CONSTRAINT fk_librarians_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_librarians_branch FOREIGN KEY (branch_id) REFERENCES branches (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE membership_types (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    name               VARCHAR(80) NOT NULL,
    max_borrow_limit   INT NOT NULL,
    loan_period_days   INT NOT NULL,
    fine_per_day       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grace_period_days  INT NOT NULL DEFAULT 0,
    renewal_limit      INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_membership_types_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE memberships (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    membership_type_id BIGINT NOT NULL,
    holder_type        ENUM('STUDENT','FACULTY') NOT NULL,
    holder_id          BIGINT NOT NULL,
    start_date         DATE NOT NULL,
    expiry_date        DATE NOT NULL,
    status             ENUM('ACTIVE','EXPIRED','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_memberships_type FOREIGN KEY (membership_type_id) REFERENCES membership_types (id) ON DELETE RESTRICT,
    CONSTRAINT chk_memberships_dates CHECK (expiry_date > start_date),
    INDEX idx_memberships_holder (holder_type, holder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
