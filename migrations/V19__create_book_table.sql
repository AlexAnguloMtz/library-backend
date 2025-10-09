CREATE TABLE book(
    id SERIAL PRIMARY KEY,
    isbn VARCHAR(25) UNIQUE,
    title VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    publisher_id INT NOT NULL,
    year INT NOT NULL,
    image VARCHAR(255) NOT NULL,
    FOREIGN KEY(category_id) REFERENCES book_category(id) ON DELETE SET NULL
);