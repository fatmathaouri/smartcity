-- SmartCity Reporter - Migration V2
-- Ajout: départements, role_assignments, must_change_password

-- Colonnes ajoutées à users
ALTER TABLE users ADD COLUMN must_change_password BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN temp_password_expires_at DATETIME NULL;

-- Nouvelle table departments
CREATE TABLE IF NOT EXISTS departments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category_id BIGINT,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Nouvelle table role_assignments
CREATE TABLE IF NOT EXISTS role_assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at  DATETIME NULL,
    reason      TEXT,
    FOREIGN KEY (user_id)     REFERENCES users(id),
    FOREIGN KEY (role_id)     REFERENCES roles(id),
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);

-- Nouvelle table report_photos (SLA/media)
CREATE TABLE IF NOT EXISTS report_photos (
    report_id   BIGINT NOT NULL,
    photo_urls  VARCHAR(255),
    FOREIGN KEY (report_id) REFERENCES reports(id)
);

-- Index pour performances
CREATE INDEX idx_role_assignments_user ON role_assignments(user_id);
CREATE INDEX idx_role_assignments_active ON role_assignments(revoked_at);
CREATE INDEX idx_departments_active ON departments(is_active);
CREATE INDEX idx_users_must_change_pwd ON users(must_change_password);
