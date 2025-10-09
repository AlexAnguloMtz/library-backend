CREATE TABLE book_loan (
    id SERIAL PRIMARY KEY,
    book_copy_id INT NOT NULL,
    user_id INT NOT NULL,
    responsible_id INT NOT NULL,
    loan_date TIMESTAMPTZ NOT NULL,
    due_date TIMESTAMPTZ NOT NULL,
    return_date TIMESTAMPTZ,
    FOREIGN KEY (book_copy_id) REFERENCES book_copy(id),
    FOREIGN KEY (user_id) REFERENCES app_user(id),
    FOREIGN KEY (responsible_id) REFERENCES app_user(id)
);