# Banking Portal

An Angular banking frontend prototype with sign-in, registration, and an account dashboard. Originally generated with Angular CLI 17.3.3, this project was migrated to Angular 22 while preserving its existing components, routes, and screens.

## Run locally

Requirements: WSL/Linux, nvm, and Node.js 26 LTS. The verified Node.js version, 26.13.0, is pinned in `.nvmrc`.

```bash
nvm install
nvm use
npm ci
npm start
```

Open [http://localhost:4200/login](http://localhost:4200/login). The existing routes are `/login`, `/register`, and `/home`; there is currently no default route for `/`.

The dashboard expects a separately running backend at `http://<hostname>:8080/api/clients/<email>`. No backend is included in this repository.

## Migrating the legacy project

The upgrade modernized the existing Angular 17 application rather than rebuilding it. All six components were retained, including the login, registration, and home components. The NgModule structure, screen layouts, and existing user flows were preserved.

### Migration process

1. Installed native Linux Node.js and npm in WSL through nvm, replacing reliance on the Windows npm executable.
2. Installed the original dependencies with `npm ci`.
3. Ran the official Angular CLI, Core, and Material migrations one major release at a time: **17 → 18 → 19 → 20 → 21 → 22**. Node.js 20 was used for the intermediate migrations; the final application uses Node.js 26.
4. Reviewed the generated changes and updated remaining deprecated API usage without replacing components.
5. Built the production application, ran the existing unit tests, and checked the three main screens in Chromium with a mocked account API.

The update command was repeated for each target major version:

```bash
# Replace <major> with each successive version: 18, 19, 20, 21, 22.
npx ng update @angular/core@<major> @angular/cli@<major> @angular/material@<major>
```

Angular's guidance recommends updating [one major version at a time](https://angular.dev/reference/releases#update-paths).

### What changed

| Area | Migration result |
| --- | --- |
| Angular framework | 17.3.x → 22.2.5 |
| Angular CLI, Material, and CDK | Upgraded to 22.2.6 |
| TypeScript | 5.4.x → 6.0.3 |
| Node.js | Pinned 26.13.0 in `.nvmrc`; declared Node.js 26 in `package.json` |
| Build tooling | Replaced `@angular-devkit/build-angular` with `@angular/build`, including the Karma builder |
| Bootstrap | Replaced deprecated `platformBrowserDynamic()` with `platformBrowser()` and explicitly retained Zone.js change detection |
| Component metadata | Added `standalone: false` and `ChangeDetectionStrategy.Eager` to retain the existing NgModule and change-detection behavior |
| HTTP configuration | Replaced `HttpClientModule` with `provideHttpClient(withXhr(), withInterceptorsFromDi())` |
| Account lookup | Replaced deprecated `toPromise()` with `firstValueFrom()` and typed the ID response |
| Templates | Converted the account menu's `*ngIf` to `@if` |
| Existing directive | Updated the `DOCUMENT` import and NgModule-compatible metadata |
| TypeScript configuration | Applied the CLI's bundler module-resolution and compiler migrations |
| Tests | Updated routing and HTTP test providers, mocked the account request, and replaced the obsolete generated welcome-page assertion |
| Dependencies | Refreshed the lockfile and added the instrumentation dependency required by the Angular 22 Karma builder |

## Build and verification

```bash
npm run build
npm test -- --watch=false --browsers=ChromeHeadless
```

The production output is written to `dist/banking-portal/browser/`.

Browser tests require Chrome or Chromium. If it is not detected automatically, point `CHROME_BIN` to the Linux browser executable:

```bash
CHROME_BIN=/path/to/chromium npm test -- --watch=false --browsers=ChromeHeadless
```

Verification completed after the migration:

- Production build passed, within the configured bundle budgets.
- All **8 existing unit tests passed** in Linux headless Chromium.
- Manual browser automation confirmed that login, registration, and the dashboard render, and that the account settings menu opens, without browser runtime errors. The dashboard API was mocked; this did not verify a real backend connection.
- The installed dependency tree passed `npm ls --depth=0`.

The browser smoke checks were run as migration verification; a persistent end-to-end test suite has not been added.

## Current scope

This remains a frontend prototype. Login currently navigates using the entered email rather than authenticating a password. Registration and transaction actions are not connected to a backend, and the displayed balance is static. The framework migration preserves these existing behaviors; it does not implement production authentication or banking transactions.
