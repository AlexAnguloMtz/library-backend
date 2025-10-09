CREATE TABLE book_copy(
    id SERIAL PRIMARY KEY,
    book_id INT NOT NULL,
    observations VARCHAR(100),
    FOREIGN KEY(book_id) REFERENCES book(id)
);