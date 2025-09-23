# Gestor de Librería - Backend RESTful API

## Introducción

Este proyecto es el **backend RESTful API** de un gestor de librería.

La API permite gestionar usuarios, libros, préstamos y otras funcionalidades típicas de un sistema de librería.

## Documentación

- **Setup y desarrollo:** [docs/setup.md](docs/setup.md)

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot**
- **JPA (Java Persistence API)** – para casos de uso de escritura y edición con reglas de negocio y entidades.
- **jOOQ** – para consultas complejas y flexibles, cercanas a SQL nativo.
- **PostgreSQL**

---

## Características principales

- API RESTful con endpoints para CRUD
- Manejo de préstamos y estado de usuarios, libros y préstamos.
- Paginación, filtrado y ordenamiento de resultados.