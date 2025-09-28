CREATE TABLE book(
    id SERIAL PRIMARY KEY,
    isbn VARCHAR(25) UNIQUE,
    title VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    year SMALLINT NOT NULL,
    FOREIGN KEY(category_id) REFERENCES book_category(id)
);