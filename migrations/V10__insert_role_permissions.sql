INSERT INTO role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM app_role WHERE slug = 'ADMIN') AS role_id,
    p.id AS permission_id
FROM permission p
WHERE p.name IN (
    'users:read',
    'users:read:self',
    'users:create',
    'users:update',
    'users:delete',

    'books:read',
    'books:create',
    'books:update',
    'books:delete',

    'authors:read',
    'authors:create',
    'authors:edit',
    'authors:delete',

    'book-categories:read',
    'book-categories:create',
    'book-categories:update',
    'book-categories:delete'
);

INSERT INTO role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM app_role WHERE slug = 'LIBRARIAN') AS role_id,
    p.id AS permission_id
FROM permission p
WHERE p.name IN (
    'users:read',
    'users:read:self',
    'users:create',
    'users:update',
    'users:delete',

    'books:read',
    'books:create',
    'books:update',
    'books:delete',

    'authors:read',
    'authors:create',
    'authors:edit',
    'authors:delete',

    'book-categories:read',
    'book-categories:create',
    'book-categories:update',
    'book-categories:delete'
);

INSERT INTO role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM app_role WHERE slug = 'USER') AS role_id,
    p.id AS permission_id
FROM permission p
WHERE p.name IN (
    'users:read:self',
    'books:read',
    'authors:read',
    'book-categories:read'
);