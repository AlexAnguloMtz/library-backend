CREATE TABLE audit_event(
    id SERIAL PRIMARY KEY,
    resource_id VARCHAR(50) NOT NULL,
    responsible_id INT NOT NULL,
    event_type_id VARCHAR(40) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    FOREIGN KEY (responsible_id) REFERENCES app_user(id),
    FOREIGN KEY (event_type_id) REFERENCES audit_event_type(id)
);