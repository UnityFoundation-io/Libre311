import { expect, test } from '@playwright/test';

test('homepage features logo and wemove text', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('img', { name: 'we move logo' }).click();
  await page.getByText('We Move').click();
});

test('homepage features slogan text', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByText('Empowering Communities Together:').click();
  await page.getByText('Share the location of a walking, biking, or transit issue with our mobile app').click();
})
