CREATE TABLE audit_event (
    id SERIAL PRIMARY KEY,
    responsible_id INT NOT NULL,
    event_type_id VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    event_data JSONB NOT NULL,
    FOREIGN KEY (responsible_id) REFERENCES app_user(id),
    FOREIGN KEY (event_type_id) REFERENCES audit_event_type(id)
);
