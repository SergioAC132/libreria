# Biblioteca API

API REST para gestionar una **biblioteca**: catálogo de libros, socios (personas) y préstamos. Controla que no se presten más ejemplares de los que tiene un libro, registra la devolución de cada préstamo y valida los datos de cada recurso.

> **Sin autenticación.** Según las indicaciones de la asignatura, esta entrega no implementa login: todos los endpoints son públicos. Las contraseñas de las personas se guardan cifradas con BCrypt y nunca se devuelven en las respuestas.

**Autor:** Sergio Alvarado Carrasco

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje |
| Spring Boot 4.1.1 | Framework (Web MVC, Data JPA) |
| Hibernate / JPA | Persistencia |
| MySQL 8 | Base de datos |
| Spring Security Crypto | Cifrado de contraseñas (BCrypt) |
| Maven (wrapper incluido) | Compilación y ejecución |
| Thymeleaf + Bootstrap 5 | Interfaz web opcional en `/vista/...` (extra, no necesaria para usar la API) |

## Requisitos previos

- JDK 21
- MySQL 8 en ejecución
- Postman (o similar) para probar la API

## Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone <URL-DEL-REPOSITORIO>
cd libreria

# 2. Configurar las credenciales de MySQL (ver la sección siguiente)

# 3. Ejecutar
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

La API queda en `http://localhost:8080`.

No hace falta crear las tablas a mano: Hibernate las genera al arrancar (`ddl-auto=update`). La base `libreria` se crea sola si no existe (`createDatabaseIfNotExist=true` en la URL por defecto).

### Configurar la conexión a la base de datos

El proyecto necesita un usuario de MySQL con permiso para crear la base `libreria`. Hay dos formas de indicar las credenciales; elige una.

#### Opción A: variables de entorno (recomendada)

No hay que tocar ningún archivo, así las credenciales nunca terminan en el repositorio.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/libreria?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC` |
| `DB_USERNAME` | Usuario de MySQL | *(sin valor: obligatoria)* |
| `DB_PASSWORD` | Contraseña de MySQL | *(sin valor: obligatoria si tu usuario tiene contraseña)* |

```bash
# Linux / macOS
export DB_USERNAME=mi_usuario
export DB_PASSWORD=mi_password
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:DB_USERNAME="mi_usuario"
$env:DB_PASSWORD="mi_password"
.\mvnw.cmd spring-boot:run
```

Si tu MySQL no está en `localhost:3306`, define también `DB_URL`.

#### Opción B: editar `application.properties`

Abre `src/main/resources/application.properties` y haz esto:

1. **Comenta** las tres líneas que leen variables de entorno (agrégales `#` al inicio).
2. **Descomenta** las tres líneas de abajo y escribe tus datos.

```properties
# spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/libreria?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC}
# spring.datasource.username=${DB_USERNAME:}
# spring.datasource.password=${DB_PASSWORD:}

spring.datasource.url=jdbc:mysql://localhost:3306/libreria?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=mi_usuario
spring.datasource.password=mi_password
```

Después ejecuta normalmente con `./mvnw spring-boot:run`.

> **Importante:** si usas esta opción, no subas el archivo con tus credenciales reales a GitHub. Deja las líneas vacías en el repositorio y rellénalas solo en tu copia local.

## Estructura del proyecto

```
src/main/java/com/api/libreria
├── controller/      Controladores REST (/libros, /personas, /prestamos) y de vistas (/vista/...)
├── exceptions/      Manejo global de errores de la API
├── models/          Entidades JPA (LibroModel, PersonaModel, PrestamoModel)
├── repositories/    Repositorios Spring Data JPA
└── services/        Reglas de negocio y validaciones
```

## Modelo de datos

| Entidad | Tabla | Descripción |
|---|---|---|
| `LibroModel` | `libros` | Libro del catálogo, con su número de ejemplares |
| `PersonaModel` | `personas` | Socio de la biblioteca |
| `PrestamoModel` | `prestamos` | Préstamo de un libro a una persona |

Relaciones: un préstamo pertenece a **un libro** y a **una persona** (socio); un libro puede tener muchos préstamos y una persona también.

Formatos de fecha: fecha simple `yyyy-MM-dd` (por ejemplo `1995-06-21`) y fecha con hora `yyyy-MM-ddTHH:mm:ss` (por ejemplo `2026-11-10T18:00:00`).

## Reglas de negocio

- **Identificadores.** El `isbn` de un libro y el `id` de personas y préstamos los genera la base de datos. Si se envían al crear, se ignoran.
- **Libros.** Requieren `titulo`, `autor` y al menos 1 ejemplar (`numeroEjemplares`).
- **Personas.** Requieren `nombre`, `apellidoPaterno`, `correoElectronico` y `password`. El correo no se puede repetir. La contraseña se cifra con BCrypt y no aparece en ninguna respuesta.
- **Préstamos.** Se crean enviando solo el `isbn` del libro y el `id` del socio. Un préstamo nace **en curso**.
- **Disponibilidad.** Un libro se puede prestar mientras su número de préstamos **en curso** (sin devolver) sea menor que su número de ejemplares; si no, la API responde que no hay ejemplares disponibles.
- **Devolución.** `PUT /prestamos/{id}/devolver` guarda la fecha y hora reales en `fechaDevolucionReal` y el ejemplar queda libre otra vez. Un préstamo no se puede devolver dos veces, y un préstamo ya devuelto no se puede modificar. Eliminar un préstamo en curso también libera el ejemplar.
- **Fechas de un préstamo.** `fechaPrestamo` se asigna sola con la fecha y hora actuales y no se puede modificar. `fechaDevolucion` es la **fecha límite** acordada: es opcional y, si se envía, debe ser posterior a `fechaPrestamo`. `fechaDevolucionReal` solo la llena el sistema al devolver, y `devuelto` (`true` o `false`) se calcula a partir de ella.
- **Borrados.** No se puede eliminar un libro ni una persona que tiene préstamos registrados.

## Endpoints

Base URL: `http://localhost:8080`. Todos los cuerpos van en JSON (`Content-Type: application/json`).

### Libros — `/libros`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/libros` | Listar libros | 200 |
| GET | `/libros/{isbn}` | Obtener un libro | 200 / 404 |
| GET | `/libros/autor/{autor}` | Libros de un autor (nombre exacto) | 200 |
| GET | `/libros/anio/{anio}` | Libros de un año de publicación | 200 |
| POST | `/libros` | Crear libro | 201 / 400 |
| PUT | `/libros/{isbn}` | Actualizar libro | 200 / 400 / 404 |
| DELETE | `/libros/{isbn}` | Eliminar libro | 204 / 404 / 409 |

### Personas — `/personas`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/personas` | Listar personas | 200 |
| GET | `/personas/{id}` | Obtener una persona | 200 / 404 |
| GET | `/personas/correo/{correo}` | Buscar una persona por su correo | 200 / 404 |
| POST | `/personas` | Crear persona | 201 / 400 |
| PUT | `/personas/{id}` | Actualizar datos de la persona (no cambia la contraseña) | 200 / 400 / 404 |
| DELETE | `/personas/{id}` | Eliminar persona | 204 / 404 / 409 |

### Préstamos — `/prestamos`

| Método | Ruta | Descripción | Respuesta |
|---|---|---|---|
| GET | `/prestamos` | Listar todos los préstamos | 200 |
| GET | `/prestamos/activos` | Listar solo los préstamos en curso (sin devolver) | 200 |
| GET | `/prestamos/{id}` | Obtener un préstamo | 200 / 404 |
| POST | `/prestamos` | Registrar préstamo | 201 / 400 |
| PUT | `/prestamos/{id}` | Cambiar libro, socio o fecha límite (no aplica a préstamos devueltos) | 200 / 400 / 404 / 409 |
| PUT | `/prestamos/{id}/devolver` | Registrar la devolución del libro | 200 / 404 / 409 |
| DELETE | `/prestamos/{id}` | Eliminar préstamo | 204 / 404 |

## Manejo de errores

Los errores de la API devuelven un JSON con un único campo `error`:

```json
{ "error": "Libro no encontrado con ISBN: 99" }
```

| Código | Cuándo |
|---|---|
| 400 Bad Request | Datos faltantes o inválidos, el libro o la persona de un préstamo no existen, no hay ejemplares disponibles, JSON o fechas con formato incorrecto |
| 404 Not Found | El recurso buscado por `id` o `isbn` no existe. En los `GET` por id y en los `DELETE` la respuesta no trae cuerpo |
| 409 Conflict | Se intenta eliminar un libro o una persona que tiene préstamos registrados, devolver un préstamo que ya fue devuelto o modificar uno ya devuelto |

## Ejemplos de peticiones y respuestas

Flujo completo de uso. Los IDs dependen de tu base de datos.

> **Nota:** el campo del año de publicación se llama `añoPublicacion` (con ñ). Escríbelo exactamente así en el JSON.

### 1. Crear un libro

`POST /libros`

```json
{
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "añoPublicacion": "1967",
  "numeroEjemplares": 2
}
```

Respuesta `201 Created`:

```json
{
  "isbn": 1,
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "añoPublicacion": "1967",
  "numeroEjemplares": 2
}
```

### 2. Crear una persona (socio)

`POST /personas`

```json
{
  "nombre": "Laura",
  "apellidoPaterno": "Méndez",
  "apellidoMaterno": "Cruz",
  "fechaNacimiento": "1995-06-21",
  "correoElectronico": "laura.mendez@correo.com",
  "telefono": "2299876543",
  "direccion": "Calle Reforma 12, Veracruz",
  "password": "Secreta123"
}
```

Respuesta `201 Created` (sin contraseña):

```json
{
  "id": 1,
  "nombre": "Laura",
  "apellidoPaterno": "Méndez",
  "apellidoMaterno": "Cruz",
  "fechaNacimiento": "1995-06-21",
  "correoElectronico": "laura.mendez@correo.com",
  "telefono": "2299876543",
  "direccion": "Calle Reforma 12, Veracruz"
}
```

### 3. Registrar un préstamo

Solo hace falta indicar el `isbn` del libro y el `id` del socio.

`POST /prestamos`

```json
{
  "libro": { "isbn": 1 },
  "socio": { "id": 1 },
  "fechaDevolucion": "2026-11-10T18:00:00"
}
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "libro": {
    "isbn": 1,
    "titulo": "Cien años de soledad",
    "autor": "Gabriel García Márquez",
    "añoPublicacion": "1967",
    "numeroEjemplares": 2
  },
  "socio": {
    "id": 1,
    "nombre": "Laura",
    "apellidoPaterno": "Méndez",
    "apellidoMaterno": "Cruz",
    "fechaNacimiento": "1995-06-21",
    "correoElectronico": "laura.mendez@correo.com",
    "telefono": "2299876543",
    "direccion": "Calle Reforma 12, Veracruz"
  },
  "fechaPrestamo": "2026-10-12T09:15:00",
  "fechaDevolucion": "2026-11-10T18:00:00",
  "fechaDevolucionReal": null,
  "devuelto": false
}
```

`fechaDevolucion` es la fecha límite acordada. El préstamo queda **en curso** (`devuelto: false`) hasta que se registre la devolución.

### 4. Devolver un libro

`PUT /prestamos/1/devolver` — sin cuerpo.

Respuesta `200 OK` (los objetos `libro` y `socio` vienen completos, igual que en el ejemplo anterior; aquí se abrevian):

```json
{
  "id": 1,
  "libro": { ... },
  "socio": { ... },
  "fechaPrestamo": "2026-10-12T09:15:00",
  "fechaDevolucion": "2026-11-10T18:00:00",
  "fechaDevolucionReal": "2026-10-20T16:40:00",
  "devuelto": true
}
```

El ejemplar queda libre: ese libro se puede volver a prestar.

### 5. Actualizar un libro

`PUT /libros/1`

```json
{
  "titulo": "Cien años de soledad",
  "autor": "Gabriel García Márquez",
  "añoPublicacion": "1967",
  "numeroEjemplares": 5
}
```

Respuesta `200 OK` con el libro actualizado.

### 6. Consultas

- `GET /libros/autor/Gabriel García Márquez` → lista los libros de ese autor.
- `GET /libros/anio/1967` → lista los libros publicados ese año.
- `GET /prestamos/activos` → lista los préstamos que todavía no se devuelven.
- `GET /personas/correo/laura.mendez@correo.com` → devuelve la persona con ese correo.

### Ejemplos de error

`POST /libros` sin título → `400 Bad Request`:

```json
{ "error": "El título es obligatorio." }
```

`POST /personas` con un correo ya registrado → `400 Bad Request`:

```json
{ "error": "Ya existe una persona con ese correo electrónico." }
```

`POST /prestamos` cuando ya se prestaron todos los ejemplares → `400 Bad Request`:

```json
{ "error": "No hay ejemplares disponibles de \"Cien años de soledad\"." }
```

`PUT /prestamos/1/devolver` cuando el préstamo ya fue devuelto → `409 Conflict`:

```json
{ "error": "Este préstamo ya fue devuelto." }
```

`PUT /prestamos/1` sobre un préstamo ya devuelto → `409 Conflict`:

```json
{ "error": "No se puede modificar un préstamo que ya fue devuelto." }
```

`GET /libros/99` → `404 Not Found` (sin cuerpo).

`PUT /libros/99` → `404 Not Found`:

```json
{ "error": "Libro no encontrado con ISBN: 99" }
```

`DELETE /libros/1` cuando tiene préstamos → `409 Conflict`:

```json
{ "error": "No se puede eliminar el libro porque tiene préstamos registrados." }
```

## Interfaz web (extra)

Además de la API, el proyecto incluye pantallas Thymeleaf para administrar todo desde el navegador, sin necesidad de Postman:

`http://localhost:8080/vista/libros`, `/vista/personas` y `/vista/prestamos` (la raíz `/` abre la de personas).