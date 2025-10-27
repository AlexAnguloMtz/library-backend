CREATE TABLE audit_event_type(
  id VARCHAR(40) PRIMARY KEY,
  resource_type_id VARCHAR(40) NOT NULL,
  FOREIGN KEY (resource_type_id) REFERENCES audit_resource_type(id)
);