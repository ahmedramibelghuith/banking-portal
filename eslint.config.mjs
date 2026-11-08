import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import angular from 'angular-eslint';
export default tseslint.config(
  { ignores: ['dist/**', '.angular/**', 'node_modules/**', 'GestionBanque/**', 'test-results/**'] },
  {
    files: ['src/**/*.ts', 'e2e/**/*.ts', 'playwright.config.ts'],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      // Preserve NgModules and zone-driven mutable views; an OnPush migration needs separate state changes.
      '@angular-eslint/prefer-standalone': 'off',
      '@angular-eslint/prefer-on-push-component-change-detection': 'off',
      '@typescript-eslint/consistent-type-imports': 'error',
    },
  },
  { files: ['src/**/*.html'], extends: [...angular.configs.templateRecommended] },
);
