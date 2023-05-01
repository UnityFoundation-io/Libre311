import { test } from '@playwright/test';


test('open reported issues screen', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
});

test('reported issues screen displays map', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.locator('div').filter({ hasText: /^To navigate, press the arrow keys\.$/ }).first().click();
});

test('reported issues screen displays table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('cell', { name: 'Two city buses parked obstructing the bike lane rather than using the bus lane' }).click();
});

test('apply filters to reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  // Opens filters
  await page.locator('span').filter({ hasText: '+' }).click();
  // Filters to only display Bus Stop issues submitted by User 1
  await page.getByRole('combobox').first().selectOption('002');
  await page.getByRole('combobox').nth(1).selectOption('user1');
});

test('view expanded issue details from reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('cell', { name: 'Green light out' }).click();
  await page.getByRole('img', { name: 'detail view' }).click();
  await page.getByRole('button', { name: 'Close', exact: true }).click();
});


/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();

// });