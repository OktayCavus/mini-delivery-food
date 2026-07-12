-- =============================================================================
-- V2__migrate_role_enum_to_rbac.sql
-- Enum tabanlı users.role kolonundan RBAC tablolarına geçiş
-- Kaynak: V1__baseline.sql (users.role = 'USER' | 'ADMIN')
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. roles
-- Entity: com.cavus.delivery_food.auth.entity.Role
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id          UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

-- -----------------------------------------------------------------------------
-- 2. permissions
-- Entity: com.cavus.delivery_food.auth.entity.Permission
-- Not: İzin seed'leri Faz 3 (@PreAuthorize) ile eklenecek; tablo şimdilik boş kalabilir
-- -----------------------------------------------------------------------------
CREATE TABLE permissions (
    id          UUID            NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP(6),
    updated_at  TIMESTAMP(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

-- -----------------------------------------------------------------------------
-- 3. user_roles (User ↔ Role N:M)
-- Entity: com.cavus.delivery_food.auth.entity.User.roles
-- -----------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- -----------------------------------------------------------------------------
-- 4. role_permissions (Role ↔ Permission N:M)
-- Entity: com.cavus.delivery_food.auth.entity.Role.permissions
-- -----------------------------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);

-- -----------------------------------------------------------------------------
-- 5. Varsayılan rolleri seed et (USER, ADMIN)
-- -----------------------------------------------------------------------------
INSERT INTO roles (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), 'USER', NOW(), NOW(), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO roles (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), 'ADMIN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

-- -----------------------------------------------------------------------------
-- 6. Mevcut users.role verisini user_roles tablosuna taşı
-- Not: users.role kolonu yalnızca V1 baseline uygulanmış DB'lerde vardır
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND column_name = 'role'
    ) THEN
        INSERT INTO user_roles (user_id, role_id)
        SELECT u.id, r.id
        FROM users u
        INNER JOIN roles r ON r.name = u.role
        WHERE u.role IN ('USER', 'ADMIN')
          AND NOT EXISTS (
              SELECT 1
              FROM user_roles ur
              WHERE ur.user_id = u.id
                AND ur.role_id = r.id
          );
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 7. Eski users.role kolonunu kaldır
-- Not: User entity artık role kolonunu map etmediği için NOT NULL kolon bırakılırsa
--      yeni kayıt (register) insert'leri başarısız olur. Bu yüzden V2'de kaldırılıyor.
-- -----------------------------------------------------------------------------
ALTER TABLE users DROP COLUMN IF EXISTS role;
