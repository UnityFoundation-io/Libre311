import { expect, test } from '@playwright/test';

test('homepage features logo', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await expect(page.getByRole('img', { name: 'we move logo' })).toBeVisible();
});