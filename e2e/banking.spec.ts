import type { Page } from '@playwright/test';
import { test, expect } from '@playwright/test';
const password = 'Customer-password-123';
async function register(page: Page, email: string) {
  await page.goto('/register');
  await page.getByLabel('First name', { exact: true }).fill('Browser');
  await page.getByLabel('Last name', { exact: true }).fill('Customer');
  await page.getByLabel('Email address', { exact: true }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByLabel('Confirm password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Create account', exact: true }).click();
  await expect(page).toHaveURL(/\/login\?registered=true$/);
  await expect(page.getByRole('status')).toContainText('Account created');
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page).toHaveURL(/\/home$/);
}
test('customer registration, transactions, history, closure, and session persistence', async ({
  page,
}) => {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  const email = `customer-${Date.now()}@example.com`;
  await register(page, email);
  await page.getByRole('button', { name: '+ Current account', exact: true }).click();
  const current = page
    .locator('.account-card')
    .filter({ has: page.getByRole('heading', { name: 'Current account', exact: true }) });
  await expect(current).toBeVisible();
  await page.getByRole('button', { name: '+ Savings account', exact: true }).click();
  const savings = page
    .locator('.account-card')
    .filter({ has: page.getByRole('heading', { name: 'Savings account', exact: true }) });
  await expect(savings).toBeVisible();
  const savingsId = (await savings.locator('.muted').innerText()).match(/Account #(\d+)/)![1];
  await current.getByRole('button', { name: 'Deposit', exact: true }).click();
  let dialog = page.getByRole('dialog');
  await dialog.getByLabel('Amount (USD)').fill('100.25');
  await dialog.getByRole('button', { name: 'Deposit', exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(current.locator('.account-balance')).toHaveText('$100.25');
  await current.getByRole('button', { name: 'Transfer', exact: true }).click();
  dialog = page.getByRole('dialog');
  await dialog.getByLabel('Destination account ID').fill(savingsId);
  await dialog.getByLabel('Amount (USD)').fill('25.25');
  await dialog.getByRole('button', { name: 'Transfer', exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(savings.locator('.account-balance')).toHaveText('$25.25');
  await current.getByRole('button', { name: 'Withdraw', exact: true }).click();
  dialog = page.getByRole('dialog');
  await dialog.getByLabel('Amount (USD)').fill('10.00');
  await dialog.getByRole('button', { name: 'Withdraw', exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(current.locator('.account-balance')).toHaveText('$65.00');
  await current.getByRole('button', { name: 'Withdraw', exact: true }).click();
  dialog = page.getByRole('dialog');
  await dialog.getByLabel('Amount (USD)').fill('1000');
  await dialog.getByRole('button', { name: 'Withdraw', exact: true }).click();
  await expect(dialog.getByRole('alert')).toContainText('Insufficient funds');
  await dialog.getByRole('button', { name: 'Cancel' }).click();
  await current.getByRole('button', { name: 'History', exact: true }).click();
  await expect(page.locator('.history tbody tr')).toHaveCount(3);
  // Closing a funded account fails without losing its balance.
  page.on('dialog', (dialog) => dialog.accept());
  await savings.getByRole('button', { name: 'Close account', exact: true }).click();
  await expect(page.getByRole('alert')).toContainText('Empty the account');
  await savings.getByRole('button', { name: 'Withdraw', exact: true }).click();
  dialog = page.getByRole('dialog');
  await dialog.getByLabel('Amount (USD)').fill('25.25');
  await dialog.getByRole('button', { name: 'Withdraw', exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(savings.locator('.account-balance')).toHaveText('$0.00');
  await savings.getByRole('button', { name: 'Close account', exact: true }).click();
  await expect(savings.locator('.badge')).toHaveText('CLOSED');
  await page.reload();
  await expect(current.locator('.account-balance')).toHaveText('$65.00');
  await page.getByRole('button', { name: 'Sign out', exact: true }).click();
  await expect(page).toHaveURL(/\/login$/);
  expect((await page.request.get('/api/auth/me')).status()).toBe(401);
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Password', { exact: true }).fill('wrong-password');
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page.getByRole('alert')).toBeVisible();
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page).toHaveURL(/\/home$/);
  await expect(current.locator('.account-balance')).toHaveText('$65.00');
  expect(errors).toEqual([]);
});
test('customer cannot access admin screens or admin API', async ({ page }) => {
  await page.goto('/admin');
  await expect(page).toHaveURL(/\/login$/);
  await register(page, `restricted-${Date.now()}@example.com`);
  await page.goto('/admin');
  await expect(page).toHaveURL(/\/home$/);
  const response = await page.request.get('/api/admin/clients');
  expect(response.status()).toBe(403);
});
test('administrator can inspect clients, accounts, and ledger entries', async ({ page }) => {
  const seededEmail = `admin-record-${Date.now()}@example.com`;
  await register(page, seededEmail);
  await page.getByRole('button', { name: '+ Current account', exact: true }).click();
  const seeded = page.locator('.account-card');
  await expect(seeded).toBeVisible();
  const seededId = (await seeded.locator('.muted').innerText()).match(/Account #(\d+)/)![1];
  await seeded.getByRole('button', { name: 'Deposit', exact: true }).click();
  await page.getByRole('dialog').getByLabel('Amount (USD)').fill('42.17');
  await page.getByRole('dialog').getByRole('button', { name: 'Deposit', exact: true }).click();
  await expect(page.getByRole('dialog')).toBeHidden();
  await expect(seeded.locator('.account-balance')).toHaveText('$42.17');
  await page.getByRole('button', { name: 'Sign out', exact: true }).click();

  await page.goto('/login');
  await page.getByLabel('Email address').fill('admin@e2e.example');
  await page.getByLabel('Password', { exact: true }).fill('E2e-test-password-123');
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page).toHaveURL(/\/admin$/);
  for (const [name, expected] of [
    ['View clients', seededEmail],
    ['View accounts', seededEmail],
    ['View transactions', seededId],
  ]) {
    await page.getByRole('button', { name: new RegExp(name) }).click();
    await expect(
      page.getByRole('dialog').locator('tbody tr').filter({ hasText: expected }).first(),
    ).toBeVisible();
    if (name === 'View transactions')
      await expect(page.getByRole('dialog').locator('tbody')).toContainText('$42.17');
    await page.getByRole('dialog').getByRole('button', { name: 'Close', exact: true }).click();
  }
});

test('a lost deposit response can be retried without crediting the account twice', async ({
  page,
}) => {
  await register(page, `retry-${Date.now()}@example.com`);
  await page.getByRole('button', { name: '+ Current account', exact: true }).click();
  const account = page.locator('.account-card');
  await expect(account).toBeVisible();
  const keys: string[] = [];
  await page.route('**/api/operations/versement', async (route) => {
    keys.push(route.request().headers()['idempotency-key']);
    const response = await route.fetch();
    if (keys.length === 1) await route.abort('failed');
    else await route.fulfill({ response });
  });
  await account.getByRole('button', { name: 'Deposit', exact: true }).click();
  const dialog = page.getByRole('dialog');
  await dialog.getByLabel('Amount (USD)').fill('10.2');
  await dialog.getByRole('button', { name: 'Deposit', exact: true }).click();
  await expect(dialog.getByRole('alert')).toContainText('response was interrupted');
  await dialog.getByLabel('Amount (USD)').fill('10.20');
  await dialog.getByRole('button', { name: 'Deposit', exact: true }).click();
  await expect(dialog).toBeHidden();
  await expect(account.locator('.account-balance')).toHaveText('$10.20');
  expect(keys).toHaveLength(2);
  expect(keys[1]).toBe(keys[0]);
  await account.getByRole('button', { name: 'History', exact: true }).click();
  await expect(page.locator('.history tbody tr')).toHaveCount(1);
  await expect(page.locator('.history tbody')).toContainText('$10.20');
});
