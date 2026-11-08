import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  use: {
    baseURL: 'http://127.0.0.1:4200',
    trace: 'retain-on-failure',
    launchOptions: { executablePath: process.env['E2E_CHROME_BIN'] || undefined },
  },
  webServer: [
    {
      command: 'cd GestionBanque && ./mvnw -B -ntp spring-boot:run',
      url: 'http://127.0.0.1:8080/api/auth/csrf',
      timeout: 180_000,
      reuseExistingServer: false,
      env: {
        SPRING_PROFILES_ACTIVE: 'default',
        SERVER_PORT: '8080',
        AUTH_REQUESTS_PER_MINUTE: '1000',
        DB_BASELINE_ON_MIGRATE: 'false',
        DB_URL: 'jdbc:h2:mem:e2e;DB_CLOSE_DELAY=-1',
        DB_USER: 'sa',
        DB_PASSWORD: '',
        ADMIN_EMAIL: 'admin@e2e.example',
        ADMIN_PASSWORD: 'E2e-test-password-123',
        COOKIE_SECURE: 'false',
      },
    },
    {
      command: 'npm start -- --host 127.0.0.1',
      url: 'http://127.0.0.1:4200/login',
      timeout: 90_000,
      reuseExistingServer: false,
    },
  ],
});
