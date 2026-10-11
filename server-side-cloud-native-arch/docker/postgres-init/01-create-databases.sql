-- Runs once, automatically, the first time the postgres container starts
-- (official Postgres image executes every *.sql in /docker-entrypoint-initdb.d).
-- Creates one database per microservice: strict "database per service" so
-- each service is the sole owner of, and only one with write access to, its
-- own schema.

CREATE DATABASE authdb;
CREATE DATABASE productdb;
CREATE DATABASE customerdb;
