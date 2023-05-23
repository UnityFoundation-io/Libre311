import { expect, test } from '@playwright/test';

test('open reported issues screen', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.locator('#map').isVisible();
  await page.getByRole('table').isVisible();
});

test('apply filters to reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  // Opens filters
  await page.locator('div').filter({ hasText: /\+$/ }).locator('span').click();
  // Filters to only display Bike Lane issues submitted by User 1
  await page.getByRole('combobox').first().selectOption({ index:2 });
  await page.getByRole('combobox').nth(1).selectOption({ index:0 });
});

test('view expanded issue details from reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('cell', { name: /Two city buses parked obstructing .*/ }).click();
  await page.getByRole('img', { name: 'detail view' }).click();
  await expect(page.getByText('Type: Bike Lane')).toBeVisible();
  await expect(page.getByText('Description:Two city buses parked obstructing the bike lane rather than using th')).toBeVisible();
  await expect(page.getByText('Location:270 Temple St New Haven, CT, 06511, USA')).toBeVisible();
});


/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();

// });