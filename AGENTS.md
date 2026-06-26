# AGENTS.md

## Cursor Cloud specific instructions

This is a Java 17 / Spring Boot 3.2 + Spring Cloud 2023 multi-module Maven monorepo
(13 microservices) plus a React/Vite frontend (`frontend/`). Standard commands live in
`README.md`, `QUICK_START.md`, the root `pom.xml`, `manage-services.sh`, and
`frontend/package.json` — refer to those rather than duplicating them.

### Pre-installed in the VM snapshot (do NOT reinstall in the update script)
- JDK 17 is the **default** `java`/`javac` (set via `update-alternatives`). JDK 21 is also
  present; the project targets Java 17, so keep 17 as the default or the build/run may misbehave.
- Maven 3.8 (`mvn`), Docker engine + compose plugin. There is **no Maven wrapper** (`mvnw`)
  in the repo even though `recommendation-service/Dockerfile` references one — always use system `mvn`.

### Starting infrastructure (required before tests / running services)
The Docker daemon and containers are NOT running on a fresh pod — start them each session:
- Start the daemon (no systemd here): run `sudo dockerd` in a background tmux session.
- Bring up infra with `sudo docker compose up -d postgres mysql redis mongodb zookeeper kafka zipkin`.
  - PostgreSQL auto-creates the per-service databases via `init-scripts/01-init-databases.sql`.
  - Do **not** start the `mongo-express` service — it binds host port **8081**, which collides
    with `user-service`. The heavy `elasticsearch`/`kibana`/`pgadmin` services are optional.
  - MongoDB occasionally loses a startup race on "address already in use"; just
    `sudo docker compose up -d mongodb` again.

### Build / lint / test
- Build all: `mvn -B clean install -DskipTests` (from repo root). Frontend: `npm --prefix frontend run build`.
- Lint: `mvn -B checkstyle:check` (passes clean). Frontend `npm --prefix frontend run lint`
  currently reports pre-existing unused-var errors in app source — not an environment problem.
- Tests: `mvn -B test -Dspring.profiles.active=test`. **Gotcha:** several services use
  `spring.cloud.config.fail-fast: true` (or a hard `spring.config.import=configserver`), so their
  `contextLoads` tests need `eureka-server` (8761) + `config-server` (8888) **and** the infra
  databases running, otherwise they fail to load the context. (CI tolerates this with
  `continue-on-error`; locally, start eureka + config-server + infra first and they pass.)

### Running the platform (dev)
- Run order: `eureka-server` (8761) → `config-server` (8888) → `api-gateway` (8080) →
  business services. The gateway resolves routes via `lb://` from Eureka, so Eureka must be up
  for any gateway/frontend-driven flow.
- Minimal stack for the core register/browse flow: `eureka-server`, `config-server`,
  `api-gateway`, `user-service` (8081, PostgreSQL), `product-service` (8082, MySQL).
- Simplest way to run a built service: `java -jar <svc>/target/<svc>-1.0.0.jar` (after the Maven
  build). `manage-services.sh start <svc>` also works but its `JAVA_HOME` line targets macOS;
  on Linux it falls back to the ambient `JAVA_HOME`/default java.
- Frontend dev server: `npm --prefix frontend run dev` (Vite on 5173, proxies `/api` → gateway 8080).

### Known pre-existing frontend caveats (not environment issues; do not "fix" as setup)
- The installed Vite dev server returns **HTTP 403 for proxied POST/PUT/PATCH requests** (it
  rejects requests carrying a browser `Origin` header), so UI **write** actions fail in dev even
  though reads (GET) work. The request never reaches the gateway.
- The login page sends `{email, password}` but the backend `LoginRequestDto` expects
  `usernameOrEmail`, and the auth store reads `res.data.token` while the backend returns
  `accessToken` — so UI login cannot succeed without app code changes.
- Net effect: to exercise auth/write flows end-to-end, call the **API gateway directly**
  (e.g. `POST http://localhost:8080/api/auth/login` with `{"usernameOrEmail":...,"password":...}`,
  `POST /api/users/register`, `POST /api/categories`, `POST /api/products`). The frontend is
  reliable for browsing/read flows. Note `categories` is at `/api/categories`, not `/api/products/categories`.
