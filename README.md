# Banking Portal

A banking demo built by improving the older [Digital-Bank project](https://github.com/ahmedramibelghuith/Digital-Bank). It uses Angular 22 and Spring Boot with Java 21.

Customers can register, sign in, open current or savings accounts, and record deposits, withdrawals, and transfers. Account history and administrator lists are paginated. Deposits are simulated ledger credits, not payments from an external provider; interest and overdrafts are not implemented.

## Run locally

The commands below use Bash on Linux, macOS, or WSL. Install Node.js 26 and a Java 21 JDK. Set `JAVA_HOME` to the JDK installation if Java is not already available. Maven is supplied by the wrapper.

From the repository root:

```bash
node --version
java -version
npm ci
```

If you use nvm, `nvm install` and `nvm use` select the version in `.nvmrc`.

Start the API:

```bash
npm run backend
```

In another terminal, start the frontend:

```bash
npm start
```

Open [localhost:4200](http://localhost:4200). Registration creates a customer, then returns to sign-in. No default customer or administrator password is supplied.

The default H2 database is stored in `GestionBanque/data/`. Flyway runs versioned migrations and Hibernate validates the schema. For an existing database from Banking Portal 1.0, read the [migration instructions](docs/MIGRATION.md) before starting this version. The old Digital-Bank database is not imported automatically.

## Create an administrator

Set these variables in the backend terminal before starting it:

```bash
read -r -p 'Admin email: ' ADMIN_EMAIL
read -r -s -p 'Admin password: ' ADMIN_PASSWORD
export ADMIN_EMAIL ADMIN_PASSWORD
npm run backend
```

Use a separate email and a password of 12–64 characters, at most 72 UTF-8 bytes. Existing admin passwords are not reset on startup, and a customer is never silently promoted. Administrator sign-in opens `/admin`.

## MySQL

Create an empty `banking_portal` database. Export `DB_USER` and `DB_PASSWORD` for that database, then run:

```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_URL='jdbc:mysql://localhost:3306/banking_portal'
npm run backend
```

Migrations, persistence, rollback, and concurrent transactions are tested with MySQL as well as H2. Integration tests require a separate, disposable database: they delete test records between cases.

## Build and test

```bash
npm run lint
npm run format:check
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

Unit tests require a Linux Chrome/Chromium executable; set `CHROME_BIN` if needed. Environments without a working Chromium sandbox can select `--browsers=ChromeHeadlessNoSandbox` for tests.

Backend checks and tests:

```bash
cd GestionBanque
./mvnw -B -ntp test
```

From the repository root, run browser tests with both application ports free:

```bash
npx playwright install --with-deps chromium
npm run e2e
```

Playwright starts both servers with a disposable in-memory database and test-only credentials. The suite covers customer workflows, rejected operations, session handling, and seeded administrator records.

To package the frontend and backend in one executable JAR:

```bash
npm run package
cd GestionBanque
java -jar target/GestionBanque-1.0.0.jar
```

Open [localhost:8080](http://localhost:8080). Starting the JAR from `GestionBanque` keeps database storage at the same path used during development.

## API behavior

Money and identifiers are JSON strings, so cents and large IDs are preserved. Money requests require a UUID `Idempotency-Key` header. Reusing a key with the same customer and normalized payload returns the original result; changing the payload with that key returns HTTP 409. Failed transactions roll back the receipt as well as the ledger changes.

The dialog retains its key when retrying unchanged values after a lost response. Closing the dialog or starting a new operation creates a new key: check history before beginning a separate transaction after an uncertain result.

Paged endpoints accept `page` (zero-based) and `size` (1–100, default 25). Account totals cover all active accounts, not just the displayed page.

Login and registration share a per-IP limit of 20 requests per minute, configurable through `AUTH_REQUESTS_PER_MINUTE`. The limiter uses the direct remote address and does not trust arbitrary forwarded headers. Multi-instance deployments need a shared edge limiter. Use HTTPS and `COOKIE_SECURE=true` outside local development.

Migration history is in [MIGRATION.md](docs/MIGRATION.md).
