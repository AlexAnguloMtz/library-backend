CREATE TABLE user_role (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user
      FOREIGN KEY(user_id)
      REFERENCES app_user(id)
      ON DELETE CASCADE,
  CONSTRAINT fk_role
      FOREIGN KEY(role_id)
      REFERENCES app_role(id)
      ON DELETE CASCADE
);