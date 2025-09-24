CREATE TABLE role_permission (
    role_id INT NOT NULL
        REFERENCES app_role(id)
        ON DELETE CASCADE,
    permission_id INT NOT NULL
        REFERENCES permission(id)
        ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
