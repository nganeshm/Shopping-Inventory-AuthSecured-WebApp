# Shopping-Inventory-Order-Management Enterprise Platform

A practical, runnable multi-module project demonstrating Spring Boot, Spring
Security + JWT authentication at every layer (gateway edge check +
independent per-service verification), Spring Cloud (Eureka + Gateway +
OpenFeign), Spring Data JPA with **PostgreSQL** and proper relational joins,
Resilience4j Circuit Breaker, Redis cache-aside, Spark CSV product import,
centralized exception handling, and a layered architecture that follows
**SOA** and **SOLID** principles throughout.

## Architecture

```
                          ┌────────────────────┐
                          │   Eureka Server     │  :8761  (service registry)
                          └─────────▲──────────┘
                                    │ register / discover
          ┌─────────────────────────┼─────────────────────────┐
          │                         │                          │
  ┌───────▼────────┐       ┌────────▼────────┐        ┌────────▼────────┐
  │  API Gateway    │──────▶│  auth-service   │        │ customer-service │
  │  :8080          │       │  :8081          │        │  :8083           │
  │ JWT check +     │       │ Register/Login  │        │ Customer,        │
  │ routing + CORS  │       │ OTP verify +    │        │ Address (1:N),   │
  │ + circuit       │       │ password reset  │        │ ContactDetail(1:1)│
  │ breaker         │       │ (authdb)        │◀─Feign─│ (customerdb)     │
  │                 │       └────────▲────────┘        └────────▲─────────┘
  │                 │                │ Feign                     │
  │                 │       ┌────────▼────────┐                 │
  │                 │       │ email-service   │                 │
  │                 │       │ :8084 SMTP/OTP  │                 │
  └───────┬─────────┘                                  └────────▲─────────┘
          │                                                     │ Feign +
  ┌───────▼─────────┐     optional cache                        │ CircuitBreaker
  │ product-service │──────── Redis :6379                       │
  │ :8082            │
  │ Category, Product,│     CSV import via local Spark 4.2
  │ Inventory (1:1),  │──── /opt/spark/bin/spark-submit
  │ Order/OrderItem   │
  │ (productdb)       │
  └───────────────────┘

  PostgreSQL (one server, one DATABASE PER SERVICE: authdb / productdb / customerdb)
```

### Services

- **eureka-server** — the registry every other service registers with
  (`@EnableEurekaServer`) and discovers each other through.
- **api-gateway** — the single public entry point on `:8080`. A reactive
  `GlobalFilter` checks JWTs before routing; `RouteLocator` declares one
  route per downstream service; product/order routes have a Resilience4j
  circuit breaker with a local fallback. CORS is configured for any browser
  origin (`allowedOriginPatterns: *`).
- **auth-service** — owns identity only: registration (pending until email
  OTP is verified), login, forgot/reset password, JWT issuance. Profile and
  address are staged in `pending_customer_profiles` and copied into
  customer-service after OTP verify. Database: `authdb`.
- **email-service** — owns outbound mail only (`:8084`). auth-service sends
  OTPs through Feign + an internal key. Not exposed on the public gateway.
  Local SMTP is Mailhog (`localhost:1025`, UI `:8025`).
- **product-service** — owns the catalog + ordering domain: **Category**
  (self-referencing tree) → **Product** → **Inventory** (1:1) and
  **Order** → **OrderItem** (1:N). Independently validates JWTs (shared
  secret, no callback to auth-service). Database: `productdb`.
- **customer-service** — owns the customer profile domain: **Customer** →
  **Address** (1:N) and **Customer** → **ContactDetail** (1:1).
  Database: `customerdb`.
- **product-import-job** — thin Spark 4.2 job. `product-service` submits it
  with the local `/opt/spark/bin/spark-submit`; Spark is not loaded into
  the Spring JVM.

Each Spring Boot module has its own `Dockerfile`. Images are built from the
repository root so the Maven reactor can resolve the parent POM.

### Why three separate databases? ("Database per Service")

Each service is the **only** thing that can write to its own database.
`product-service` never joins directly against `customerdb` — the *only*
way it learns about a customer is through customer-service's HTTP API
(a Feign client, wrapped in a circuit breaker). This is deliberate: it is
what lets each service be deployed, scaled, and changed independently
without a schema migration in one breaking another — the core promise of
SOA/microservices. See `docker-compose.yml` + `docker/postgres-init/` for
how the three databases are created automatically.

## Recent features

### Paginated product catalog

`GET /api/products` returns 20 active products per page.

```http
GET /api/products?page=0
GET /api/products?page=1&size=20
```

Response shape:

```json
{
  "content": [ { "id": 1, "sku": "SKU-0001", "name": "..." } ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false
}
```

`size` defaults to 20 and is capped at 20. Pagination uses an id page
query first, then a join-fetch of category + inventory for those ids
(Hibernate cannot combine `JOIN FETCH` with `firstResult`/`maxResults`).

### Redis cache (optional)

product-service caches catalog DTOs in Redis (`localhost:6379`), never JPA
entities. Redis is an accelerator, not a dependency:

- The process starts if Redis is down (`min-idle: 0`).
- A connection failure is treated as a cache miss; PostgreSQL still answers.
- Product/category/import/inventory writes evict the affected keys.

Start Redis with the rest of local infra:

```bash
docker compose up -d redis
```

Override host with `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`.

### Email verification and forgot password

Registration does not issue a JWT. The user is stored with `enabled=false`
until the OTP on their email is confirmed. `username`, `email`, and `password`
are required. Name, address, and contact are optional — when sent they are
written to `customerdb` after OTP; when omitted, a starter customer row is
still created so unverified emails never become customers.

```bash
# 1. Start onboarding — OTP is emailed (and logged if SMTP is down)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "username":"ganesh","password":"secret123","email":"ganesh@example.com",
        "firstName":"Ganesh","lastName":"Narangle",
        "address":{"addressType":"HOME","line1":"221B Baker Street","city":"Pune",
                   "state":"Maharashtra","postalCode":"411001","country":"India"},
        "contact":{"primaryPhone":"+91-9876543210","preferredContactMethod":"EMAIL"}
      }'

# 2. Confirm the OTP (Mailhog UI: http://localhost:8025)
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email":"ganesh@example.com","otp":"123456"}'

# 3. Login now works
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ganesh","password":"secret123"}'

# Forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"ganesh@example.com"}'

curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"email":"ganesh@example.com","otp":"123456","newPassword":"newpass123"}'

# Resend (purpose: REGISTRATION or PASSWORD_RESET)
curl -X POST http://localhost:8080/api/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"ganesh@example.com","purpose":"REGISTRATION"}'
```

OTPs expire in 10 minutes and are stored as BCrypt hashes. Forgot-password
does not say whether the email exists. After a successful verify, auth-service
POSTs the staged profile to `customer-service` (`pending_customer_profiles`
is then deleted) and email-service sends a congratulations onboarding mail
from `registration-success.html`.

### CSV product import (Spark)

`POST /api/products/import` (ADMIN) accepts a multipart CSV named `file`.
product-service stores the file and runs the local Spark job. Valid rows
are written as Product + Inventory. Any row error rejects the whole file.

Header:

```text
sku,name,description,price,categoryName,initialQuantity,warehouseLocation
```

Sample file: `product-service/sample/products-100.csv`.
Categories **Electronics, Laptops, Accessories, Audio, Storage** are seeded
on product-service startup. Spark does not need a standalone cluster —
`spark-submit --master local[*]` starts Spark for that request only.

```bash
curl -X POST http://localhost:8080/api/products/import \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@product-service/sample/products-100.csv"
```

### CORS

The React app on any origin can call the gateway (`:8080`) and auth-service
(`:8081`). Gateway preflight `OPTIONS` skips the JWT filter. auth-service
uses `allowedOriginPatterns: *` with credentials.

## Entity relationships (the "proper joins" you asked for)

**product-service (productdb)**
```
Category (self-referencing tree)
   │ 1
   │
   │ N
Product ──────────────── 1:1 ──────────────── Inventory
   │ 1                                     (quantityAvailable,
   │                                        quantityReserved,
   │ N                                      reorderLevel)
OrderItem ──── N:1 ──── Order
                           (customerId: Long  ← soft cross-service reference,
                            NOT a foreign key — see below)
```
- `Category` ↔ `Category`: self-referencing `@ManyToOne`/`@OneToMany` for
  parent/sub-category trees.
- `Product` → `Category`: `@ManyToOne`.
- `Product` ↔ `Inventory`: `@OneToOne`, Inventory is the split-off,
  high-write half of a product.
- `Order` → `OrderItem`: `@OneToMany(cascade=ALL, orphanRemoval=true)`.
- `OrderItem` → `Product`: `@ManyToOne`, with `unitPriceAtPurchase`
  snapshotted at order time.
- `Order.customerId`: a **plain Long**, not a JPA relationship. Shipping
  address is snapshotted onto the Order at purchase time.

**customer-service (customerdb)**
```
Customer ──── 1:N ──── Address (HOME / WORK / BILLING / SHIPPING, one marked default)
   │
   │ 1:1
ContactDetail (primaryPhone, secondaryPhone, preferredContactMethod)
```
- `Customer.email` is a loose correlation key back to auth-service's
  `User.email` — no cross-database foreign key.

## Where SOLID and SOA show up

| Principle | Where |
|---|---|
| **S**ingle Responsibility | Product vs Inventory, Customer vs ContactDetail; Controller → Service → Repository |
| **O**pen/Closed | `PricingStrategy` — add a discount class without editing `OrderServiceImpl` |
| **L**iskov Substitution | Any `PricingStrategy` can replace another |
| **I**nterface Segregation | Narrow `CategoryService`, `ProductService`, `InventoryService`, `OrderService` |
| **D**ependency Inversion | Services depend on interfaces; Spark extract and Redis sit behind abstractions |
| SOA: service autonomy | Database-per-service; no shared tables |
| SOA: explicit contracts | Each service owns its own DTO copy of a remote contract |
| SOA: resilience | Feign circuit breaker + gateway route breaker + fail-open Redis cache |

## Concepts → where to look

| Concept | File |
|---|---|
| `@SpringBootApplication`, `@Bean`, `@Configuration` | every `*Application.java`, `*Config.java` |
| `@EnableEurekaServer` / `@EnableDiscoveryClient` | `EurekaServerApplication`, every service's main class |
| Spring Cloud Gateway routes + reactive circuit breaker | `api-gateway/.../config/GatewayRouteConfig.java` |
| Reactive JWT `GlobalFilter` + CORS OPTIONS bypass | `api-gateway/.../filter/JwtAuthenticationFilter.java` |
| JWT generation & validation | `auth-service/.../security/JwtUtil.java` |
| Servlet JWT filter | `JwtAuthFilter.java` (auth), `JwtValidationFilter.java` (product, customer) |
| `@PreAuthorize` | `UserController`, `ProductController`, `CategoryController` |
| Paginated product list | `ProductController.getAll`, `ProductRepository.findActiveIds` |
| Redis cache-aside + fail-open | `product-service/.../config/RedisCacheConfig.java` |
| Spark CSV import | `ProductImportController`, `product-import-job/.../ProductCsvJob.java` |
| `@FeignClient` | `product-service/.../client/CustomerServiceClient.java`, `auth-service/.../client/EmailServiceClient.java`, `auth-service/.../client/CustomerServiceClient.java` |
| Email OTP (register + reset) | `email-service`, `auth-service/.../service/OtpService.java` |
| Registration success email | `email-service/.../templates/registration-success.html` |
| `@CircuitBreaker` + `@Retry` | `product-service/.../service/CustomerLookupService.java` |
| Strategy pattern (OCP) | `product-service/.../strategy/PricingStrategy.java` |
| Global exception handling | `exception/GlobalExceptionHandler.java` in every service |
| Dockerfile per service | `<service>/Dockerfile` (build context = repo root) |

## Running it locally

Requires JDK 17+, Maven, and Docker (for Postgres and Redis). Local Spark
4.2 at `/opt/spark` is required only for CSV import.

```bash
# 1. start PostgreSQL + Redis + Mailhog
docker compose up -d

# 2. build everything
mvn clean install -DskipTests

# 3. start in this order (separate terminals)
cd eureka-server    && mvn spring-boot:run
cd email-service    && mvn spring-boot:run
cd auth-service     && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd product-service  && mvn spring-boot:run
cd api-gateway      && mvn spring-boot:run
```

Check the registry at http://localhost:8761 — you should see AUTH-SERVICE,
EMAIL-SERVICE, CUSTOMER-SERVICE, PRODUCT-SERVICE, and API-GATEWAY.

### Docker images

Each service is a multi-stage image (Maven build, then JRE 17). Build from
the **repository root**:

```bash
docker build -f eureka-server/Dockerfile    -t secureauth/eureka-server .
docker build -f api-gateway/Dockerfile      -t secureauth/api-gateway .
docker build -f auth-service/Dockerfile     -t secureauth/auth-service .
docker build -f email-service/Dockerfile    -t secureauth/email-service .
docker build -f product-service/Dockerfile  -t secureauth/product-service .
docker build -f customer-service/Dockerfile -t secureauth/customer-service .
```

When running the images, point them at Docker-network hosts, for example:

```bash
-e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/productdb
-e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
-e REDIS_HOST=redis
```

`docker-compose.yml` currently starts **infra only** (Postgres + Redis +
Mailhog) so you can keep iterating with `mvn spring-boot:run`.

### Try it end-to-end (through the gateway on :8080)

```bash
# 1. Register (identity + profile/address), then verify the OTP from Mailhog
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "username":"ganesh","password":"secret123","email":"ganesh@example.com",
        "firstName":"Ganesh","lastName":"Narangle",
        "address":{"addressType":"HOME","line1":"221B Baker Street","city":"Pune",
                   "state":"Maharashtra","postalCode":"411001","country":"India"},
        "contact":{"primaryPhone":"+91-9876543210","preferredContactMethod":"EMAIL"}
      }'

curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email":"ganesh@example.com","otp":"paste-otp"}'

# 2. Login -> JWT  (customer row is already in customerdb after verify)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ganesh","password":"secret123"}'
TOKEN="paste-the-token-here"

# 2b. Signed-in profile (identity + addresses + contact)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/customers/me"

# 2c. All customer profiles (admin-style list)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/customers/details"
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/customers/details/1"

# 3. Browse the catalog (public GET, no token)
curl "http://localhost:8080/api/categories"
curl "http://localhost:8080/api/products?page=0"
curl "http://localhost:8080/api/products?page=1"

# 4. Create a category + product (ADMIN)
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Devices and gadgets"}'

curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"sku":"KB-001","name":"Mechanical Keyboard","price":79.99,"categoryId":1,
       "initialQuantity":50,"warehouseLocation":"PUNE-WH-1"}'

# 5. Import 100 products from CSV (ADMIN; local Spark required)
curl -X POST http://localhost:8080/api/products/import \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@product-service/sample/products-100.csv"

# 6. Place an order — product-service calls customer-service (Feign + breaker)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"customerId": 1, "items":[{"productId":1,"quantity":2}]}'
```

### Seeing the circuit breaker trip

Stop `customer-service` and repeat the order call a few times —
after the configured failure threshold
(`resilience4j.circuitbreaker.instances.customerServiceCB` in
`product-service/application.yml`), the breaker OPENS and
`CustomerLookupService`'s fallback kicks in. Watch it at
`http://localhost:8082/actuator/health`.

## What's intentionally simplified (and what you'd add for real production)

- `ddl-auto: update` is dev-only; production would use Flyway/Liquibase
  migrations and `ddl-auto: validate`.
- The JWT secret is a plaintext demo value; production would pull it from a
  secrets manager and rotate keys.
- Redis has no AUTH in local Compose; production would require a password
  and a dedicated cache per environment.
- Spark CSV import assumes `/opt/spark` on the host. The product-service
  Docker image does not bundle Spark.
- No distributed tracing or centralized logging yet.
- No Spring Cloud Config — each `application.yml` is local.
