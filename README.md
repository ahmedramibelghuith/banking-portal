# Banking Portal

Banking Portal is an improved and modernized version of an older banking project, [Digital-Bank](https://github.com/ahmedramibelghuith/Digital-Bank). It builds on that original work by upgrading Angular, integrating the frontend and backend, completing existing features, and improving authentication, transaction handling, and testing.

Version **1.0.0** combines the upgraded **Banque frontend** with the banking features and Java backend from the original Digital-Bank project, retaining the existing Angular component names.

## What was combined

| Area           | Banque before integration    | Digital-Bank reference                        | Combined version                                                                  |
| -------------- | ---------------------------- | --------------------------------------------- | --------------------------------------------------------------------------------- |
| Frontend       | Angular 22, static dashboard | Angular 17, banking dialogs and admin screens | Angular 22.2.5, Material/CDK 22.2.6, TypeScript 6                                 |
| Login          | Email navigation             | Client email lookup                           | Password login with a server session                                              |
| Registration   | Form only                    | Form only                                     | Validated registration with hashed passwords                                      |
| Accounts       | Static balance               | Account creation, listing, deletion           | Current/savings accounts, live balances, closure with retained history            |
| Transactions   | Placeholder actions          | Deposit, withdrawal, transfer endpoints       | Validated operations, decimal money, atomic transfers and account locks           |
| Administration | Absent                       | Client/account/transaction lists              | Server-protected admin statistics and lists                                       |
| Backend        | Not included                 | Spring Boot 3.2.4, Java 17, MySQL             | Spring Boot 3.5.16, Java 21, H2 locally and optional MySQL                        |
| Verification   | Eight migration tests        | Generated test scaffolding                    | 15 frontend tests, nine backend integration tests, three full-stack browser tests |

The combined source is in this repository. [Digital-Bank](https://github.com/ahmedramibelghuith/Digital-Bank) is the older original project, retained as a historical reference. Banking Portal continues and improves that work; the two repositories do not synchronize automatically. Existing database records are **not** copied or migrated by this integration.

## Run in WSL

Requirements: **Node.js 26**, **Java 21**, and a Linux shell. Maven is downloaded by the included Maven Wrapper; no global Maven installation is required.

```bash
nvm install
nvm use
npm ci
java -version
```

In the current WSL environment Java was installed under `$HOME/.local/share/jdk-21`. Open a new Bash terminal or load it in an existing one:

```bash
export JAVA_HOME="$HOME/.local/share/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
```

On another machine, install a Java 21 JDK and set `JAVA_HOME` to that installation.

Start the backend in the first terminal:

```bash
npm run backend
```

Start Angular in a second terminal:

```bash
nvm use
npm start
```

Open [http://localhost:4200](http://localhost:4200). Register a customer, open an account, and use the deposit, withdrawal, transfer, and history actions. Amounts are displayed in USD; this version has one currency.

The Angular development proxy forwards `/api` requests to `127.0.0.1:8080`. The browser uses relative URLs, with no hardcoded client email or API host in application components.

### Local data

The default database is a persistent H2 file under `GestionBanque/data/` when using `npm run backend`. No customer or administrator is created automatically. Data and build artifacts are ignored by Git. Back up the database with the application stopped before removing or replacing it.

Deposits in this app are local ledger entries; there is no external payment-provider connection.

### Administrator account

Set `ADMIN_EMAIL` and `ADMIN_PASSWORD` in the **backend terminal** before its first startup to provision an administrator. Use a separate email and a password of 12–64 characters (at most 72 UTF-8 bytes). For example, enter them interactively without putting the password in shell history:

```bash
read -r -p 'Admin email: ' ADMIN_EMAIL
read -r -s -p 'Admin password: ' ADMIN_PASSWORD
export ADMIN_EMAIL ADMIN_PASSWORD
npm run backend
```

Sign in with that account to open `/admin`. Existing admin passwords are not reset on startup. If the email already belongs to a regular customer, startup fails instead of silently promoting the customer.

### Optional MySQL

Create a new empty database, set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the backend environment, then enable the profile:

```bash
export SPRING_PROFILES_ACTIVE=mysql
# Set DB_URL=jdbc:mysql://localhost:3306/banking_portal
# Set DB_USER and DB_PASSWORD for your database.
npm run backend
```

The MySQL profile is configured but has not been tested against a live MySQL server. Use a new schema: the decimal balances, credentials, and ledger relationships differ from the old Digital-Bank schema. Importing historical records needs a separate, backed-up data migration.

## Build a single application

```bash
npm run package
```

This builds Angular, runs the backend tests, and embeds the frontend in `GestionBanque/target/GestionBanque-1.0.0.jar`.

```bash
cd GestionBanque
java -jar target/GestionBanque-1.0.0.jar
```

Open [http://localhost:8080](http://localhost:8080). The packaged frontend and API share one origin; the Angular development server is unnecessary in this mode. Starting from the backend folder also keeps database storage at the same location as development.

## Tests

```bash
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
cd GestionBanque
./mvnw -B -ntp test
```

The frontend tests require Chrome/Chromium. Set `CHROME_BIN` to the Linux browser executable if it is not detected automatically.

Full-stack browser tests:

```bash
npx playwright install --with-deps chromium
npm run e2e
```

Run these commands from the repository root. Keep ports **4200** and **8080** free. Playwright starts and stops both servers, uses its own in-memory H2 database and test-only admin credentials, and does not reuse a running app or its customer data. The browser tests cover registration, exact balances, transfers, insufficient funds, account history, closure, session persistence, logout, login failure, and admin restrictions.

The GitHub Actions workflow runs builds, backend tests, frontend tests, and the full-stack suite. Formatting can be checked with `npm run format:check`.

## Migration history

The original Banque project was generated with Angular CLI 17.3.3. The official CLI, Core, and Material migrations were applied one major at a time: **17 → 18 → 19 → 20 → 21 → 22**, following [Angular's update guidance](https://angular.dev/reference/releases#update-paths). Node.js 20 was used for the intermediate migrations; the final runtime is pinned to Node.js 26.13.0 in `.nvmrc`.

The upgrade preserved NgModules and existing change-detection behavior, adopted `provideHttpClient`, `platformBrowser`, `firstValueFrom`, and `@if`, and replaced the deprecated build package with `@angular/build`.

The subsequent Digital-Bank integration retained its seven additional components: `AdminComponent`, `ListComponent`, `ListcomptesComponent`, `ListtransactionsComponent`, `OperationComponent`, `WithdrawComponent`, and `TransferComponent`. The existing login, registration, and home components now use typed reactive forms/services. Templates were cleaned up to remove nested HTML documents and runtime CDN scripts, with responsive local styles. Account, admin, and registration modules load on demand.

The backend preserves the original French domain names and transaction routes while replacing email-only access with Spring Security sessions and CSRF protection. Account access is checked server-side. Money uses `BigDecimal`; transfers lock accounts in ID order and create linked ledger entries in one transaction. Closing an account requires a zero balance and preserves its history. The unrelated `Scolarite` snapshot dependency and compiled `bin/` copies were not imported.

## Next enhancements

See [the enhancement roadmap](docs/ENHANCEMENTS.md) for priorities, effort, and acceptance criteria. Start with idempotent transaction requests, production database migrations and MySQL tests, then operational monitoring and authentication recovery features.
