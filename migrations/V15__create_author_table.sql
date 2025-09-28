CREATE TABLE author(
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    country_id INT NOT NULL,
    FOREIGN KEY(country_id) REFERENCES country(id)
);