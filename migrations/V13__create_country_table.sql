CREATE SEQUENCE country_seq;

CREATE TABLE IF NOT EXISTS country (
  id int NOT NULL DEFAULT NEXTVAL ('country_seq'),
  iso char(2) NOT NULL,
  name varchar(80) NOT NULL,
  nicename varchar(80) NOT NULL,
  iso3 char(3) DEFAULT NULL,
  numcode smallint DEFAULT NULL,
  phonecode int NOT NULL,
  PRIMARY KEY (id)
);