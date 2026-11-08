# Migration history and database adoption

The original Banque frontend was generated with Angular CLI 17.3.3. Angular CLI, Core, and Material migrations were applied one major at a time: 17 → 18 → 19 → 20 → 21 → 22. The Digital-Bank frontend features and Java backend were subsequently integrated into this repository. The original repository remains a separate reference; changes do not synchronize between the two.

The integration retained the banking components, added password sessions and CSRF protection, and replaced floating-point ledger calculations with `BigDecimal`. The approved cleanup removed unused generated components/assets, centralized money form handling, changed API money and IDs to strings, added idempotency receipts, bounded queries, throttling, and static analysis.

Current and savings account classes now represent account categories only. The unused Java interest/overdraft fields were removed. New databases do not contain those fields; an existing database's extra legacy columns are left intact rather than dropping stored values silently. No interest or overdraft policy is executed.

## API client changes

Money and identifiers in responses are strings. List responses now contain `content`, `page`, `size`, `totalElements`, and `last` rather than returning a bare array. Money POST requests require a UUID `Idempotency-Key` header. Update external clients together with the API; the Angular client in this repository already uses the new contract.

## Fresh databases

Flyway selects the H2 or MySQL migration directory from the database vendor. V1 creates the banking tables; V2 adds idempotency receipts and query indexes. Hibernate uses `ddl-auto=validate`, not automatic schema updates. Never change a migration that has already been applied; add a new version instead.

## Existing Banking Portal 1.0 databases

This procedure is for the previous combined Banking Portal schema, including `clients.password_hash` and the decimal account/operation tables. It is **not** a migration of the older Digital-Bank schema or its credentials.

1. Stop the application and back up the database. Preserve the original backup until the upgraded database has been verified.
2. Test a copy first. Check that its tables match V1, including the money types and foreign keys. Unknown or partially upgraded schemas must not be adopted blindly.
3. For the first startup of that reviewed copy, set `DB_BASELINE_ON_MIGRATE=true`. The baseline version is 0: Flyway records adoption, runs V1's non-destructive `CREATE TABLE IF NOT EXISTS` statements, then applies V2. Hibernate validates the result. Existing data is not cleared.
4. Remove `DB_BASELINE_ON_MIGRATE` after successful adoption. Check customer/account counts, balances, transaction history, and a backup/restore cycle before using the upgraded copy.

Without explicit adoption, Flyway refuses a nonempty database with no migration history. This is intentional. H2 adoption is tested with a pre-existing balance and legacy columns; the complete transaction suite also runs against a fresh MySQL schema.

## Rollback

Do not assume an older application can safely run against a newer schema. Stop writes and restore the verified pre-upgrade backup if rollback is required. In particular, rolling back to an application without idempotency support removes duplicate-request protection. Do not drop receipt records as routine cleanup without a defined retention/replay policy.
