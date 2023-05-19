import { test } from '@playwright/test';


test('open reported issues screen', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
});

test('reported issues screen displays map', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.locator('#map').click();
});

test('reported issues screen displays table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('table').click();
});

test('apply filters to reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  // Opens filters
  await page.locator('div').filter({ hasText: /\+$/ }).locator('span').click();
  // Filters to only display Bike Lane issues submitted by User 1
  await page.getByRole('combobox').first().selectOption('003');
  await page.getByRole('combobox').nth(1).selectOption('user1');
});

test('view expanded issue details from reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('cell', { name: /Two city buses parked obstructing .*/ }).click();
  await page.getByRole('img', { name: 'detail view' }).click();
  await page.getByText('Type: Bike Lane').click();
  await page.getByText('Description:Two city buses parked obstructing the bike lane rather than using th').click();
  await page.getByText('Location:270 Temple St New Haven, CT, 06511, USA').click();
});


/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();

// });