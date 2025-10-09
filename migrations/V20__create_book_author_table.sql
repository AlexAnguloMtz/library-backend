CREATE TABLE book_author (
    book_id   INT NOT NULL,
    author_id INT NOT NULL,
    position  INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE,
    CONSTRAINT check_position_non_negative CHECK (position >= 0)
);