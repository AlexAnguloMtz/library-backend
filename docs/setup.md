# Instrucciones para levantar el proyecto

Este proyecto utiliza Spring Boot con dos librerias para el acceso a la base de datos: JPA y jOOQ. A continuación se indican los pasos para levantarlo en un entorno de desarrollo.

## Requisitos

- Docker y Docker Compose
- IntelliJ IDEA
- Maven

## Pasos para levantar el proyecto

### 1. Levantar servicios con Docker Compose

Ejecuta el siguiente comando para levantar los contenedores necesarios:

```bash
docker compose -f dev.docker-compose.yml up
```

> Este comando levantará PostgreSQL y Flyway. Flyway ejecutará automáticamente las migraciones que se encuentran en la carpeta `./migrations`, asegurando que la base de datos esté lista para usar en desarrollo.

### 2. Generar clases JOOQ

Desde IntelliJ:

1. Abre el panel de **Maven**.
2. Localiza el plugin `jooq-codegen`.
3. Ejecuta la tarea `generate`.

Esto generará las clases JOOQ a partir del esquema de la base de datos.

### 3. Configurar perfil de Spring en IntelliJ

1. Abre **Run > Edit Configurations**.
2. Selecciona tu configuración de Spring Boot.
3. En **Environment Variables**, agrega:

```
SPRING_PROFILES_ACTIVE=dev
```

> Esto asegura que la aplicación use la configuración de desarrollo, con lo cual se insertarán en la base de datos algunas filas de ejemplo en las tablas para utilizar la aplicación

### ¿Por qué usamos **JPA** y **jOOQ** en el mismo proyecto?

En este proyecto combinamos **JPA** y **jOOQ**, cada uno enfocado en lo que hace mejor:

- **JPA**: lo usamos principalmente para casos de uso donde se necesita **crear, actualizar o eliminar** datos. Nos permite trabajar con entidades de manera sencilla y mantener un código más limpio en operaciones estándar de escritura.

- **jOOQ**: lo usamos en escenarios de **lecturas complejas**. Nos da mayor **flexibilidad** y un control más cercano al SQL, lo que facilita consultas avanzadas, joins complicados y optimizaciones específicas de la base de datos.

De esta forma, aprovechamos lo mejor de ambos mundos:
- **Simplicidad y productividad** con JPA en operaciones de escritura.
- **Poder y precisión** con jOOQ en consultas de lectura más sofisticadas.  
