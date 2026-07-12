-- =============================================================================
-- V3__seed_permissions.sql
-- RBAC izinlerini seed eder ve rollere atar
-- ADMIN → tüm izinler | USER → sadece READ izinleri
-- =============================================================================

INSERT INTO permissions (id, name, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), perm.name, NOW(), NOW(), 'SYSTEM', 'SYSTEM'
FROM (
    VALUES
        ('OUTLET_CREATE'),
        ('OUTLET_UPDATE'),
        ('OUTLET_READ'),
        ('CATEGORY_CREATE'),
        ('CATEGORY_READ'),
        ('PRODUCT_CREATE'),
        ('PRODUCT_UPDATE'),
        ('PRODUCT_DELETE'),
        ('PRODUCT_READ')
) AS perm(name)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.name = perm.name
);

-- ADMIN: tüm izinler
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- USER: sadece okuma izinleri
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
INNER JOIN permissions p ON p.name IN ('OUTLET_READ', 'CATEGORY_READ', 'PRODUCT_READ')
WHERE r.name = 'USER'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
