# API REST de Alquiler de Bicicletas

Sistema de gestión de alquiler de bicicletas urbanas desarrollado en **Java 17 + Spring Boot 3.2 + MySQL 8**. Permite registrar bicicletas, iniciar y finalizar alquileres, calcular automáticamente el costo según el tipo de bicicleta y aplicar multas por devoluciones tardías.

> Prueba técnica · Jacobo Solarte

---

## 🚀 Despliegue en producción

La aplicación está desplegada y accesible públicamente:

| Recurso | URL / Plataforma |
|---|---|
| **API en vivo (Azure)** | https://pruebatecnica-ddc6bzeudpdmdfdv.eastus-01.azurewebsites.net |
| **Swagger UI** | https://pruebatecnica-ddc6bzeudpdmdfdv.eastus-01.azurewebsites.net/swagger-ui.html |
| **OpenAPI JSON** | https://pruebatecnica-ddc6bzeudpdmdfdv.eastus-01.azurewebsites.net/v3/api-docs |
| **Listar bicicletas** | https://pruebatecnica-ddc6bzeudpdmdfdv.eastus-01.azurewebsites.net/api/bicicletas |

### Infraestructura

- **API (Spring Boot)** → desplegada en **Microsoft Azure App Service** (Linux + Java 17, plan B1), con despliegue continuo desde **GitHub Actions**: cada push al repositorio dispara un build con Maven y publica el JAR automáticamente.
- **Base de datos (MySQL 8)** → hospedada en **Railway**, accesible públicamente vía TLS. Las tablas se crean automáticamente con Hibernate (`ddl-auto=update`) y `data.sql` inserta las bicicletas iniciales de forma idempotente.

```
┌─────────────────────────┐
│ GitHub (repositorio)    │
│   └ código fuente       │
└──────────┬──────────────┘
           │ push
           ▼
┌─────────────────────────┐          ┌──────────────────────┐
│ GitHub Actions          │ ─build──►│ Azure App Service    │
│  (mvn package)          │ + JAR    │ Spring Boot + Java 17│
└─────────────────────────┘          └──────────┬───────────┘
                                                │ JDBC sobre TLS
                                                ▼
                                     ┌──────────────────────┐
                                     │ Railway MySQL 8      │
                                     │ ballast.proxy.rlwy   │
                                     └──────────────────────┘
```

Las credenciales de la base de datos están externalizadas en **variables de entorno de Azure App Service** (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE=prod`), por lo que **no viajan en el código fuente** del repositorio.

---

## Tabla de contenidos

1. [Requisitos previos](#1-requisitos-previos)
2. [Instalación y ejecución](#2-instalación-y-ejecución)
3. [Documentación interactiva (Swagger)](#3-documentación-interactiva-swagger)
4. [Endpoints disponibles](#4-endpoints-disponibles)
5. [Reglas de negocio](#5-reglas-de-negocio)
6. [Arquitectura](#6-arquitectura)
7. [Estructura del proyecto](#7-estructura-del-proyecto)
8. [Tests](#8-tests)
9. [Ejemplos de uso](#9-ejemplos-de-uso)
10. [Criterios de evaluación: cómo se abordaron](#10-criterios-de-evaluación-cómo-se-abordaron)

---

## 1. Requisitos previos

| Herramienta | Versión mínima |
|---|---|
| Java JDK | 17 (probado con 21) |
| Maven | 3.8+ |
| MySQL | 8.0 |

Verifica las versiones:
```bash
java -version
mvn -version
mysql --version
```

---

## 2. Instalación y ejecución

### Paso 1 — Crear la base de datos

Conéctate a MySQL y ejecuta:
```sql
CREATE DATABASE IF NOT EXISTS alquiler_db;
```

### Paso 2 — Configurar credenciales

Edita `src/main/resources/application.properties` con tus credenciales locales:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/alquiler_db
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

### Paso 3 — Descargar dependencias y compilar

Desde la raíz del proyecto:
```bash
mvn clean install -DskipTests
```

> La primera ejecución descarga Spring Boot, MySQL Connector, SpringDoc OpenAPI, etc. (tarda 2–5 minutos).

### Paso 4 — Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Espera a ver el mensaje:
```
Started AlquilerApiApplication in X.XXX seconds
```

La aplicación quedará disponible en **http://localhost:8080**.

> Al arrancar, Spring inicializa automáticamente 5 bicicletas de prueba desde `data.sql`.

### Paso 5 — Detener la aplicación

`Ctrl + C` en la terminal donde está corriendo.

---

## 3. Documentación interactiva (Swagger)

Una vez levantada la aplicación, abre en el navegador:

**http://localhost:8080/swagger-ui.html**

**https://pruebatecnica-ddc6bzeudpdmdfdv.eastus-01.azurewebsites.net/swagger-ui/index.html#/**

Desde Swagger UI puedes:
- Ver todos los endpoints con sus esquemas (request/response)
- Probar peticiones directamente con "Try it out"
- Ver los códigos HTTP esperados (200, 201, 400, 404, 409)
- Inspeccionar los DTOs y sus validaciones

También está disponible el JSON OpenAPI crudo en:
```
http://localhost:8080/v3/api-docs
```

---

## 4. Endpoints disponibles

### Bicicletas — `/api/bicicletas`

| Método | URL | Descripción | Códigos |
|--------|-----|-------------|---------|
| `POST` | `/api/bicicletas` | Registra una bicicleta | 201, 400 |
| `GET` | `/api/bicicletas` | Lista todas las bicicletas | 200 |
| `GET` | `/api/bicicletas/{codigo}` | Obtiene bicicleta por código | 200, 404 |
| `GET` | `/api/bicicletas/disponibles?tipo=URBANA` | Lista disponibles (filtro de tipo opcional) | 200 |

### Alquileres — `/api/alquileres`

| Método | URL | Descripción | Códigos |
|--------|-----|-------------|---------|
| `POST` | `/api/alquileres/iniciar` | Inicia un alquiler | 201, 400, 404 |
| `POST` | `/api/alquileres/{id}/finalizar` | Finaliza un alquiler | 200, 404, 409 |
| `GET` | `/api/alquileres/{id}` | Detalle de un alquiler | 200, 404 |
| `GET` | `/api/alquileres/bicicleta/{codigo}` | Historial de una bicicleta | 200, 404 |

---

## 5. Reglas de negocio

### 5.1 Tarifas por tipo de bicicleta

| Tipo | Tarifa/hora | Multa/hora de retraso |
|---|---|---|
| `URBANA` | $3.500 | $1.750 |
| `MONTAÑA` | $5.000 | $2.500 |
| `ELÉCTRICA` | $7.500 | $3.750 |

### 5.2 Cálculo del costo base

- Se calcula sobre el **tiempo real de uso** (desde `horaInicio` hasta `horaFinReal`).
- Se **redondea al alza** a la hora completa.
- Fórmula: `costoBase = ceil(duracionHoras) × tarifaPorHora`

| Tiempo real | Horas cobradas |
|---|---|
| 1h 10min | 2 horas |
| 2h exactas | 2 horas |
| 2h 01min | 3 horas |

### 5.3 Multa por devolución tardía

- Solo aplica si `horaFinReal > horaFinEstimada`.
- Las horas de retraso también se redondean al alza.
- `multa = ceil(horasRetraso) × (tarifaPorHora × 0.5)`

### 5.4 Ejemplo completo

Bicicleta MONTAÑA, estimada 2h, devuelta a 3h 20min después del inicio:

| Cálculo | Valor |
|---|---|
| Tiempo real (3h 20min → ceil) | 4 horas |
| Costo base (4h × $5.000) | $20.000 |
| Retraso (1h 20min → ceil) | 2 horas |
| Multa (2h × $2.500) | $5.000 |
| **Costo total** | **$25.000** |

### 5.5 Validaciones

- Una bicicleta **no DISPONIBLE** no puede alquilarse → `400 Bad Request`
- Un alquiler **inexistente** → `404 Not Found`
- Un alquiler **ya finalizado** → `409 Conflict`

---

## 6. Arquitectura

El proyecto sigue una **arquitectura en capas (Layered Architecture)** clásica de Spring Boot, con separación clara de responsabilidades:

```
┌──────────────────────────────────────────────────────────────┐
│                       CLIENTE (Postman, Swagger UI)          │
└──────────────────────────────────────────────────────────────┘
                              │ HTTP/JSON
                              ▼
┌──────────────────────────────────────────────────────────────┐
│  CAPA DE CONTROLADORES (REST Controllers)                    │
│  BicicletaController · AlquilerController                    │
│  • Reciben peticiones HTTP                                   │
│  • Validan DTOs con @Valid                                   │
│  • Delegan al servicio                                       │
│  • Devuelven ResponseEntity con código HTTP                  │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│  CAPA DE SERVICIOS (Lógica de negocio)                       │
│  AlquilerService · BicicletaService · CostoCalculoService    │
│  • Implementan las reglas de negocio                         │
│  • Coordinan transacciones (@Transactional)                  │
│  • Lanzan excepciones de dominio                             │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────┐
│  CAPA DE REPOSITORIOS (Acceso a datos)                       │
│  BicicletaRepository · AlquilerRepository                    │
│  • Extienden JpaRepository                                   │
│  • Queries derivadas por nombre de método                    │
└──────────────────────────────────────────────────────────────┘
                              │ JPA/Hibernate
                              ▼
┌──────────────────────────────────────────────────────────────┐
│  CAPA DE PERSISTENCIA (MySQL 8)                              │
│  Tablas: bicicleta · alquiler                                │
└──────────────────────────────────────────────────────────────┘
```

### 6.1 Capa de controladores (`controller/`)

Los **controllers** son la puerta de entrada HTTP. Su responsabilidad es:
- Recibir y deserializar las peticiones JSON.
- Validar los DTOs con `@Valid`.
- Invocar al servicio correspondiente.
- Construir la respuesta HTTP con el código apropiado (200, 201, 400, 404, 409).

**No** contienen lógica de negocio. Las excepciones se gestionan globalmente en `GlobalExceptionHandler`, lo que mantiene los controllers limpios.

### 6.2 Capa de servicios (`service/`)

Aquí vive toda la **lógica de negocio**. Hay tres servicios:

- **`BicicletaService`** — CRUD de bicicletas, cambios de estado, conversión Entity ↔ DTO.
- **`AlquilerService`** — Orquesta el ciclo de vida del alquiler: validaciones, cambio de estado de la bicicleta y persistencia del alquiler.
- **`CostoCalculoService`** — Servicio puro (sin estado ni dependencias) que encapsula las fórmulas de costo base, multa y total. Esto facilita los tests unitarios y aísla la lógica matemática.

Todos los servicios están anotados con `@Transactional` para garantizar atomicidad: si algo falla a mitad de una operación (ej. fallo al guardar el alquiler), el cambio de estado de la bicicleta se revierte.

### 6.3 Capa de repositorios (`repository/`)

Interfaces que extienden `JpaRepository<T, ID>`. Spring Data JPA genera las implementaciones automáticamente:

```java
Optional<Bicicleta> findByCodigo(String codigo);
List<Bicicleta> findByEstadoAndTipo(EstadoBicicleta estado, TipoBicicleta tipo);
```

Sin escribir SQL, Spring traduce el nombre del método a la query correspondiente.

### 6.4 Capa de entidades (`entity/`)

Mapeo objeto-relacional con **JPA/Hibernate**. Dos entidades:

- **`Bicicleta`** — Tiene `id`, `codigo`, `tipo`, `estado`, `fechaCreacion` y una colección de alquileres (`@OneToMany`).
- **`Alquiler`** — Referencia a una `Bicicleta` (`@ManyToOne`), cliente, horas estimadas/reales, y los campos calculados (`costoBase`, `multa`, `costoTotal`).

Se definieron **índices** sobre los campos más consultados (`codigo`, `estado`, `bicicleta_id`) para optimizar las búsquedas.

### 6.5 DTOs (`dto/`)

Objetos de transferencia que separan la representación externa (JSON) de la entidad interna. Esto:
- Evita exponer detalles internos del modelo (ej. la relación bidireccional Bicicleta ↔ Alquiler).
- Permite validar input con `@NotBlank`, `@NotNull`, `@Min`.
- Da flexibilidad para evolucionar la API sin romper el modelo de datos.

Hay DTOs de **request** (`CrearBicicletaRequest`, `CrearAlquilerRequest`, `FinalizarAlquilerRequest`) y de **response** (`BicicletaDTO`, `AlquilerDTO`, `ErrorResponse`).

### 6.6 Manejo de errores (`exception/`)

Excepciones de dominio personalizadas:
- `BicicletaNotFoundException` → 404
- `BicicletaNoDisponibleException` → 400
- `AlquilerNotFoundException` → 404
- `AlquilerYaFinalizadoException` → 409

Todas se interceptan en `GlobalExceptionHandler` (`@RestControllerAdvice`), que devuelve un `ErrorResponse` consistente:

```json
{
  "status": 404,
  "message": "Bicicleta BIC-XYZ no encontrada",
  "timestamp": "2026-05-18T14:30:00"
}
```

### 6.7 Enums (`enums/`)

- `TipoBicicleta` — Lleva embebida la tarifa por hora (`tarifaPorHora`).
- `EstadoBicicleta` — `DISPONIBLE`, `ALQUILADA`, `EN_MANTENIMIENTO`.
- `EstadoAlquiler` — `EN_CURSO`, `FINALIZADO`.

Embeber la tarifa en el enum mantiene el dominio cohesionado: añadir un nuevo tipo de bicicleta solo requiere una línea.

### 6.8 Flujo de un alquiler completo

```
1. POST /api/bicicletas
   └── BicicletaController → BicicletaService → BicicletaRepository → MySQL
                                                     [bicicleta DISPONIBLE]

2. POST /api/alquileres/iniciar
   └── AlquilerController → AlquilerService
        ├── valida bicicleta DISPONIBLE
        ├── cambia bicicleta a ALQUILADA
        └── crea Alquiler en estado EN_CURSO

3. POST /api/alquileres/{id}/finalizar
   └── AlquilerController → AlquilerService
        ├── valida alquiler EN_CURSO
        ├── CostoCalculoService calcula costoBase + multa + total
        ├── cambia bicicleta a DISPONIBLE
        └── marca alquiler como FINALIZADO
```

---

## 7. Estructura del proyecto

```
proyecto-alquiler-bicicletas/
├── pom.xml                        ← Dependencias Maven
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/turismo/alquiler/
    │   │   ├── AlquilerApiApplication.java      ← Clase main
    │   │   ├── config/
    │   │   │   └── AppConfig.java               ← Beans (ObjectMapper, OpenAPI)
    │   │   ├── controller/
    │   │   │   ├── BicicletaController.java
    │   │   │   └── AlquilerController.java
    │   │   ├── service/
    │   │   │   ├── BicicletaService.java
    │   │   │   ├── AlquilerService.java
    │   │   │   └── CostoCalculoService.java
    │   │   ├── repository/
    │   │   │   ├── BicicletaRepository.java
    │   │   │   └── AlquilerRepository.java
    │   │   ├── entity/
    │   │   │   ├── Bicicleta.java
    │   │   │   └── Alquiler.java
    │   │   ├── dto/
    │   │   │   ├── BicicletaDTO.java
    │   │   │   ├── AlquilerDTO.java
    │   │   │   ├── CrearBicicletaRequest.java
    │   │   │   ├── CrearAlquilerRequest.java
    │   │   │   ├── FinalizarAlquilerRequest.java
    │   │   │   └── ErrorResponse.java
    │   │   ├── exception/
    │   │   │   ├── BicicletaNotFoundException.java
    │   │   │   ├── BicicletaNoDisponibleException.java
    │   │   │   ├── AlquilerNotFoundException.java
    │   │   │   ├── AlquilerYaFinalizadoException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   └── enums/
    │   │       ├── TipoBicicleta.java
    │   │       ├── EstadoBicicleta.java
    │   │       └── EstadoAlquiler.java
    │   └── resources/
    │       ├── application.properties          ← Config MySQL + JPA
    │       └── data.sql                        ← Bicicletas iniciales
    └── test/
        ├── java/com/turismo/alquiler/
        │   ├── service/
        │   │   ├── BicicletaServiceTest.java
        │   │   ├── AlquilerServiceTest.java
        │   │   └── CostoCalculoServiceTest.java
        │   └── controller/
        │       └── AlquilerIntegrationTest.java
        └── resources/
            └── application-test.properties     ← Config H2 en memoria
```

---

## 8. Tests

El proyecto incluye **26 tests** divididos en:

- **Tests unitarios de cálculo** (`CostoCalculoServiceTest`): validan las fórmulas de costo base, multa y total para los casos límite del redondeo al alza.
- **Tests de servicios con BD** (`BicicletaServiceTest`, `AlquilerServiceTest`): verifican la lógica de negocio contra una base de datos H2 en memoria.
- **Tests de integración** (`AlquilerIntegrationTest`): ejercitan los controllers vía `MockMvc` simulando peticiones HTTP completas.

### Ejecutar todos los tests

```bash
mvn test
```

> Los tests **no requieren MySQL**: usan H2 en memoria activado por el perfil `test`.

### Salida esperada
```
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 9. Ejemplos de uso

### 9.1 Registrar una bicicleta

```http
POST /api/bicicletas
Content-Type: application/json

{
  "codigo": "BIC-010",
  "tipo": "ELÉCTRICA"
}
```

Respuesta `201 Created`:
```json
{
  "id": 6,
  "codigo": "BIC-010",
  "tipo": "ELÉCTRICA",
  "estado": "DISPONIBLE",
  "fechaCreacion": "2026-05-18T14:30:00"
}
```

### 9.2 Iniciar un alquiler

```http
POST /api/alquileres/iniciar
Content-Type: application/json

{
  "codigoBicicleta": "BIC-001",
  "cliente": "Juan Pérez",
  "horaInicio": "2026-05-18T10:00:00",
  "duracionEstimadaHoras": 2
}
```

Respuesta `201 Created`:
```json
{
  "id": 1,
  "codigoBicicleta": "BIC-001",
  "cliente": "Juan Pérez",
  "horaInicio": "2026-05-18T10:00:00",
  "horaFinEstimada": "2026-05-18T12:00:00",
  "horaFinReal": null,
  "duracionRealHoras": 0,
  "costoBase": null,
  "multa": null,
  "costoTotal": null,
  "estado": "EN_CURSO",
  "fechaCreacion": "2026-05-18T10:00:01"
}
```

### 9.3 Finalizar un alquiler (con multa)

```http
POST /api/alquileres/1/finalizar
Content-Type: application/json

{
  "horaFinReal": "2026-05-18T13:20:00"
}
```

Respuesta `200 OK`:
```json
{
  "id": 1,
  "codigoBicicleta": "BIC-001",
  "cliente": "Juan Pérez",
  "horaInicio": "2026-05-18T10:00:00",
  "horaFinEstimada": "2026-05-18T12:00:00",
  "horaFinReal": "2026-05-18T13:20:00",
  "duracionRealHoras": 3.33,
  "costoBase": 14000,
  "multa": 3500,
  "costoTotal": 17500,
  "estado": "FINALIZADO",
  "fechaCreacion": "2026-05-18T10:00:01"
}
```

### 9.4 Consultar disponibles filtrando por tipo

```http
GET /api/bicicletas/disponibles?tipo=URBANA
```

### 9.5 Historial de una bicicleta

```http
GET /api/alquileres/bicicleta/BIC-001
```

### 9.6 Error: bicicleta no disponible

```http
POST /api/alquileres/iniciar
{
  "codigoBicicleta": "BIC-004",
  "cliente": "Ana",
  "horaInicio": "2026-05-18T15:00:00",
  "duracionEstimadaHoras": 1
}
```

Respuesta `400 Bad Request`:
```json
{
  "status": 400,
  "message": "La bicicleta BIC-004 no está disponible",
  "timestamp": "2026-05-18T15:00:00"
}
```

---

## 10. Criterios de evaluación: cómo se abordaron

Esta sección documenta de forma concreta cómo el proyecto cumple los criterios de evaluación: **calidad del código, principios SOLID, DRY y manejo de excepciones**.

### 10.1 Principios SOLID

#### S — Single Responsibility Principle (Responsabilidad única)

Cada clase tiene **una sola razón para cambiar**:

| Clase | Responsabilidad única |
|---|---|
| `BicicletaController` / `AlquilerController` | Recibir HTTP, validar, delegar. **No** contiene lógica de negocio. |
| `BicicletaService` | Gestionar bicicletas (registrar, consultar, cambiar estado). |
| `AlquilerService` | Orquestar el ciclo de vida de un alquiler. |
| `CostoCalculoService` | **Solo** calcular costos, multas y total. Sin dependencias. |
| `GlobalExceptionHandler` | **Solo** transformar excepciones a respuestas HTTP. |
| DTOs | **Solo** transportar datos entre capas. |
| Entidades | **Solo** representar el modelo persistente. |

> Esta separación es la que permite que `CostoCalculoService` se pueda testear de forma aislada sin necesidad de levantar la base de datos.

#### O — Open/Closed Principle (Abierto/cerrado)

El código está **abierto a extensión y cerrado a modificación**:

- **Agregar un nuevo tipo de bicicleta** (ej. `PLEGABLE`) solo requiere añadir una línea al enum `TipoBicicleta` con su tarifa. Ni el cálculo de costos ni los servicios cambian.
- **Agregar una nueva excepción de dominio** solo requiere crear la clase y un `@ExceptionHandler` en `GlobalExceptionHandler`. Los controllers existentes no se tocan.
- **Agregar un nuevo endpoint** no obliga a modificar los existentes.

#### L — Liskov Substitution Principle (Sustitución de Liskov)

Los repositorios extienden `JpaRepository<T, ID>` y pueden sustituirse sin afectar al consumidor. Esto se demuestra en los tests: la misma interfaz `BicicletaRepository` funciona contra MySQL (producción) y H2 (tests) sin cambios en `BicicletaService`.

#### I — Interface Segregation Principle (Segregación de interfaces)

Los repositorios exponen **solo** los métodos que se necesitan:

```java
public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    Optional<Bicicleta> findByCodigo(String codigo);
    List<Bicicleta> findByEstado(EstadoBicicleta estado);
    List<Bicicleta> findByEstadoAndTipo(EstadoBicicleta estado, TipoBicicleta tipo);
}
```

Nada de métodos genéricos sin uso que obliguen a los consumidores a depender de funcionalidad que no usan.

#### D — Dependency Inversion Principle (Inversión de dependencias)

Las clases dependen de **abstracciones**, no de implementaciones:

- `AlquilerService` depende de `BicicletaService`, `CostoCalculoService` y `AlquilerRepository` (interfaz) — Spring inyecta las implementaciones concretas con `@Autowired`.
- Los controllers dependen de servicios, no de repositorios directamente.
- En tests, podríamos mockear cualquier dependencia sin tocar el código.

```java
@Service
public class AlquilerService {
    @Autowired private AlquilerRepository alquilerRepository;     // interfaz
    @Autowired private BicicletaService bicicletaService;          // colaborador
    @Autowired private CostoCalculoService costoCalculoService;    // colaborador
}
```

---

### 10.2 DRY (Don't Repeat Yourself)

Se evita la duplicación en varios puntos clave:

| Patrón duplicable | Cómo se evita |
|---|---|
| Tarifa por tipo de bicicleta | Embebida en el enum `TipoBicicleta`, no esparcida en `if/switch` |
| Cálculo de costo | Centralizado en `CostoCalculoService` — un único punto de verdad |
| Mapeo Entity → DTO | Método único `convertirADTO()` en cada servicio |
| Manejo de errores | Centralizado en `GlobalExceptionHandler`, los controllers no repiten `try/catch` |
| Validación de input | Anotaciones `@NotBlank`, `@NotNull`, `@Min` en los DTOs; la validación la dispara `@Valid` |
| Conversión a `ResponseEntity` con código HTTP | Hecha automáticamente por el `RestControllerAdvice` |

**Ejemplo concreto:** Antes los controllers tenían bloques `try/catch` repetitivos. Al centralizar el manejo en `GlobalExceptionHandler`, los controllers quedan así:

```java
@PostMapping("/iniciar")
public ResponseEntity<AlquilerDTO> iniciar(@RequestBody @Valid CrearAlquilerRequest request) {
    AlquilerDTO alquiler = alquilerService.iniciarAlquiler(...);
    return ResponseEntity.status(HttpStatus.CREATED).body(alquiler);
}
```

Una sola línea de negocio, sin ruido.

---

### 10.3 Manejo de excepciones

El proyecto implementa una estrategia **centralizada, semántica y consistente**:

#### Excepciones de dominio personalizadas

Cada situación excepcional se modela como una clase específica que **expresa intención**:

| Excepción | Semántica | Código HTTP |
|---|---|---|
| `BicicletaNotFoundException` | La bicicleta consultada no existe | `404 Not Found` |
| `BicicletaNoDisponibleException` | La bicicleta existe pero no puede alquilarse | `400 Bad Request` |
| `AlquilerNotFoundException` | El alquiler consultado no existe | `404 Not Found` |
| `AlquilerYaFinalizadoException` | El alquiler ya fue cerrado | `409 Conflict` |

Esto sigue la regla: **usar excepciones para condiciones excepcionales, no para flujo de control**. El nombre de la excepción comunica el problema sin necesidad de leer el mensaje.

#### Manejador global con `@RestControllerAdvice`

`GlobalExceptionHandler` intercepta las excepciones y produce respuestas HTTP **uniformes**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BicicletaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBicicletaNotFound(BicicletaNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, e.getMessage()));
    }
    // ...más handlers...
}
```

Ventajas concretas:
- **Controllers limpios**: sin `try/catch` repetidos.
- **Respuesta consistente**: todos los errores devuelven el mismo formato `ErrorResponse` con `status`, `message` y `timestamp`.
- **Fácil extensión**: añadir un nuevo error es agregar un método con `@ExceptionHandler`.

#### Validación de input

Las validaciones de bean (`@NotBlank`, `@NotNull`, `@Min`) se aplican con `@Valid` en los controllers. Cuando fallan, `MethodArgumentNotValidException` es atrapada por `GlobalExceptionHandler` y devuelve un `400` con los mensajes acumulados:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
    String mensaje = e.getBindingResult().getAllErrors().stream()
        .map(error -> error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(400, "Error de validación: " + mensaje));
}
```

Esto evita que las validaciones se rompan en producción con un `500 Internal Server Error` poco descriptivo.

#### Transaccionalidad y rollback

Los servicios están anotados con `@Transactional`. Si una excepción se lanza a mitad de una operación (ej. al guardar el alquiler después de cambiar el estado de la bicicleta), Spring **revierte automáticamente** los cambios anteriores, garantizando consistencia del estado.

---

### 10.4 Calidad del código

| Aspecto | Implementación |
|---|---|
| **Nomenclatura** | Nombres en español alineados con el dominio (`iniciarAlquiler`, `calcularMulta`, `obtenerHistorial`). Convenciones Java estándar (camelCase, clases PascalCase). |
| **Tamaño de clases** | Ninguna clase supera las ~100 líneas. Cada una hace una sola cosa. |
| **Tamaño de métodos** | Métodos cortos y enfocados. El más largo (`finalizarAlquiler`) tiene 20 líneas legibles. |
| **Sin código muerto** | No hay imports sin usar, variables declaradas y no usadas, ni comentarios obsoletos. |
| **Lombok** | Reduce boilerplate de getters/setters/constructores manteniendo el código limpio. |
| **Inmutabilidad de cálculos** | `CostoCalculoService` no guarda estado; sus métodos son funciones puras testeables. |
| **Índices de BD** | Definidos en las entidades para columnas consultadas frecuentemente. |
| **Tests automatizados** | 26 tests cubriendo cálculo (unitarios), persistencia (servicios) e integración HTTP (MockMvc). |
| **Documentación API** | Swagger UI generada automáticamente desde el código — no se desincroniza. |

---

### 10.5 Resumen

| Criterio | Evidencia en el código |
|---|---|
| **SOLID** | Capas separadas, servicios cohesionados, dependencias inyectadas, enum extensible |
| **DRY** | Cálculo centralizado, manejo global de errores, mapping unificado, tarifa en el enum |
| **Manejo de excepciones** | 4 excepciones de dominio + `@RestControllerAdvice` + `ErrorResponse` consistente |
| **Calidad** | Tests pasando, código corto y legible, sin duplicación, validaciones declarativas |
