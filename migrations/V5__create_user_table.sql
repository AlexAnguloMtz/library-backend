CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(10) NOT NULL,
    profile_picture_url TEXT NOT NULL,
    registration_date TIMESTAMPTZ NOT NULL,
    role_id INT NOT NULL,
    gender_id INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES app_role(id),
    FOREIGN KEY (gender_id) REFERENCES gender(id)
);