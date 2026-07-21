# Catalog Service

Microservicio de catálogo extraído del monolito JPetStore 6 usando el patrón Strangler Fig.

## Requisitos

- Java 21
- Maven 3.9+
- Docker y Docker Compose (para pruebas de integración y despliegue)

## Compilación

```bash
mvn clean package -DskipTests
```

## Pruebas

Requiere Docker en ejecución (Testcontainers levanta PostgreSQL automáticamente):

```bash
mvn test
```

## Ejecución local

Requiere PostgreSQL corriendo en `localhost:5432` con base de datos `catalogdb`, usuario `catalog`, contraseña `catalog`.

```bash
mvn spring-boot:run
```

Con variables de entorno personalizadas:

```bash
DB_HOST=myhost DB_PORT=5432 DB_NAME=catalogdb DB_USER=catalog DB_PASSWORD=secret mvn spring-boot:run
```

## Ejecución con Docker Compose

```bash
docker compose up --build
```

El servicio queda disponible en http://localhost:8081

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/categories` | Lista todas las categorías |
| GET | `/api/categories/{categoryId}` | Consulta una categoría por ID |
| GET | `/api/categories/{categoryId}/products` | Lista productos de una categoría |
| GET | `/api/products/{productId}` | Consulta un producto por ID |
| GET | `/api/products/{productId}/items` | Lista ítems de un producto |
| GET | `/api/products/search?keyword={keyword}` | Busca productos por palabra clave |
| GET | `/api/items/{itemId}` | Consulta un ítem por ID |
| GET | `/actuator/health` | Health check |

## Ejemplos curl

```bash
# Listar categorías
curl http://localhost:8081/api/categories

# Consultar categoría
curl http://localhost:8081/api/categories/FISH

# Productos de una categoría
curl http://localhost:8081/api/categories/FISH/products

# Consultar producto
curl http://localhost:8081/api/products/FI-SW-01

# Ítems de un producto
curl http://localhost:8081/api/products/FI-SW-01/items

# Buscar productos
curl "http://localhost:8081/api/products/search?keyword=fish"
curl "http://localhost:8081/api/products/search?keyword=golden+retriever"

# Consultar ítem
curl http://localhost:8081/api/items/EST-1

# Health check
curl http://localhost:8081/actuator/health
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `catalogdb` | Nombre de la base de datos |
| `DB_USER` | `catalog` | Usuario |
| `DB_PASSWORD` | `catalog` | Contraseña (no usar default en producción) |

## Estructura del proyecto

```
catalog-service/
├── src/main/java/org/mybatis/jpetstore/catalog/
│   ├── CatalogServiceApplication.java
│   ├── controller/        # CategoryController, ProductController, ItemController
│   ├── service/           # CatalogService
│   ├── mapper/            # Interfaces MyBatis
│   ├── domain/            # Category, Product, Item
│   ├── exception/         # GlobalExceptionHandler, ResourceNotFoundException
│   ├── dto/               # (reservado para iteraciones futuras)
│   └── config/            # (reservado para iteraciones futuras)
├── src/main/resources/
│   ├── mapper/            # XMLs MyBatis (CategoryMapper, ProductMapper, ItemMapper)
│   ├── db/migration/      # V1__schema.sql, V2__data.sql (Flyway)
│   └── application.yml
├── src/test/              # Pruebas de integración con Testcontainers
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Decisiones arquitectónicas

- **Proyecto hermano independiente**: cero acoplamiento con el monolito. No importa ninguna clase de JPetStore.
- **Spring Boot 3 + Java 21**: Jakarta EE 10, sin deuda técnica de `javax.servlet`.
- **MyBatis con XML**: reutiliza el mismo estilo de mappers del monolito para facilitar la migración incremental.
- **Flyway**: gestión de migraciones reproducibles. V1 crea el schema, V2 carga datos iniciales equivalentes a HSQLDB.
- **Optional en servicio**: los métodos de búsqueda por ID retornan `Optional<T>` para forzar manejo explícito de ausencia en los controllers.
- **Entidades expuestas directamente**: en esta fase no se usan DTOs. Los campos de dominio son equivalentes a los del monolito. Se evaluará introducir DTOs si se requiere versionar la API o desacoplar el schema de la respuesta.
- **ItemMapper solo lectura**: `updateInventoryQuantity` no existe en este servicio. Las escrituras de inventario permanecen en el monolito hasta que se extraiga el servicio de órdenes.
- **Búsqueda multi-keyword**: `CatalogService.searchProductList` divide por espacios y hace una query por keyword, acumulando resultados con `distinct()`. Equivalente al comportamiento del monolito.
- **Testcontainers**: las pruebas de integración usan PostgreSQL real, no H2 ni mocks de base de datos.
- **Sin sesión HTTP**: el servicio es completamente stateless. No comparte estado con el monolito.

## Formato de error

Todos los errores retornan JSON uniforme:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found: UNKNOWN"
}
```

## Limitaciones conocidas

- No hay autenticación ni autorización.
- No hay paginación (se evaluará al integrar un gateway o al crecer el dataset).
- `dto/` y `config/` están reservados para iteraciones futuras.
- El `Dockerfile` instala Maven vía `apk` en la imagen de build; en un pipeline CI se reemplazaría por una imagen con Maven preinstalado o usando el wrapper.
