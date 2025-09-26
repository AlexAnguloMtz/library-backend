CREATE TABLE user_address (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    address VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    zip_code VARCHAR(100) NOT NULL,
    state_id INT NOT NULL REFERENCES state(id),
    CONSTRAINT one_address_per_user UNIQUE (user_id)  -- For now, each user can have only one address
);

ALTER TABLE user_address
ADD CONSTRAINT valid_zip_code CHECK (zip_code ~ '^\d{5}$');